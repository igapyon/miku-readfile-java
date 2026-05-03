package jp.igapyon.mikureadfile.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EffectiveRequest {
    public int version = 1;
    public String root;
    public List<EffectiveFileRequest> files;
    public Encoding encoding = new Encoding();
    public Limits limits = new Limits();

    public static class Encoding {
        @JsonProperty("default")
        public String defaultEncoding = "utf-8";
        public Map<String, String> extensions = new LinkedHashMap<String, String>();
    }

    public static class Limits {
        public long maxFileBytes = 10485760L;
        public int maxFiles = 100;
        public long maxTotalBytes = 4194304L;
    }
}
