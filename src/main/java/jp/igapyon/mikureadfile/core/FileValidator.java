package jp.igapyon.mikureadfile.core;

import com.fasterxml.jackson.databind.JsonNode;

import jp.igapyon.mikureadfile.model.EffectiveFileRequest;
import jp.igapyon.mikureadfile.model.RangeRequest;

final class FileValidator {
    private FileValidator() {
    }

    static EffectiveFileRequest validateFile(JsonNode node, String fieldPath) throws ValidationException {
        EffectiveFileRequest file = new EffectiveFileRequest();
        if (node != null && node.isTextual()) {
            validateRelativeFilePath(node.textValue(), fieldPath);
            file.path = node.textValue();
            return file;
        }
        if (node == null || !node.isObject()) {
            ValidationCommon.fail("file entry must be a string or object", fieldPath);
        }
        String unknown = ValidationCommon.firstUnknown(node, ValidationCommon.shape("path", "range", "encoding"));
        if (unknown != null) {
            ValidationCommon.fail("unknown file entry field: " + unknown, fieldPath + "." + unknown);
        }
        if (!node.has("path") || !node.get("path").isTextual()) {
            ValidationCommon.fail("file path is required", fieldPath + ".path");
        }
        file.path = node.get("path").textValue();
        validateRelativeFilePath(file.path, fieldPath + ".path");
        JsonNode rangeNode = node.get("range");
        if (rangeNode != null) {
            if (!rangeNode.isObject()) {
                ValidationCommon.fail("range must be an object", fieldPath + ".range");
            }
            String unknownRange = ValidationCommon.firstUnknown(rangeNode, ValidationCommon.shape("startLine", "lineCount"));
            if (unknownRange != null) {
                ValidationCommon.fail("unknown range field: " + unknownRange, fieldPath + ".range." + unknownRange);
            }
            RangeRequest range = new RangeRequest();
            range.startLine = (int) ValidationCommon.positiveInteger(rangeNode.get("startLine"), fieldPath + ".range.startLine", "range.startLine must be an integer greater than or equal to 1");
            range.lineCount = (int) ValidationCommon.positiveInteger(rangeNode.get("lineCount"), fieldPath + ".range.lineCount", "range.lineCount must be an integer greater than or equal to 1");
            file.range = range;
        }
        JsonNode encodingNode = node.get("encoding");
        if (encodingNode != null) {
            if (!ValidationCommon.isSupportedEncoding(encodingNode)) {
                ValidationCommon.fail("unsupported file encoding", fieldPath + ".encoding");
            }
            file.encoding = encodingNode.textValue();
        }
        return file;
    }

    private static void validateRelativeFilePath(String filePath, String fieldPath) throws ValidationException {
        if (filePath.length() == 0) {
            ValidationCommon.fail("file path must not be empty", fieldPath);
        }
        if (filePath.indexOf('\\') >= 0) {
            ValidationCommon.fail("file path must use / as separator", fieldPath);
        }
        if (filePath.startsWith("/") || filePath.startsWith("//") || filePath.matches("^[A-Za-z]:/.*")) {
            ValidationCommon.fail("absolute file paths are not allowed", fieldPath);
        }
        if (PathSecurity.hasParentSegment(filePath)) {
            ValidationCommon.fail("file path must not contain .. path segments", fieldPath);
        }
    }
}
