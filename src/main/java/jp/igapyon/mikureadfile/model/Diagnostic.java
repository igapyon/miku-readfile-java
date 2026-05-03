package jp.igapyon.mikureadfile.model;

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
}
