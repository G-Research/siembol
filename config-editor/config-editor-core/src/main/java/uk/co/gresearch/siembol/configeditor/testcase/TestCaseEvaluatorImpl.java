package uk.co.gresearch.siembol.configeditor.testcase;

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
import uk.co.gresearch.siembol.common.jsonschema.JsonSchemaValidator;
import uk.co.gresearch.siembol.common.jsonschema.SiembolJsonSchemaValidator;
import uk.co.gresearch.siembol.common.result.SiembolResult;
import uk.co.gresearch.siembol.configeditor.common.ConfigEditorUtils;
import uk.co.gresearch.siembol.configeditor.model.*;
import uk.co.gresearch.siembol.configeditor.testcase.model.AssertionTypeDto;
import uk.co.gresearch.siembol.configeditor.testcase.model.TestAssertionDto;
import uk.co.gresearch.siembol.configeditor.testcase.model.TestCaseDto;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.ERROR;
import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.OK;

public class TestCaseEvaluatorImpl implements TestCaseEvaluator {
    private static final ObjectReader TEST_CASE_READER =
            new ObjectMapper().readerFor(TestCaseDto.class);
    private static final String EMPTY_VALIDATION_JSON = "{}";
    private static final String EMPTY_PATCHED_UI_SCHEMA = "Error during computing patched test case schema";
    private final JsonSchemaValidator jsonSchemaValidator;
    private final String testCaseSchema;

    public TestCaseEvaluatorImpl(ConfigEditorUiLayout uiLayout) throws Exception {
        this.jsonSchemaValidator = new SiembolJsonSchemaValidator(TestCaseDto.class);
        String schemaStr = jsonSchemaValidator.getJsonSchema().getAttributes().getJsonSchema();
        Optional<String> patchedSchema = ConfigEditorUtils.patchJsonSchema(schemaStr, uiLayout.getTestCaseLayout());
        if (!patchedSchema.isPresent()) {
            throw new IllegalArgumentException(EMPTY_PATCHED_UI_SCHEMA);
        }
        testCaseSchema = patchedSchema.get();

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
        SiembolResult validationResult = jsonSchemaValidator.validate(testCase);
        if (validationResult.getStatusCode() != SiembolResult.StatusCode.OK) {
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
        return ConfigEditorResult.fromSchema(testCaseSchema);
    }

}
