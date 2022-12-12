package uk.co.gresearch.siembol.response.evaluators.assignment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import uk.co.gresearch.siembol.common.jsonschema.SiembolJsonSchemaValidator;
import uk.co.gresearch.siembol.common.result.SiembolResult;
import uk.co.gresearch.siembol.response.common.ProvidedEvaluators;
import uk.co.gresearch.siembol.response.common.RespondingEvaluatorFactory;
import uk.co.gresearch.siembol.response.common.RespondingResult;
import uk.co.gresearch.siembol.response.model.AssignmentEvaluatorAttributesDto;
/**
 * An object for creating a json path assignment evaluator
 *
 * <p>This class implements RespondingEvaluatorFactory interface.
 * It is for creating a json path assignment evaluator and providing metadata such as a type and attributes schema.
 * The json path assignment evaluator evaluates a json path query and adds the result into the alert after
 * successful evaluation. Moreover, it provides the functionality for validating the evaluator attributes.
 *
 * @author  Marian Novotny
 * @see RespondingEvaluatorFactory
 * @see JsonPathAssignmentEvaluator
 */
public class JsonPathAssignmentEvaluatorFactory implements RespondingEvaluatorFactory {
    private static final ObjectReader JSON_ATTRIBUTES_READER = new ObjectMapper()
            .readerFor(AssignmentEvaluatorAttributesDto.class);
    private final SiembolJsonSchemaValidator attributesSchema;

    public JsonPathAssignmentEvaluatorFactory() throws Exception {
        attributesSchema = new SiembolJsonSchemaValidator(AssignmentEvaluatorAttributesDto.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RespondingResult createInstance(String attributes) {
        try {
            SiembolResult validationResult = attributesSchema.validate(attributes);
            if (validationResult.getStatusCode() != SiembolResult.StatusCode.OK) {
                return RespondingResult.fromSiembolResult(validationResult);
            }
            AssignmentEvaluatorAttributesDto attributesDto = JSON_ATTRIBUTES_READER.readValue(attributes);
            JsonPathAssignmentEvaluator evaluator = new JsonPathAssignmentEvaluator(attributesDto);
            return RespondingResult.fromEvaluator(evaluator);
        } catch (Exception e) {
            return RespondingResult.fromException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RespondingResult getType() {
        return RespondingResult.fromEvaluatorType(ProvidedEvaluators.JSON_PATH_ASSIGNMENT_EVALUATOR.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RespondingResult getAttributesJsonSchema() {
        return RespondingResult.fromAttributesSchema(
                attributesSchema.getJsonSchema().getAttributes().getJsonSchema());
    }
}
