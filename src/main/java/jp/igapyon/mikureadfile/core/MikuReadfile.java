package jp.igapyon.mikureadfile.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import jp.igapyon.mikureadfile.model.Diagnostic;
import jp.igapyon.mikureadfile.model.EffectiveFileRequest;
import jp.igapyon.mikureadfile.model.EffectiveRequest;
import jp.igapyon.mikureadfile.model.FileResult;
import jp.igapyon.mikureadfile.model.MikuReadfileResult;
import jp.igapyon.mikureadfile.model.RangeResult;

public final class MikuReadfile {
    private MikuReadfile() {
    }

    public static MikuReadfileResult runRequest(JsonNode request) {
        return runRequest(request, Paths.get("").toAbsolutePath());
    }

    public static MikuReadfileResult runRequest(JsonNode request, Path cwd) {
        EffectiveRequest effectiveRequest;
        try {
            effectiveRequest = RequestValidator.validateAndNormalize(request);
        } catch (ValidationException ex) {
            return ResultFactory.validationFailure(ex.getCode(), ex.getMessage(), ex.getPath());
        }

        RootCheck rootCheck = checkRoot(cwd.resolve(effectiveRequest.root).normalize(), effectiveRequest.root);
        if (!rootCheck.ok) {
            List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();
            diagnostics.add(rootCheck.diagnostic);
            return ResultFactory.finish(new ArrayList<FileResult>(), diagnostics, effectiveRequest.files.size(), false);
        }
        return readFiles(effectiveRequest, rootCheck.realPath);
    }

    private static MikuReadfileResult readFiles(EffectiveRequest request, Path rootRealPath) {
        List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();
        List<FileResult> files = new ArrayList<FileResult>();
        long totalBytes = 0L;

        for (EffectiveFileRequest file : request.files) {
            ReadOneResult read = readOne(request, rootRealPath, file);
            if (!read.ok) {
                diagnostics.add(read.diagnostic);
                continue;
            }
            totalBytes += read.file.bytes;
            if (totalBytes > request.limits.maxTotalBytes) {
                List<Diagnostic> totalDiagnostics = new ArrayList<Diagnostic>();
                totalDiagnostics.add(Diagnostic.error("max_total_bytes_exceeded", "total filesystem bytes exceeded maxTotalBytes")
                        .withDetail("maxTotalBytes", request.limits.maxTotalBytes));
                totalDiagnostics.get(0).skipped = Boolean.TRUE;
                return ResultFactory.finish(new ArrayList<FileResult>(), totalDiagnostics, request.files.size(), false);
            }
            files.add(read.file);
        }
        return ResultFactory.finish(files, diagnostics, request.files.size());
    }

