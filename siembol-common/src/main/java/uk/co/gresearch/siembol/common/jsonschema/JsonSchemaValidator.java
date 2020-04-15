package uk.co.gresearch.siembol.common.jsonschema;

import uk.co.gresearch.siembol.common.result.SiembolResult;

public interface JsonSchemaValidator {
    SiembolResult getJsonSchema();
    SiembolResult validate(String json);
}
