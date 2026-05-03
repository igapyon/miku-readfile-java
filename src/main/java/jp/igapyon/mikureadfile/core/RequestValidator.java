package jp.igapyon.mikureadfile.core;

import java.util.ArrayList;

import com.fasterxml.jackson.databind.JsonNode;

import jp.igapyon.mikureadfile.model.EffectiveFileRequest;
import jp.igapyon.mikureadfile.model.EffectiveRequest;

public final class RequestValidator {
    private RequestValidator() {
    }

    public static EffectiveRequest validateAndNormalize(JsonNode input) throws ValidationException {
        if (input == null || !input.isObject()) {
            ValidationCommon.fail("request must be a JSON object", null);
        }
        String unknown = ValidationCommon.firstUnknown(input, ValidationCommon.shape("version", "root", "files", "encoding", "limits"));
        if (unknown != null) {
            ValidationCommon.fail("unknown request field: " + unknown, unknown);
        }
        if (!input.has("version") || !input.get("version").isInt() || input.get("version").intValue() != Constants.VERSION) {
            ValidationCommon.fail("version must be 1", "version");
        }
        if (!input.has("root") || !input.get("root").isTextual() || input.get("root").textValue().length() == 0) {
            ValidationCommon.fail("root is required", "root");
        }
        if (!input.has("files") || !input.get("files").isArray() || input.get("files").size() == 0) {
            ValidationCommon.fail("files must be a non-empty array", "files");
        }

        EffectiveRequest request = new EffectiveRequest();
        request.root = input.get("root").textValue();
        request.encoding = EncodingValidator.validateEncoding(input.get("encoding"));
        request.limits = LimitsValidator.validateLimits(input.get("limits"));
        if (input.get("files").size() > request.limits.maxFiles) {
            ValidationCommon.fail("files exceeds maxFiles", "files");
        }
        request.files = new ArrayList<EffectiveFileRequest>();
        for (int index = 0; index < input.get("files").size(); index++) {
            request.files.add(FileValidator.validateFile(input.get("files").get(index), "files." + index));
        }
        return request;
    }
}
