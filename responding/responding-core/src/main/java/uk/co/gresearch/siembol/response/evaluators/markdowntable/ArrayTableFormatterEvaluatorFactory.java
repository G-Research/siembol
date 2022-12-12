package uk.co.gresearch.siembol.response.evaluators.markdowntable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import uk.co.gresearch.siembol.common.jsonschema.SiembolJsonSchemaValidator;
import uk.co.gresearch.siembol.common.result.SiembolResult;
import uk.co.gresearch.siembol.response.common.*;
import uk.co.gresearch.siembol.response.model.ArrayTableFormatterEvaluatorAttributesDto;
/**
 * An object for creating an array table formatter evaluator
 *
 * <p>This class implements RespondingEvaluatorFactory interface.
 * It is for creating an array table formatter evaluator and providing metadata such as a type and attributes schema.
 * The table formatter evaluator generates a string with a Markdown table from the json array from the alert.
 * It writes the table into the alert to be used by the next evaluators of the rule.
 * Moreover, it provides the functionality for validating the evaluator attributes.
 *
 * @author  Marian Novotny
 * @see RespondingEvaluatorFactory
 * @see TableFormatter
 */
public class ArrayTableFormatterEvaluatorFactory implements RespondingEvaluatorFactory {
    private static final ObjectReader JSON_ATTRIBUTES_READER = new ObjectMapper()
            .readerFor(ArrayTableFormatterEvaluatorAttributesDto.class);
    private final SiembolJsonSchemaValidator attributesSchema;

    public ArrayTableFormatterEvaluatorFactory() throws Exception {
        attributesSchema = new SiembolJsonSchemaValidator(ArrayTableFormatterEvaluatorAttributesDto.class);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public RespondingResult getType() {
        return RespondingResult.fromEvaluatorType(ProvidedEvaluators.ARRAY_MARKDOWN_TABLE_FORMATTER_EVALUATOR.toString());
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
