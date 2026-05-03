package jp.igapyon.mikureadfile.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import jp.igapyon.mikureadfile.model.EffectiveFileRequest;
import jp.igapyon.mikureadfile.model.EffectiveRequest;
import jp.igapyon.mikureadfile.model.RangeRequest;

public final class RequestValidator {
    private RequestValidator() {
    }

    public static EffectiveRequest validateAndNormalize(JsonNode input) throws ValidationException {
        if (input == null || !input.isObject()) {
            fail("request must be a JSON object", null);
        }
        String unknown = firstUnknown(input, shape("version", "root", "files", "encoding", "limits"));
        if (unknown != null) {
            fail("unknown request field: " + unknown, unknown);
        }
        if (!input.has("version") || !input.get("version").isInt() || input.get("version").intValue() != Constants.VERSION) {
            fail("version must be 1", "version");
        }
        if (!input.has("root") || !input.get("root").isTextual() || input.get("root").textValue().length() == 0) {
            fail("root is required", "root");
        }
        if (!input.has("files") || !input.get("files").isArray() || input.get("files").size() == 0) {
            fail("files must be a non-empty array", "files");
        }

        EffectiveRequest request = new EffectiveRequest();
        request.root = input.get("root").textValue();
        request.encoding = validateEncoding(input.get("encoding"));
        request.limits = validateLimits(input.get("limits"));
        if (input.get("files").size() > request.limits.maxFiles) {
            fail("files exceeds maxFiles", "files");
        }
        request.files = new ArrayList<EffectiveFileRequest>();
        for (int index = 0; index < input.get("files").size(); index++) {
            request.files.add(validateFile(input.get("files").get(index), "files." + index));
        }
        return request;
    }

