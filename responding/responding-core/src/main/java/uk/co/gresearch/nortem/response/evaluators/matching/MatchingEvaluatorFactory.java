package uk.co.gresearch.nortem.response.evaluators.matching;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import uk.co.gresearch.nortem.common.jsonschema.NortemJsonSchemaValidator;
import uk.co.gresearch.nortem.common.result.NortemResult;
import uk.co.gresearch.nortem.response.common.ProvidedEvaluators;
import uk.co.gresearch.nortem.response.common.RespondingEvaluatorFactory;
import uk.co.gresearch.nortem.response.common.RespondingResult;
import uk.co.gresearch.nortem.response.model.MatchingEvaluatorAttributesDto;

public class MatchingEvaluatorFactory implements RespondingEvaluatorFactory {
    private static final ObjectReader JSON_ATTRIBUTES_READER = new ObjectMapper()
            .readerFor(MatchingEvaluatorAttributesDto.class);
    private final NortemJsonSchemaValidator attributesSchema;

    public MatchingEvaluatorFactory() throws Exception {
        attributesSchema = new NortemJsonSchemaValidator(MatchingEvaluatorAttributesDto.class);
    }

    @Override
    public RespondingResult createInstance(String attributes) {
        try {
            NortemResult validationResult = attributesSchema.validate(attributes);
            if (validationResult.getStatusCode() !=  NortemResult.StatusCode.OK) {
                return RespondingResult.fromNortemResult(validationResult);
            }
            MatchingEvaluatorAttributesDto attributesDto = JSON_ATTRIBUTES_READER.readValue(attributes);
            MatchingEvaluator evaluator = new MatchingEvaluator(attributesDto);
            return RespondingResult.fromEvaluator(evaluator);
        } catch (Exception e) {
            return RespondingResult.fromException(e);
        }
    }

    @Override
    public RespondingResult getType() {
        return RespondingResult.fromEvaluatorType(ProvidedEvaluators.MATCHING_EVALUATOR.toString());
    }

    @Override
    public RespondingResult getAttributesJsonSchema() {
        return RespondingResult.fromAttributesSchema(
                attributesSchema.getJsonSchema().getAttributes().getJsonSchema());
    }
}
