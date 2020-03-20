package uk.co.gresearch.nortem.response.evaluators.assignment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import uk.co.gresearch.nortem.common.jsonschema.NortemJsonSchemaValidator;
import uk.co.gresearch.nortem.common.result.NortemResult;
import uk.co.gresearch.nortem.response.common.ProvidedEvaluators;
import uk.co.gresearch.nortem.response.common.RespondingEvaluatorFactory;
import uk.co.gresearch.nortem.response.common.RespondingResult;
import uk.co.gresearch.nortem.response.model.AssignmentEvaluatorAttributesDto;


public class AssignmentEvaluatorFactory implements RespondingEvaluatorFactory {
    private static final ObjectReader JSON_ATTRIBUTES_READER = new ObjectMapper()
            .readerFor(AssignmentEvaluatorAttributesDto.class);
    private final NortemJsonSchemaValidator attributesSchema;

    public  AssignmentEvaluatorFactory() throws Exception {
        attributesSchema = new NortemJsonSchemaValidator(AssignmentEvaluatorAttributesDto.class);
    }

    @Override
    public RespondingResult createInstance(String attributes) {
        try {
            NortemResult validationResult = attributesSchema.validate(attributes);
            if (validationResult.getStatusCode() != NortemResult.StatusCode.OK) {
                return RespondingResult.fromNortemResult(validationResult);
            }
            AssignmentEvaluatorAttributesDto attributesDto = JSON_ATTRIBUTES_READER.readValue(attributes);
            AssignmentEvaluator evaluator = new AssignmentEvaluator(attributesDto);
            return RespondingResult.fromEvaluator(evaluator);
        } catch (Exception e) {
            return RespondingResult.fromException(e);
        }
    }

    @Override
    public RespondingResult getType() {
        return RespondingResult.fromEvaluatorType(ProvidedEvaluators.ASSIGNMENT_EVALUATOR.toString());
    }

    @Override
    public RespondingResult getAttributesJsonSchema() {
        return RespondingResult.fromAttributesSchema(
                attributesSchema.getJsonSchema().getAttributes().getJsonSchema());
    }
}
