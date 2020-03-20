package uk.co.gresearch.nortem.response.evaluators.fixed;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import uk.co.gresearch.nortem.common.jsonschema.NortemJsonSchemaValidator;
import uk.co.gresearch.nortem.common.result.NortemResult;
import uk.co.gresearch.nortem.response.common.ProvidedEvaluators;
import uk.co.gresearch.nortem.response.common.RespondingEvaluatorFactory;
import uk.co.gresearch.nortem.response.common.RespondingResult;
import uk.co.gresearch.nortem.response.model.FixedEvaluatorAttributesDto;

public class FixedEvaluatorFactory implements RespondingEvaluatorFactory {
    private static final ObjectReader JSON_ATTRIBUTES_READER = new ObjectMapper()
            .readerFor(FixedEvaluatorAttributesDto.class);
    private final NortemJsonSchemaValidator attributesSchema;

    public FixedEvaluatorFactory() throws Exception {
        attributesSchema = new NortemJsonSchemaValidator(FixedEvaluatorAttributesDto.class);
    }

    @Override
    public RespondingResult createInstance(String attributes) {
        try {
            NortemResult validationResult = attributesSchema.validate(attributes);
            if (validationResult.getStatusCode() !=  NortemResult.StatusCode.OK) {
                return RespondingResult.fromNortemResult(validationResult);
            }
            FixedEvaluatorAttributesDto attributesDto = JSON_ATTRIBUTES_READER.readValue(attributes);
            FixedEvaluator evaluator = new FixedEvaluator(attributesDto.getResponseEvaluationResult());
            return RespondingResult.fromEvaluator(evaluator);
        } catch (Exception e) {
            return RespondingResult.fromException(e);
        }
    }

    @Override
    public RespondingResult getType() {
        return RespondingResult.fromEvaluatorType(ProvidedEvaluators.FIXED_EVALUATOR.toString());
    }

    @Override
    public RespondingResult getAttributesJsonSchema() {
        return RespondingResult.fromAttributesSchema(
                attributesSchema.getJsonSchema().getAttributes().getJsonSchema());
    }
}
