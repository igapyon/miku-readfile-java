package jp.igapyon.mikureadfile.core;

import com.fasterxml.jackson.databind.JsonNode;

import jp.igapyon.mikureadfile.model.EffectiveRequest;

final class LimitsValidator {
    private LimitsValidator() {
    }

    static EffectiveRequest.Limits validateLimits(JsonNode node) throws ValidationException {
        EffectiveRequest.Limits limits = new EffectiveRequest.Limits();
        if (node == null || node.isMissingNode() || node.isNull()) {
            return limits;
        }
        if (!node.isObject()) {
            ValidationCommon.fail("limits must be an object", "limits");
        }
        String unknown = ValidationCommon.firstUnknown(node, ValidationCommon.shape("maxFileBytes", "maxFiles", "maxTotalBytes"));
        if (unknown != null) {
            ValidationCommon.fail("unknown limits field: " + unknown, "limits." + unknown);
        }
        if (node.has("maxFileBytes")) {
            limits.maxFileBytes = ValidationCommon.integerInRange(node.get("maxFileBytes"), 1L, Constants.LIMIT_MAX_FILE_BYTES, "limits.maxFileBytes");
        }
        if (node.has("maxFiles")) {
            limits.maxFiles = (int) ValidationCommon.integerInRange(node.get("maxFiles"), 1L, Constants.LIMIT_MAX_FILES, "limits.maxFiles");
        }
        if (node.has("maxTotalBytes")) {
            limits.maxTotalBytes = ValidationCommon.integerInRange(node.get("maxTotalBytes"), 1L, Constants.LIMIT_MAX_TOTAL_BYTES, "limits.maxTotalBytes");
        }
        return limits;
    }
}
