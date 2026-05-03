package jp.igapyon.mikureadfile.core;

import java.util.LinkedHashMap;
import java.util.Map;

import jp.igapyon.mikureadfile.model.Diagnostic;

public final class Diagnostics {
    private Diagnostics() {
    }

    public static Diagnostic error(String code, String message) {
        Diagnostic diagnostic = new Diagnostic();
        diagnostic.severity = "error";
        diagnostic.code = code;
        diagnostic.message = message;
        return diagnostic;
    }

    public static Diagnostic rootError(String code, String message, String path) {
        Diagnostic diagnostic = error(code, message);
        diagnostic.path = path;
        return diagnostic;
    }

    public static Diagnostic fileError(String code, String message, String file) {
        Diagnostic diagnostic = error(code, message);
        diagnostic.file = file;
        diagnostic.skipped = Boolean.TRUE;
        return diagnostic;
    }

    public static Diagnostic fileError(String code, String message, String file, Map<String, Object> details) {
        Diagnostic diagnostic = fileError(code, message, file);
        diagnostic.details = details;
        return diagnostic;
    }

    public static Diagnostic maxTotalBytesExceeded(long maxTotalBytes) {
        Diagnostic diagnostic = error("max_total_bytes_exceeded", "total filesystem bytes exceeded maxTotalBytes");
        diagnostic.skipped = Boolean.TRUE;
        diagnostic.details = new LinkedHashMap<String, Object>();
        diagnostic.details.put("maxTotalBytes", maxTotalBytes);
        return diagnostic;
    }

    public static Map<String, Object> details(String firstKey, Object firstValue) {
        Map<String, Object> details = new LinkedHashMap<String, Object>();
        details.put(firstKey, firstValue);
        return details;
    }
}
