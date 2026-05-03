package jp.igapyon.mikureadfile.model;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Diagnostic {
    public String severity;
    public String code;
    public String message;
    public String file;
    public String path;
    public Boolean skipped;
    public Map<String, Object> details;

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

    public Diagnostic withDetail(String key, Object value) {
        if (details == null) {
            details = new LinkedHashMap<String, Object>();
        }
        details.put(key, value);
        return this;
    }
}
