package jp.igapyon.mikureadfile.core;

public class ValidationException extends Exception {
    private static final long serialVersionUID = 1L;

    private final String code;
    private final String path;

    public ValidationException(String code, String message, String path) {
        super(message);
        this.code = code;
        this.path = path;
    }

    public String getCode() {
        return code;
    }

    public String getPath() {
        return path;
    }
}