    private static EffectiveRequest.Encoding validateEncoding(JsonNode node) throws ValidationException {
        EffectiveRequest.Encoding encoding = new EffectiveRequest.Encoding();
        if (node == null || node.isMissingNode() || node.isNull()) {
            return encoding;
        }
        if (!node.isObject()) {
            fail("encoding must be an object", "encoding");
        }
        String unknown = firstUnknown(node, shape("default", "extensions"));
        if (unknown != null) {
            fail("unknown encoding field: " + unknown, "encoding." + unknown);
        }
        JsonNode defaultNode = node.get("default");
        if (defaultNode != null) {
            if (!isSupportedEncoding(defaultNode)) {
                fail("unsupported default encoding", "encoding.default");
            }
            encoding.defaultEncoding = defaultNode.textValue();
        }
        JsonNode extensionsNode = node.get("extensions");
        if (extensionsNode != null) {
            if (!extensionsNode.isObject()) {
                fail("encoding.extensions must be an object", "encoding.extensions");
            }
            Iterator<Map.Entry<String, JsonNode>> fields = extensionsNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String extension = field.getKey();
                if (!extension.startsWith(".") || extension.indexOf('/') >= 0 || extension.indexOf('\\') >= 0) {
                    fail("encoding extension keys must be exact extensions with leading dot", "encoding.extensions." + extension);
                }
                if (!isSupportedEncoding(field.getValue())) {
                    fail("unsupported extension encoding", "encoding.extensions." + extension);
                }
                encoding.extensions.put(extension, field.getValue().textValue());
            }
        }
        return encoding;
    }

    private static EffectiveRequest.Limits validateLimits(JsonNode node) throws ValidationException {
        EffectiveRequest.Limits limits = new EffectiveRequest.Limits();
        if (node == null || node.isMissingNode() || node.isNull()) {
            return limits;
        }
        if (!node.isObject()) {
            fail("limits must be an object", "limits");
        }
        String unknown = firstUnknown(node, shape("maxFileBytes", "maxFiles", "maxTotalBytes"));
        if (unknown != null) {
            fail("unknown limits field: " + unknown, "limits." + unknown);
        }
        if (node.has("maxFileBytes")) {
            limits.maxFileBytes = integerInRange(node.get("maxFileBytes"), 1L, Constants.LIMIT_MAX_FILE_BYTES, "limits.maxFileBytes");
        }
        if (node.has("maxFiles")) {
            limits.maxFiles = (int) integerInRange(node.get("maxFiles"), 1L, Constants.LIMIT_MAX_FILES, "limits.maxFiles");
        }
        if (node.has("maxTotalBytes")) {
            limits.maxTotalBytes = integerInRange(node.get("maxTotalBytes"), 1L, Constants.LIMIT_MAX_TOTAL_BYTES, "limits.maxTotalBytes");
        }
        return limits;
    }

    private static EffectiveFileRequest validateFile(JsonNode node, String fieldPath) throws ValidationException {
        EffectiveFileRequest file = new EffectiveFileRequest();
        if (node != null && node.isTextual()) {
            validateRelativeFilePath(node.textValue(), fieldPath);
            file.path = node.textValue();
            return file;
        }
        if (node == null || !node.isObject()) {
            fail("file entry must be a string or object", fieldPath);
        }
        String unknown = firstUnknown(node, shape("path", "range", "encoding"));
        if (unknown != null) {
            fail("unknown file entry field: " + unknown, fieldPath + "." + unknown);
        }
        if (!node.has("path") || !node.get("path").isTextual()) {
            fail("file path is required", fieldPath + ".path");
        }
        file.path = node.get("path").textValue();
        validateRelativeFilePath(file.path, fieldPath + ".path");
        JsonNode rangeNode = node.get("range");
        if (rangeNode != null) {
            if (!rangeNode.isObject()) {
                fail("range must be an object", fieldPath + ".range");
            }
            String unknownRange = firstUnknown(rangeNode, shape("startLine", "lineCount"));
            if (unknownRange != null) {
                fail("unknown range field: " + unknownRange, fieldPath + ".range." + unknownRange);
            }
            RangeRequest range = new RangeRequest();
            range.startLine = (int) positiveInteger(rangeNode.get("startLine"), fieldPath + ".range.startLine", "range.startLine must be an integer greater than or equal to 1");
            range.lineCount = (int) positiveInteger(rangeNode.get("lineCount"), fieldPath + ".range.lineCount", "range.lineCount must be an integer greater than or equal to 1");
            file.range = range;
        }
        JsonNode encodingNode = node.get("encoding");
        if (encodingNode != null) {
            if (!isSupportedEncoding(encodingNode)) {
                fail("unsupported file encoding", fieldPath + ".encoding");
            }
            file.encoding = encodingNode.textValue();
        }
        return file;
    }

    private static void validateRelativeFilePath(String filePath, String fieldPath) throws ValidationException {
        if (filePath.length() == 0) {
            fail("file path must not be empty", fieldPath);
        }
        if (filePath.indexOf('\\') >= 0) {
            fail("file path must use / as separator", fieldPath);
        }
        if (filePath.startsWith("/") || filePath.startsWith("//") || filePath.matches("^[A-Za-z]:/.*")) {
            fail("absolute file paths are not allowed", fieldPath);
        }
        if (PathSecurity.hasParentSegment(filePath)) {
            fail("file path must not contain .. path segments", fieldPath);
        }
    }

    private static long positiveInteger(JsonNode node, String path, String message) throws ValidationException {
        if (node == null || !node.isIntegralNumber() || node.longValue() < 1L) {
            fail(message, path);
        }
        return node.longValue();
    }

    private static long integerInRange(JsonNode node, long min, long max, String path) throws ValidationException {
        if (node == null || !node.isIntegralNumber() || node.longValue() < min || node.longValue() > max) {
            fail(path + " is out of range", path);
        }
        return node.longValue();
    }

    private static boolean isSupportedEncoding(JsonNode node) {
        return node != null && node.isTextual() && ("utf-8".equals(node.textValue()) || "shift_jis".equals(node.textValue()));
    }

    private static void fail(String message, String path) throws ValidationException {
        throw new ValidationException("validation_error", message, path);
    }

    private static Map<String, Boolean> shape(String... names) {
        Map<String, Boolean> shape = new LinkedHashMap<String, Boolean>();
        for (String name : names) {
            shape.put(name, Boolean.TRUE);
        }
        return shape;
    }

    private static String firstUnknown(JsonNode node, Map<String, Boolean> shape) {
        Iterator<String> fieldNames = node.fieldNames();
        while (fieldNames.hasNext()) {
            String name = fieldNames.next();
            if (!shape.containsKey(name)) {
                return name;
            }
        }
        return null;
    }
}
