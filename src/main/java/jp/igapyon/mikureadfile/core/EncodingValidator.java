package jp.igapyon.mikureadfile.core;

import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import jp.igapyon.mikureadfile.model.EffectiveRequest;

final class EncodingValidator {
    private EncodingValidator() {
    }

    static EffectiveRequest.Encoding validateEncoding(JsonNode node) throws ValidationException {
        EffectiveRequest.Encoding encoding = new EffectiveRequest.Encoding();
        if (node == null || node.isMissingNode() || node.isNull()) {
            return encoding;
        }
        if (!node.isObject()) {
            ValidationCommon.fail("encoding must be an object", "encoding");
        }
        String unknown = ValidationCommon.firstUnknown(node, ValidationCommon.shape("default", "extensions"));
        if (unknown != null) {
            ValidationCommon.fail("unknown encoding field: " + unknown, "encoding." + unknown);
        }
        JsonNode defaultNode = node.get("default");
        if (defaultNode != null) {
            if (!ValidationCommon.isSupportedEncoding(defaultNode)) {
                ValidationCommon.fail("unsupported default encoding", "encoding.default");
            }
            encoding.defaultEncoding = defaultNode.textValue();
        }
        JsonNode extensionsNode = node.get("extensions");
        if (extensionsNode != null) {
            if (!extensionsNode.isObject()) {
                ValidationCommon.fail("encoding.extensions must be an object", "encoding.extensions");
            }
            Iterator<Map.Entry<String, JsonNode>> fields = extensionsNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String extension = field.getKey();
                if (!extension.startsWith(".") || extension.indexOf('/') >= 0 || extension.indexOf('\\') >= 0) {
                    ValidationCommon.fail("encoding extension keys must be exact extensions with leading dot", "encoding.extensions." + extension);
                }
                if (!ValidationCommon.isSupportedEncoding(field.getValue())) {
                    ValidationCommon.fail("unsupported extension encoding", "encoding.extensions." + extension);
                }
                encoding.extensions.put(extension, field.getValue().textValue());
            }
        }
        return encoding;
    }
}
