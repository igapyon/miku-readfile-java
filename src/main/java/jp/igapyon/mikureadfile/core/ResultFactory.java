package jp.igapyon.mikureadfile.core;

import java.util.List;

import jp.igapyon.mikureadfile.model.Diagnostic;
import jp.igapyon.mikureadfile.model.FileResult;
import jp.igapyon.mikureadfile.model.MikuReadfileResult;

public final class ResultFactory {
    private ResultFactory() {
    }

    public static MikuReadfileResult validationFailure(String code, String message, String path) {
        MikuReadfileResult result = new MikuReadfileResult();
        Diagnostic diagnostic = Diagnostic.error(code, message);
        diagnostic.path = path;
        result.diagnostics.add(diagnostic);
        result.summary.requestedFiles = 0;
        result.summary.filesRead = 0;
        result.summary.filesSkipped = 0;
        result.summary.diagnostics = 1;
        result.ok = false;
        return result;
    }

    public static MikuReadfileResult finish(List<FileResult> files, List<Diagnostic> diagnostics, int requestedFiles) {
        return finish(files, diagnostics, requestedFiles, true);
    }

    public static MikuReadfileResult finish(List<FileResult> files, List<Diagnostic> diagnostics, int requestedFiles, boolean allowOk) {
        MikuReadfileResult result = new MikuReadfileResult();
        result.files.addAll(files);
        result.diagnostics.addAll(diagnostics);
        result.summary.requestedFiles = requestedFiles;
        result.summary.filesRead = files.size();
        result.summary.filesSkipped = Math.max(0, requestedFiles - files.size());
        result.summary.diagnostics = diagnostics.size();
        boolean hasError = false;
        for (Diagnostic diagnostic : diagnostics) {
            if ("error".equals(diagnostic.severity)) {
                hasError = true;
                break;
            }
        }
        result.ok = allowOk && result.summary.filesSkipped == 0 && !hasError;
        return result;
    }
}
