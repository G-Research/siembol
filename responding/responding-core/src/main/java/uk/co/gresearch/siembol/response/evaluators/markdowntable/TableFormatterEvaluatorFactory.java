package uk.co.gresearch.siembol.response.evaluators.markdowntable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import uk.co.gresearch.siembol.common.jsonschema.SiembolJsonSchemaValidator;
import uk.co.gresearch.siembol.common.result.SiembolResult;
import uk.co.gresearch.siembol.response.common.ProvidedEvaluators;
import uk.co.gresearch.siembol.response.common.RespondingEvaluatorFactory;
import uk.co.gresearch.siembol.response.common.RespondingResult;
import uk.co.gresearch.siembol.response.model.TableFormatterEvaluatorAttributesDto;
/**
 * An object for creating a table formatter evaluator
 *
 * <p>This class implements RespondingEvaluatorFactory interface.
 * It is for creating a table formatter evaluator and providing metadata such as a type and attributes schema.
 * The table formatter evaluator generates a string with a Markdown table from the json object from the alert.
 * It writes the table into the alert to be used by next evaluators of the rule.
 * Moreover, it provides the functionality for validating the evaluator attributes.
 *
 * @author  Marian Novotny
 * @see RespondingEvaluatorFactory
 * @see TableFormatter
 */
public class TableFormatterEvaluatorFactory implements RespondingEvaluatorFactory {
    private static final ObjectReader JSON_ATTRIBUTES_READER = new ObjectMapper()
            .readerFor(TableFormatterEvaluatorAttributesDto.class);
    private final SiembolJsonSchemaValidator attributesSchema;

    public TableFormatterEvaluatorFactory() throws Exception {
        attributesSchema = new SiembolJsonSchemaValidator(TableFormatterEvaluatorAttributesDto.class);
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
            TableFormatterEvaluatorAttributesDto attributesDto = JSON_ATTRIBUTES_READER.readValue(attributes);
            TableFormatter.Builder builder = new TableFormatter.Builder()
                    .fieldName(attributesDto.getFieldName())
                    .tableName(attributesDto.getTableName())
                    .columnNames(attributesDto.getFieldsColumnName(), attributesDto.getValuesColumnName());

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
        return RespondingResult.fromEvaluatorType(ProvidedEvaluators.MARKDOWN_TABLE_FORMATTER_EVALUATOR.toString());
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