    private static ReadOneResult readOne(EffectiveRequest request, Path rootRealPath, EffectiveFileRequest file) {
        Candidate candidate = resolveReadableFile(request, rootRealPath, file);
        if (!candidate.ok) {
            return ReadOneResult.failure(candidate.diagnostic);
        }
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(candidate.realPath);
        } catch (IOException ex) {
            return ReadOneResult.failure(Diagnostic.fileError("file_not_readable", "file could not be read", file.path));
        }
        if (containsZero(bytes)) {
            return ReadOneResult.failure(Diagnostic.fileError("binary_file_skipped", "binary file was skipped", file.path));
        }
        String encoding = Encoding.selectEncoding(request, file);
        String decoded;
        try {
            decoded = Encoding.decode(bytes, encoding);
        } catch (Exception ex) {
            return ReadOneResult.failure(Diagnostic.fileError("decode_error", "file could not be decoded", file.path));
        }
        return ReadOneResult.success(createFileResult(file, encoding, decoded, hasUtf8Bom(bytes), candidate.size, candidate.modifiedTime));
    }

    private static Candidate resolveReadableFile(EffectiveRequest request, Path rootRealPath, EffectiveFileRequest file) {
        Path absolutePath = rootRealPath;
        String[] segments = file.path.split("/");
        for (String segment : segments) {
            absolutePath = absolutePath.resolve(segment);
        }
        if (!Files.exists(absolutePath, LinkOption.NOFOLLOW_LINKS)) {
            return Candidate.failure(Diagnostic.fileError("file_not_readable", "file could not be read", file.path));
        }
        if (Files.isSymbolicLink(absolutePath)) {
            return Candidate.failure(Diagnostic.fileError("symlink_skipped", "symlink was skipped", file.path));
        }
        Path realPath;
        try {
            realPath = absolutePath.toRealPath();
        } catch (IOException ex) {
            return Candidate.failure(Diagnostic.fileError("file_not_readable", "file could not be resolved", file.path));
        }
        if (!PathSecurity.isPathInsideOrSame(realPath, rootRealPath)) {
            return Candidate.failure(Diagnostic.fileError("path_escape_skipped", "path resolved outside root and was skipped", file.path));
        }
        if (!Files.isRegularFile(realPath)) {
            return Candidate.failure(Diagnostic.fileError("not_file", "path is not a regular file", file.path));
        }
        if (!Files.isReadable(realPath)) {
            return Candidate.failure(Diagnostic.fileError("file_not_readable", "file could not be read", file.path));
        }
        long size;
        FileTime modifiedTime;
        try {
            size = Files.size(realPath);
            modifiedTime = Files.getLastModifiedTime(realPath);
        } catch (IOException ex) {
            return Candidate.failure(Diagnostic.fileError("file_not_readable", "file could not be read", file.path));
        }
        if (size > request.limits.maxFileBytes) {
            return Candidate.failure(Diagnostic.fileError("max_file_bytes_exceeded", "file exceeded maxFileBytes and was skipped", file.path)
                    .withDetail("size", size)
                    .withDetail("maxFileBytes", request.limits.maxFileBytes));
        }
        return Candidate.success(realPath, size, modifiedTime.toInstant().toString());
    }

    private static FileResult createFileResult(EffectiveFileRequest file, String encoding, String decoded, boolean utf8BomPresent, long bytes, String modifiedTime) {
        TextShape shaped = TextShape.shapeText(decoded, encoding, utf8BomPresent);
        RangeText effectiveRange = file.range == null ? null : RangeText.rangeText(shaped, file.range.startLine, file.range.lineCount);
        FileResult result = new FileResult();
        result.file = file.path.replace(java.io.File.separatorChar, '/');
        result.encoding = encoding;
        result.bom = shaped.bom;
        result.lineEnding = shaped.lineEnding;
        result.finalNewline = shaped.finalNewline;
        result.bytes = bytes;
        result.lines = shaped.logicalLineCount;
        result.modifiedTime = modifiedTime;
        if (file.range != null && effectiveRange != null) {
            RangeResult range = new RangeResult();
            range.startLine = file.range.startLine;
            range.lineCount = effectiveRange.lineCount;
            range.endLine = effectiveRange.endLine;
            range.eof = effectiveRange.eof;
            result.range = range;
            result.text = effectiveRange.text;
        } else {
            result.range = null;
            result.text = shaped.text;
        }
        return result;
    }

    private static RootCheck checkRoot(Path rootPath, String requestRoot) {
        if (!Files.exists(rootPath) || !Files.isDirectory(rootPath) || !Files.isReadable(rootPath)) {
            String code = Files.exists(rootPath) ? "root_not_accessible" : "root_not_found";
            String message = "root_not_found".equals(code) ? "root does not exist" : "root is not accessible";
            return RootCheck.failure(Diagnostic.rootError(code, message, requestRoot));
        }
        Path realPath;
        try {
            realPath = rootPath.toRealPath();
        } catch (IOException ex) {
            return RootCheck.failure(Diagnostic.rootError("root_not_accessible", "root is not accessible", requestRoot));
        }
        Path filesystemRoot = realPath.getRoot();
        Path home = Paths.get(System.getProperty("user.home")).toAbsolutePath().normalize();
        try {
            home = home.toRealPath();
        } catch (IOException ex) {
            // Keep normalized home path when the platform cannot resolve it.
        }
        if ((filesystemRoot != null && realPath.equals(filesystemRoot)) || realPath.equals(home)) {
            return RootCheck.failure(Diagnostic.rootError("root_too_broad", "root is too broad", requestRoot));
        }
        return RootCheck.success(realPath);
    }

    private static boolean containsZero(byte[] bytes) {
        for (byte value : bytes) {
            if (value == 0) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasUtf8Bom(byte[] bytes) {
        return bytes.length >= 3 && (bytes[0] & 0xff) == 0xef && (bytes[1] & 0xff) == 0xbb && (bytes[2] & 0xff) == 0xbf;
    }

    private static class RootCheck {
        boolean ok;
        Path realPath;
        Diagnostic diagnostic;

        static RootCheck success(Path realPath) {
            RootCheck check = new RootCheck();
            check.ok = true;
            check.realPath = realPath;
            return check;
        }

        static RootCheck failure(Diagnostic diagnostic) {
            RootCheck check = new RootCheck();
            check.ok = false;
            check.diagnostic = diagnostic;
            return check;
        }
    }

    private static class Candidate {
        boolean ok;
        Path realPath;
        long size;
        String modifiedTime;
        Diagnostic diagnostic;

        static Candidate success(Path realPath, long size, String modifiedTime) {
            Candidate candidate = new Candidate();
            candidate.ok = true;
            candidate.realPath = realPath;
            candidate.size = size;
            candidate.modifiedTime = modifiedTime;
            return candidate;
        }

        static Candidate failure(Diagnostic diagnostic) {
            Candidate candidate = new Candidate();
            candidate.ok = false;
            candidate.diagnostic = diagnostic;
            return candidate;
        }
    }

    private static class ReadOneResult {
        boolean ok;
        FileResult file;
        Diagnostic diagnostic;

        static ReadOneResult success(FileResult file) {
            ReadOneResult result = new ReadOneResult();
            result.ok = true;
            result.file = file;
            return result;
        }

        static ReadOneResult failure(Diagnostic diagnostic) {
            ReadOneResult result = new ReadOneResult();
            result.ok = false;
            result.diagnostic = diagnostic;
            return result;
        }
    }
}
