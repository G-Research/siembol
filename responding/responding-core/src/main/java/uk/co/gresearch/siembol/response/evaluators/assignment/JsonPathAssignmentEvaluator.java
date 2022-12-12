package uk.co.gresearch.siembol.response.evaluators.assignment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import uk.co.gresearch.siembol.response.common.*;
import uk.co.gresearch.siembol.response.model.AssignmentEvaluatorAttributesDto;
import uk.co.gresearch.siembol.response.model.JsonPathAssignmentTypeDto;

import java.util.EnumSet;
import java.util.Set;
/**
 * An object for evaluating response alerts
 *
 * <p>This class implements Evaluable interface, and it is used in a response rule.
 * The json path assignment evaluator evaluates a json path query and adds the result into the alert after
 * successful evaluation.
 *
 * @author  Marian Novotny
 * @see Evaluable
 */
public class JsonPathAssignmentEvaluator implements Evaluable {
    private static final String ERROR_MESSAGE_FORMAT = "No json path:%s found in alert: %s";
    private static final ObjectWriter JSON_WRITER = new ObjectMapper()
            .writerFor(ResponseAlert.class);
    private final String fieldName;
    private final String jsonPath;
    private final JsonPathAssignmentTypeDto assignmentType;

    public JsonPathAssignmentEvaluator(AssignmentEvaluatorAttributesDto assignmentDto) {
        this.fieldName = assignmentDto.getFieldName();
        this.jsonPath = assignmentDto.getJsonPath();
        this.assignmentType = assignmentDto.getAssignmentType();

        Configuration.setDefaults(new Configuration.Defaults() {
            private final JsonProvider jsonProvider = new JacksonJsonNodeJsonProvider();
            private final MappingProvider mappingProvider = new JacksonMappingProvider();

            @Override
            public JsonProvider jsonProvider() {
                return jsonProvider;
            }

            @Override
            public MappingProvider mappingProvider() {
                return mappingProvider;
            }

            @Override
            public Set<Option> options() {
                return EnumSet.noneOf(Option.class);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RespondingResult evaluate(ResponseAlert alert) {
        JsonNode jsonActual;
        String alertJson = null;
        try {
            alertJson = JSON_WRITER.writeValueAsString(alert);
            jsonActual = JsonPath.parse(alertJson).read(jsonPath);
        } catch (com.jayway.jsonpath.PathNotFoundException e) {
            if (assignmentType == JsonPathAssignmentTypeDto.ERROR_MATCH_WHEN_EMPTY) {
                RespondingResultAttributes attributes = new RespondingResultAttributes();
                attributes.setMessage(String.format(ERROR_MESSAGE_FORMAT, jsonPath, alertJson));
                return new RespondingResult(RespondingResult.StatusCode.ERROR, attributes);
            }

            return RespondingResult.fromEvaluationResult(assignmentType == JsonPathAssignmentTypeDto.MATCH_ALWAYS
                    ? ResponseEvaluationResult.MATCH
                    : ResponseEvaluationResult.NO_MATCH, alert);
        } catch (Exception e) {
            return RespondingResult.fromException(e);
        }

        String actualValue = jsonActual.toString();
        if (jsonActual.isTextual() || jsonActual.isArray()) {
            //NOTE: for string/arrays we remove redundant quotes/brackets
            actualValue = actualValue.substring(1, jsonActual.toString().length() - 1);
        }
        alert.put(fieldName, actualValue);
        return RespondingResult.fromEvaluationResult(ResponseEvaluationResult.MATCH, alert);
    }
}
