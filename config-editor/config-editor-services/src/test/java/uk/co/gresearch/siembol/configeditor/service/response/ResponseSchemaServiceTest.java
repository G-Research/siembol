package uk.co.gresearch.siembol.configeditor.service.response;

import org.adrianwalker.multilinestring.Multiline;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;

import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.BAD_REQUEST;
import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.OK;

public class ResponseSchemaServiceTest {
    /**
     * {
     *   "rules_version": 1,
     *   "rules": [
     *     {
     *       "rule_name": "test_rule",
     *       "rule_version": 1,
     *       "rule_author": "john",
     *       "rule_description": "Test rule",
     *       "evaluators": [
     *         {
     *           "evaluator_type": "fixed_evaluator",
     *           "evaluator_attributes": {
     *             "evaluation_result": "match"
     *           }
     *         },
     *         {
     *           "evaluator_type": "assignment_evaluator",
     *           "evaluator_attributes": {
     *             "assignment_type": "match_always",
     *             "field_name": "test_field",
     *             "json_path": "$..a"
     *           }
     *         }
     *       ]
     *     }
     *   ]
     * }
     */
    @Multiline
    public static String testingRules;

    /**
     * {
     *   "rule_name": "test_rule",
     *   "rule_version": 1,
     *   "rule_author": "john",
     *   "rule_description": "Test rule",
     *   "evaluators": [
     *     {
     *       "evaluator_type": "fixed_evaluator",
     *       "evaluator_attributes": {
     *         "evaluation_result": "match"
     *       }
     *     },
     *     {
     *       "evaluator_type": "assignment_evaluator",
     *       "evaluator_attributes": {
     *         "assignment_type": "match_always",
     *         "field_name": "test_field",
     *         "json_path": "$..a"
     *       }
     *     }
     *   ]
     * }
     */
    @Multiline
    public static String testingRule;

    ResponseSchemaService.Builder builder;
    ResponseSchemaService responseSchemaService;

    @Before
    public void setup() throws Exception {
        builder = new ResponseSchemaService.Builder();
    }

    @Test
    public void getSchemaNoUiConfig() throws Exception {
        responseSchemaService = builder.build();
        ConfigEditorResult result = responseSchemaService.getSchema();
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getRulesSchema());
    }

    @Test
    public void getSchemaWithValidUiConfig() throws Exception {
        responseSchemaService = builder
                .uiConfigSchema("{\"layout\": {}}")
                .build();
        ConfigEditorResult result = responseSchemaService.getSchema();
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getRulesSchema());
    }

    @Test(expected = com.fasterxml.jackson.core.JsonParseException.class)
    public void getSchemaWithInValidUiConfig() throws Exception {
        responseSchemaService = builder
                .uiConfigSchema("INVALID")
                .build();
    }

    @Test
    public void validateRulesOk() throws Exception {
        responseSchemaService = builder.build();
        ConfigEditorResult result = responseSchemaService.validateConfigurations(testingRules);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
    }

    @Test
    public void validateRulesMissingEvaluator() throws Exception {
        responseSchemaService = builder.build();
        ConfigEditorResult result = responseSchemaService.validateConfigurations(
                testingRules.replace("\"fixed_evaluator\"", "\"unsupported_evaluator\""));
        Assert.assertEquals(BAD_REQUEST, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }

    @Test
    public void validateRulesMissingRequiredProperties() throws Exception {
        responseSchemaService = builder.build();
        ConfigEditorResult result = responseSchemaService.validateConfigurations(
                testingRules.replace("\"evaluation_result\"", "\"unsupported_field\""));
        Assert.assertEquals(BAD_REQUEST, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }

    @Test
    public void validateRulesInvalidJson() throws Exception {
        responseSchemaService = builder.build();
        ConfigEditorResult result = responseSchemaService.validateConfigurations("INVALID");
        Assert.assertEquals(BAD_REQUEST, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }

    @Test
    public void validateRuleOk() throws Exception {
        responseSchemaService = builder.build();
        ConfigEditorResult result = responseSchemaService.validateConfiguration(testingRule);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
    }

    @Test
    public void validateRuleMissingEvaluator() throws Exception {
        responseSchemaService = builder.build();
        ConfigEditorResult result = responseSchemaService.validateConfiguration(
                testingRules.replace("\"fixed_evaluator\"", "\"unsupported_evaluator\""));
        Assert.assertEquals(BAD_REQUEST, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }

    @Test
    public void validateRuleMissingRequiredProperties() throws Exception {
        responseSchemaService = builder.build();
        ConfigEditorResult result = responseSchemaService.validateConfiguration(
                testingRules.replace("\"evaluation_result\"", "\"unsupported_field\""));
        Assert.assertEquals(BAD_REQUEST, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }

    @Test
    public void validateRuleInvalidJson() throws Exception {
        responseSchemaService = builder.build();
        ConfigEditorResult result = responseSchemaService.validateConfiguration("INVALID");
        Assert.assertEquals(BAD_REQUEST, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }
}
