package jp.igapyon.mikureadfile.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

import jp.igapyon.mikureadfile.model.Diagnostic;
import jp.igapyon.mikureadfile.model.EffectiveFileRequest;
import jp.igapyon.mikureadfile.model.EffectiveRequest;
import jp.igapyon.mikureadfile.model.FileResult;

public final class FileReader {
    private FileReader() {
    }

    public static ReadOneResult readOne(EffectiveRequest request, Path rootRealPath, EffectiveFileRequest file) {
        Candidate candidate = resolveReadableFile(request, rootRealPath, file);
        if (!candidate.ok) {
            return ReadOneResult.failure(candidate.diagnostic);
        }
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(candidate.realPath);
        } catch (IOException ex) {
            return ReadOneResult.failure(Diagnostics.fileError("file_not_readable", "file could not be read", file.path));
        }
        if (containsZero(bytes)) {
            return ReadOneResult.failure(Diagnostics.fileError("binary_file_skipped", "binary file was skipped", file.path));
        }
        String encoding = Encoding.selectEncoding(request, file);
        String decoded;
        try {
            decoded = Encoding.decode(bytes, encoding);
        } catch (Exception ex) {
            return ReadOneResult.failure(Diagnostics.fileError("decode_error", "file could not be decoded", file.path));
        }
        return ReadOneResult.success(FileResultFactory.createFileResult(file, encoding, decoded, hasUtf8Bom(bytes), candidate.size, candidate.modifiedTime));
    }

    private static Candidate resolveReadableFile(EffectiveRequest request, Path rootRealPath, EffectiveFileRequest file) {
        Path absolutePath = rootRealPath;
        String[] segments = file.path.split("/");
        for (String segment : segments) {
            absolutePath = absolutePath.resolve(segment);
        }
        if (!Files.exists(absolutePath, LinkOption.NOFOLLOW_LINKS)) {
            return Candidate.failure(Diagnostics.fileError("file_not_readable", "file could not be read", file.path));
        }
        if (Files.isSymbolicLink(absolutePath)) {
            return Candidate.failure(Diagnostics.fileError("symlink_skipped", "symlink was skipped", file.path));
        }
        Path realPath;
        try {
            realPath = absolutePath.toRealPath();
        } catch (IOException ex) {
            return Candidate.failure(Diagnostics.fileError("file_not_readable", "file could not be resolved", file.path));
        }
        if (!PathSecurity.isPathInsideOrSame(realPath, rootRealPath)) {
            return Candidate.failure(Diagnostics.fileError("path_escape_skipped", "path resolved outside root and was skipped", file.path));
        }
        if (!Files.isRegularFile(realPath)) {
            return Candidate.failure(Diagnostics.fileError("not_file", "path is not a regular file", file.path));
        }
        if (!Files.isReadable(realPath)) {
            return Candidate.failure(Diagnostics.fileError("file_not_readable", "file could not be read", file.path));
        }
        long size;
        FileTime modifiedTime;
        try {
            size = Files.size(realPath);
            modifiedTime = Files.getLastModifiedTime(realPath);
        } catch (IOException ex) {
            return Candidate.failure(Diagnostics.fileError("file_not_readable", "file could not be read", file.path));
        }
        if (size > request.limits.maxFileBytes) {
            java.util.Map<String, Object> details = Diagnostics.details("size", size);
            details.put("maxFileBytes", request.limits.maxFileBytes);
            return Candidate.failure(Diagnostics.fileError("max_file_bytes_exceeded", "file exceeded maxFileBytes and was skipped", file.path, details));
        }
        return Candidate.success(realPath, size, modifiedTime.toInstant().toString());
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

    public static final class ReadOneResult {
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

    private static final class Candidate {
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
}
