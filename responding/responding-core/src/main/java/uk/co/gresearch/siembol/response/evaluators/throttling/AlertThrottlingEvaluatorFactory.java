package uk.co.gresearch.siembol.response.evaluators.throttling;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import uk.co.gresearch.siembol.common.jsonschema.SiembolJsonSchemaValidator;
import uk.co.gresearch.siembol.common.result.SiembolResult;
import uk.co.gresearch.siembol.response.common.ProvidedEvaluators;
import uk.co.gresearch.siembol.response.common.RespondingEvaluatorFactory;
import uk.co.gresearch.siembol.response.common.RespondingResult;
import uk.co.gresearch.siembol.response.model.AlertThrottlingEvaluatorAttributesDto;

public class AlertThrottlingEvaluatorFactory implements RespondingEvaluatorFactory {
    private static final ObjectReader JSON_ATTRIBUTES_READER = new ObjectMapper()
            .readerFor(AlertThrottlingEvaluatorAttributesDto.class);
    private final SiembolJsonSchemaValidator attributesSchema;

    public AlertThrottlingEvaluatorFactory() throws Exception {
        attributesSchema = new SiembolJsonSchemaValidator(AlertThrottlingEvaluatorAttributesDto.class);
    }

    @Override
    public RespondingResult createInstance(String attributes) {
        try {
            SiembolResult validationResult = attributesSchema.validate(attributes);
            if (validationResult.getStatusCode() != SiembolResult.StatusCode.OK) {
                return RespondingResult.fromSiembolResult(validationResult);
            }
            AlertThrottlingEvaluatorAttributesDto attributesDto = JSON_ATTRIBUTES_READER.readValue(attributes);
            AlertThrottlingEvaluator evaluator = new AlertThrottlingEvaluator(attributesDto);
            return RespondingResult.fromEvaluator(evaluator);
        } catch (Exception e) {
            return RespondingResult.fromException(e);
        }
    }

    @Override
    public RespondingResult getType() {
        return RespondingResult.fromEvaluatorType(ProvidedEvaluators.ALERT_THROTTLING_EVALUATOR.toString());
    }

    @Override
    public RespondingResult getAttributesJsonSchema() {
        return RespondingResult.fromAttributesSchema(
                attributesSchema.getJsonSchema().getAttributes().getJsonSchema());
    }
}
