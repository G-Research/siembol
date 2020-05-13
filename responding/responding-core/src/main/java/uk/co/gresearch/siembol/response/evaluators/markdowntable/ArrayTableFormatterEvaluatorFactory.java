package uk.co.gresearch.siembol.response.evaluators.markdowntable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import uk.co.gresearch.siembol.common.jsonschema.SiembolJsonSchemaValidator;
import uk.co.gresearch.siembol.common.result.SiembolResult;
import uk.co.gresearch.siembol.response.common.*;
import uk.co.gresearch.siembol.response.model.ArrayTableFormatterEvaluatorAttributesDto;

public class ArrayTableFormatterEvaluatorFactory implements RespondingEvaluatorFactory {
    private static final ObjectReader JSON_ATTRIBUTES_READER = new ObjectMapper()
            .readerFor(ArrayTableFormatterEvaluatorAttributesDto.class);
    private final SiembolJsonSchemaValidator attributesSchema;

    public ArrayTableFormatterEvaluatorFactory() throws Exception {
        attributesSchema = new SiembolJsonSchemaValidator(ArrayTableFormatterEvaluatorAttributesDto.class);
    }

    @Override
    public RespondingResult createInstance(String attributes) {
        try {
            SiembolResult validationResult = attributesSchema.validate(attributes);
            if (validationResult.getStatusCode() != SiembolResult.StatusCode.OK) {
                return RespondingResult.fromSiembolResult(validationResult);
            }
            ArrayTableFormatterEvaluatorAttributesDto attributesDto = JSON_ATTRIBUTES_READER.readValue(attributes);
            TableFormatter.Builder builder = new TableFormatter.Builder()
                    .fieldName(attributesDto.getFieldName())
                    .arrayFieldName(attributesDto.getArrayField())
                    .tableName(attributesDto.getTableName());

            if (attributesDto.getFieldFilterDto() != null) {
                builder.patternFilter(attributesDto.getFieldFilterDto().getIncludingFields(),
                        attributesDto.getFieldFilterDto().getExcludingFields());
            }
            return RespondingResult.fromEvaluator(builder.build());
        } catch (Exception e) {
            return RespondingResult.fromException(e);
        }
    }

    @Override
    public RespondingResult getType() {
        return RespondingResult.fromEvaluatorType(ProvidedEvaluators.ARRAY_TABLE_FORMATTER_EVALUATOR.toString());
    }

    @Override
    public RespondingResult getAttributesJsonSchema() {
        return RespondingResult.fromAttributesSchema(
                attributesSchema.getJsonSchema().getAttributes().getJsonSchema());
    }
}
