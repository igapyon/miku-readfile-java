package jp.igapyon.mikureadfile.core;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

final class ValidationCommon {
    private ValidationCommon() {
    }

    static void fail(String message, String path) throws ValidationException {
        throw new ValidationException("validation_error", message, path);
    }

    static Map<String, Boolean> shape(String... names) {
        Map<String, Boolean> shape = new LinkedHashMap<String, Boolean>();
        for (String name : names) {
            shape.put(name, Boolean.TRUE);
        }
        return shape;
    }

    static String firstUnknown(JsonNode node, Map<String, Boolean> shape) {
        Iterator<String> fieldNames = node.fieldNames();
        while (fieldNames.hasNext()) {
            String name = fieldNames.next();
            if (!shape.containsKey(name)) {
                return name;
            }
        }
        return null;
    }

    static long positiveInteger(JsonNode node, String path, String message) throws ValidationException {
        if (node == null || !node.isIntegralNumber() || node.longValue() < 1L) {
            fail(message, path);
        }
        return node.longValue();
    }

    static long integerInRange(JsonNode node, long min, long max, String path) throws ValidationException {
        if (node == null || !node.isIntegralNumber() || node.longValue() < min || node.longValue() > max) {
            fail(path + " is out of range", path);
        }
        return node.longValue();
    }

    static boolean isSupportedEncoding(JsonNode node) {
        return node != null && node.isTextual() && ("utf-8".equals(node.textValue()) || "shift_jis".equals(node.textValue()));
    }
}
