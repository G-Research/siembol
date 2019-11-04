package uk.co.gresearch.nortem.configeditor.testcase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import uk.co.gresearch.nortem.common.jsonschema.JsonSchemaValidator;
import uk.co.gresearch.nortem.common.jsonschema.NortemJsonSchemaValidator;
import uk.co.gresearch.nortem.common.result.NortemResult;
import uk.co.gresearch.nortem.configeditor.model.ConfigEditorAssertionResult;
import uk.co.gresearch.nortem.configeditor.model.ConfigEditorAttributes;
import uk.co.gresearch.nortem.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.nortem.configeditor.model.ConfigEditorTestCaseResult;
import uk.co.gresearch.nortem.configeditor.testcase.model.AssertionTypeDto;
import uk.co.gresearch.nortem.configeditor.testcase.model.TestAssertionDto;
import uk.co.gresearch.nortem.configeditor.testcase.model.TestCaseDto;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static uk.co.gresearch.nortem.configeditor.model.ConfigEditorResult.StatusCode.ERROR;
import static uk.co.gresearch.nortem.configeditor.model.ConfigEditorResult.StatusCode.OK;

public class TestCaseEvaluatorImpl implements TestCaseEvaluator {
    private static final ObjectReader TEST_CASE_READER =
            new ObjectMapper().readerFor(TestCaseDto.class);
    private static final String EMPTY_VALIDATION_JSON = "{}";
    private final JsonSchemaValidator jsonSchemaValidator;

    public TestCaseEvaluatorImpl() throws Exception {
        this.jsonSchemaValidator =  new NortemJsonSchemaValidator(TestCaseDto.class);
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

    private ConfigEditorAssertionResult evaluateResult(ReadContext context, TestAssertionDto assertion) {
        ConfigEditorAssertionResult result = new ConfigEditorAssertionResult();
        result.setExpectedPattern(assertion.getExpectedPattern());
        result.setAssertionType(assertion.getAssertionType().toString());
        result.setNegated(assertion.getNegatedPattern());

        Pattern pattern = Pattern.compile(assertion.getExpectedPattern());
        JsonNode jsonActual;
        try {
            jsonActual = context.read(assertion.getJsonPath());
        } catch (com.jayway.jsonpath.PathNotFoundException e) {
            result.setMatch(assertion.getAssertionType() == AssertionTypeDto.ONLY_IF_PATH_EXISTS);
            return result;
        }

        String actualValue = jsonActual.isTextual() ? jsonActual.asText() : jsonActual.toString();
        result.setActualValue(actualValue);
        boolean patternMatch = pattern.matcher(actualValue).matches();
        result.setMatch(assertion.getNegatedPattern() ? !patternMatch : patternMatch);

        return result;
    }

    @Override
    public ConfigEditorResult evaluate(String jsonResult, String testCaseJson) {
        ConfigEditorTestCaseResult testCaseResult = new ConfigEditorTestCaseResult();

        try {
            final ReadContext context = JsonPath.parse(jsonResult);
            TestCaseDto testCase = TEST_CASE_READER.readValue(testCaseJson);
            List<ConfigEditorAssertionResult> assertionResults = testCase.getAssertions()
                    .stream()
                    .filter(x -> x.getActive())
                    .map(x -> evaluateResult(context, x))
                    .collect(Collectors.toList());

            int numMatches = 0, numFailures = 0;
            for (ConfigEditorAssertionResult evaluationResult :  assertionResults) {
                if (evaluationResult.getMatch()) {
                    numMatches++;
                } else {
                    numFailures++;
                }
            }

            testCaseResult.setSkippedAssertions(testCase.getAssertions().size() - assertionResults.size());
            testCaseResult.setMatchedAssertions(numMatches);
            testCaseResult.setFailedAssertions(numFailures);
            testCaseResult.setAssertionResults(assertionResults);
        } catch (Exception e) {
            return ConfigEditorResult.fromException(e);
        }

        ConfigEditorAttributes attributes = new ConfigEditorAttributes();
        attributes.setTestCaseResult(testCaseResult);
        return new ConfigEditorResult(OK, attributes);
    }

    @Override
    public ConfigEditorResult validate(String testCase) {
        NortemResult validationResult = jsonSchemaValidator.validate(testCase);
        if (validationResult.getStatusCode() != NortemResult.StatusCode.OK) {
            return ConfigEditorResult.fromMessage(ERROR, validationResult.getAttributes().getMessage());
        }

        ConfigEditorResult emptyJsonEvaluation = evaluate(EMPTY_VALIDATION_JSON, testCase);
        if (emptyJsonEvaluation.getStatusCode() != OK) {
            return ConfigEditorResult.fromMessage(ERROR, emptyJsonEvaluation.getAttributes().getException());
        }
        return new ConfigEditorResult(OK, new ConfigEditorAttributes());
    }

    @Override
    public ConfigEditorResult getSchema() {
        return ConfigEditorResult.fromSchema(jsonSchemaValidator.getJsonSchema().getAttributes().getJsonSchema());
    }

}
