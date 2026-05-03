package jp.igapyon.mikureadfile.core;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import jp.igapyon.mikureadfile.model.Diagnostic;
import jp.igapyon.mikureadfile.model.EffectiveFileRequest;
import jp.igapyon.mikureadfile.model.EffectiveRequest;
import jp.igapyon.mikureadfile.model.FileResult;
import jp.igapyon.mikureadfile.model.MikuReadfileResult;

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

        Root.Check rootCheck = Root.checkRoot(cwd.resolve(effectiveRequest.root).normalize(), effectiveRequest.root);
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
            FileReader.ReadOneResult read = FileReader.readOne(request, rootRealPath, file);
            if (!read.ok) {
                diagnostics.add(read.diagnostic);
                continue;
            }
            totalBytes += read.file.bytes;
            if (totalBytes > request.limits.maxTotalBytes) {
                List<Diagnostic> totalDiagnostics = new ArrayList<Diagnostic>();
                totalDiagnostics.add(Diagnostics.maxTotalBytesExceeded(request.limits.maxTotalBytes));
                return ResultFactory.finish(new ArrayList<FileResult>(), totalDiagnostics, request.files.size(), false);
            }
            files.add(read.file);
        }
        return ResultFactory.finish(files, diagnostics, request.files.size());
    }
}
