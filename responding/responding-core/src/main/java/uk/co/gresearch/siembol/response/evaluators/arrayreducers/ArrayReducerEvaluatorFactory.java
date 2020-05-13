package uk.co.gresearch.siembol.response.evaluators.arrayreducers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import uk.co.gresearch.siembol.common.jsonschema.SiembolJsonSchemaValidator;
import uk.co.gresearch.siembol.common.result.SiembolResult;
import uk.co.gresearch.siembol.response.common.ProvidedEvaluators;
import uk.co.gresearch.siembol.response.common.RespondingEvaluatorFactory;
import uk.co.gresearch.siembol.response.common.RespondingResult;
import uk.co.gresearch.siembol.response.model.ArrayReducerEvaluatorAttributesDto;

public class ArrayReducerEvaluatorFactory implements RespondingEvaluatorFactory {
    private static final ObjectReader JSON_ATTRIBUTES_READER = new ObjectMapper()
            .readerFor(ArrayReducerEvaluatorAttributesDto.class);
    private final SiembolJsonSchemaValidator attributesSchema;

    public ArrayReducerEvaluatorFactory() throws Exception {
        attributesSchema = new SiembolJsonSchemaValidator(ArrayReducerEvaluatorAttributesDto.class);
    }

    @Override
    public RespondingResult createInstance(String attributes) {
        try {
            SiembolResult validationResult = attributesSchema.validate(attributes);
            if (validationResult.getStatusCode() != SiembolResult.StatusCode.OK) {
                return RespondingResult.fromSiembolResult(validationResult);
            }
            ArrayReducerEvaluatorAttributesDto attributesDto = JSON_ATTRIBUTES_READER.readValue(attributes);
            ArrayReducerEvaluator evaluator = new ArrayReducerEvaluator.Builder()
                    .arrayFieldName(attributesDto.getArrayField())
                    .delimiter(attributesDto.getFieldNameDelimiter())
                    .prefixName(attributesDto.getPrefixName())
                    .reducerType(attributesDto.getArrayReducerType())
                    .patternFilter(attributesDto.getFieldFilter().getIncludingFields(),
                            attributesDto.getFieldFilter().getExcludingFields())
                    .build();
            return RespondingResult.fromEvaluator(evaluator);
        } catch (Exception e) {
            return RespondingResult.fromException(e);
        }
    }

    @Override
    public RespondingResult getType() {
        return RespondingResult.fromEvaluatorType(ProvidedEvaluators.ARRAY_REDUCER_EVALUATOR.toString());
    }

    @Override
    public RespondingResult getAttributesJsonSchema() {
        return RespondingResult.fromAttributesSchema(
                attributesSchema.getJsonSchema().getAttributes().getJsonSchema());
    }
}
