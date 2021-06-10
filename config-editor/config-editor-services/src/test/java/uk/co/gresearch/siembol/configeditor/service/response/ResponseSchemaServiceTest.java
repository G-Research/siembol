package uk.co.gresearch.siembol.configeditor.service.response;

import org.adrianwalker.multilinestring.Multiline;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.gresearch.siembol.common.utils.HttpProvider;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorUiLayout;
import uk.co.gresearch.siembol.response.common.ResponseApplicationPaths;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.*;
import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.BAD_REQUEST;
import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.ERROR;
import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.OK;

public class ResponseSchemaServiceTest {
    /**
     * {
     *   "statusCode": "OK",
     *   "attributes": {
     *     "rules_schema": {
     *       "type": "object",
     *       "description": "Incident Response Rules",
     *       "title": "rules",
     *       "properties": {
     *         "rules_version": {
     *           "type": "integer",
     *           "description": "Incident response rules version",
     *           "default": 0
     *         },
     *         "rules": {
     *           "type": "array",
     *           "items": {
     *             "type": "object",
     *             "description": "Response rule that should handle response to a siembol alert",
     *             "title": "rule",
     *             "properties": {
     *               "rule_name": {
     *                 "type": "string",
     *                 "description": "ResponseRule name that uniquely identifies the rule"
     *               },
     *               "rule_author": {
     *                 "type": "string",
     *                 "description": "The owner of the rule"
     *               },
     *               "rule_version": {
     *                 "type": "integer",
     *                 "description": "The version of the rule",
     *                 "default": 0
     *               },
     *               "rule_description": {
     *                 "type": "string",
     *                 "description": "The description of the rule"
     *               },
     *               "evaluators": {
     *                 "type": "array",
     *                 "items": {
     *                   "type": "object",
     *                   "description": "Response evaluator used in response rules",
     *                   "title": "response evaluator",
     *                   "oneOf": [
     *                     {
     *                       "type": "object",
     *                       "title": "matching_evaluator",
     *                       "properties": {
     *                         "evaluator_type": {
     *                           "enum": [
     *                             "matching_evaluator"
     *                           ],
     *                           "default": "matching_evaluator"
     *                         },
     *                         "evaluator_attributes": {
     *                           "type": "object",
     *                           "description": "Attributes for matching evaluator",
     *                           "title": "matching evaluator attributes",
     *                           "properties": {
     *                             "evaluation_result": {
     *                               "enum": [
     *                                 "match",
     *                                 "filtered"
     *                               ],
     *                               "type": "string",
     *                               "description": "Evaluation result returned by the evaluator after matching",
     *                               "default": "match"
     *                             },
     *                             "matchers": {
     *                               "type": "array",
     *                               "items": {
     *                                 "type": "object",
     *                                 "description": "Matcher for matching fields in response rules",
     *                                 "title": "matcher",
     *                                 "properties": {
     *                                   "matcher_type": {
     *                                     "enum": [
     *                                       "REGEX_MATCH",
     *                                       "IS_IN_SET"
     *                                     ],
     *                                     "type": "string",
     *                                     "description": "Type of matcher, either Regex match or list of strings (newline delimited)"
     *                                   },
     *                                   "is_negated": {
     *                                     "type": "boolean",
     *                                     "description": "The matcher is negated",
     *                                     "default": false
     *                                   },
     *                                   "field": {
     *                                     "type": "string",
     *                                     "description": "Field on which the matcher will be evaluated"
     *                                   },
     *                                   "case_insensitive": {
     *                                     "type": "boolean",
     *                                     "description": "Use case insensitive string compare",
     *                                     "default": false
     *                                   },
     *                                   "data": {
     *                                     "type": "string",
     *                                     "description": "Matcher expression as defined by matcher type"
     *                                   }
     *                                 },
     *                                 "required": [
     *                                   "data",
     *                                   "field",
     *                                   "matcher_type"
     *                                 ]
     *                               },
     *                               "description": "Matchers of the evaluator",
     *                               "minItems": 1
     *                             }
     *                           },
     *                           "required": [
     *                             "evaluation_result",
     *                             "matchers"
     *                           ]
     *                         }
     *                       },
     *                       "required": [
     *                         "evaluator_type",
     *                         "evaluator_attributes"
     *                       ]
     *                     }
     *                   ]
     *                 },
     *                 "description": "Evaluators of the rule",
     *                 "minItems": 1
     *               }
     *             },
     *             "required": [
     *               "evaluators",
     *               "rule_author",
     *               "rule_name",
     *               "rule_version"
     *             ]
     *           },
     *           "description": "Response rules",
     *           "minItems": 1
     *         }
     *       },
     *       "required": [
     *         "rules",
     *         "rules_version"
     *       ]
     *     }
     *   }
     * }
     */
    @Multiline
    public static String rulesSchema;

    /**
     * {"statusCode":"OK","attributes":{"test_schema":{  "type" : "object",  "description" : "Specification for testing responding rules",  "title" : "response test specification",  "properties" : {    "event" : {      "type" : "object",      "description" : "Alert for response alerts evaluation",      "title" : "json raw string"    }  },  "required" : [ "event" ]}}}
     */
    @Multiline
    public static String testSchema;

    /**
     * {
     *   "statusCode":"ERROR",
     *   "attributes":{ "message" : "dummy"}}
     * }
     */
    @Multiline
    public static String errorMessage;

    /**
     * {
     *   "statusCode":"OK",
     *   "attributes":{ "message" : "dummy"}}
     * }
     */
    @Multiline
    public static String okMessage;

    private ResponseSchemaService.Builder builder;
    private ResponseSchemaService responseSchemaService;
    private HttpProvider httpProvider;
    private String dummyJsonObject = "{ \"dummy\" : true }";
    private String dummyJsonObject2 = "{ \"dummy2\" : true }";

    @Before
    public void setup() throws Exception {
        httpProvider = Mockito.mock(HttpProvider.class);
        Mockito.when(httpProvider.get(eq(ResponseApplicationPaths.GET_SCHEMA.toString())))
                .thenReturn(rulesSchema);
        Mockito.when(httpProvider.get(eq(ResponseApplicationPaths.GET_TEST_SCHEMA.toString())))
                .thenReturn(testSchema);
        Mockito.when(httpProvider.post(eq(ResponseApplicationPaths.VALIDATE_RULES.toString()), anyString()))
                .thenReturn(okMessage);
        Mockito.when(httpProvider.post(eq(ResponseApplicationPaths.TEST_RULES.toString()), anyString()))
                .thenReturn(okMessage);

        builder = new ResponseSchemaService.Builder(httpProvider);
    }

    @Test
    public void buildNoUiConfigOK() throws Exception {
        responseSchemaService = builder.build();
        ConfigEditorResult result = responseSchemaService.getSchema();
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getRulesSchema());
        ConfigEditorResult resultTestSchema = responseSchemaService.getTestSchema();
        Assert.assertEquals(OK, resultTestSchema.getStatusCode());
        Assert.assertNotNull(resultTestSchema.getAttributes());
        Assert.assertNotNull(resultTestSchema.getAttributes().getTestSchema());
    }

    @Test
    public void buildUiConfigOK() throws Exception {
        responseSchemaService = builder
                .uiConfigSchema(new ConfigEditorUiLayout())
                .build();

        ConfigEditorResult result = responseSchemaService.getSchema();
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getRulesSchema());
        ConfigEditorResult resultTestSchema = responseSchemaService.getTestSchema();
        Assert.assertEquals(OK, resultTestSchema.getStatusCode());
        Assert.assertNotNull(resultTestSchema.getAttributes());
        Assert.assertNotNull(resultTestSchema.getAttributes().getTestSchema());
    }

    @Test(expected = java.lang.IllegalStateException.class)
    public void buildErrorGetSchema() throws Exception {
        Mockito.when(httpProvider.get(eq(ResponseApplicationPaths.GET_SCHEMA.toString())))
                .thenReturn("errorMessage");
        responseSchemaService = builder.build();
    }

    @Test(expected = java.lang.IllegalStateException.class)
    public void buildErrorGetTestSchema() throws Exception {
        Mockito.when(httpProvider.get(eq(ResponseApplicationPaths.GET_TEST_SCHEMA.toString())))
                .thenReturn("errorMessage");
        responseSchemaService = builder.build();
    }

    @Test
    public void validateRulesOk() throws Exception {
        responseSchemaService = builder.build();
        ConfigEditorResult result = responseSchemaService.validateConfigurations(dummyJsonObject);
        Assert.assertEquals(OK, result.getStatusCode());
    }

    @Test
    public void validateRulesError() throws Exception {
        Mockito.when(httpProvider.post(eq(ResponseApplicationPaths.VALIDATE_RULES.toString()), anyString()))
                .thenReturn(errorMessage);
        responseSchemaService = builder.build();
        ConfigEditorResult result = responseSchemaService.validateConfigurations(dummyJsonObject);
        Assert.assertEquals(BAD_REQUEST, result.getStatusCode());
    }

    @Test
    public void validateRulesException() throws Exception {
        Mockito.when(httpProvider.post(eq(ResponseApplicationPaths.VALIDATE_RULES.toString()), anyString()))
                .thenThrow(new IOException());
        responseSchemaService = builder.build();
        ConfigEditorResult result = responseSchemaService.validateConfigurations(dummyJsonObject);
        Assert.assertEquals(ERROR, result.getStatusCode());
    }

    @Test
    public void validateRuleOk() throws Exception {
        responseSchemaService = builder.build();
        ConfigEditorResult result = responseSchemaService.validateConfiguration(dummyJsonObject);
        Assert.assertEquals(OK, result.getStatusCode());
    }

    @Test
    public void validateRuleError() throws Exception {
        Mockito.when(httpProvider.post(eq(ResponseApplicationPaths.VALIDATE_RULES.toString()), anyString()))
                .thenReturn(errorMessage);
        responseSchemaService = builder.build();
        ConfigEditorResult result = responseSchemaService.validateConfiguration(dummyJsonObject);
        Assert.assertEquals(BAD_REQUEST, result.getStatusCode());
    }

    @Test
    public void testRulesOk() throws Exception {
        responseSchemaService = builder.build();
        ConfigEditorResult result = responseSchemaService.testConfigurations(dummyJsonObject, dummyJsonObject2);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getTestResultOutput());
        Assert.assertNotNull(result.getAttributes().getTestResultRawOutput());
        Assert.assertEquals("dummy", result.getAttributes().getTestResultOutput());
        Assert.assertTrue(result.getAttributes().getTestResultComplete());
    }

    @Test
    public void testRuleOk() throws Exception {
        responseSchemaService = builder.build();
        ConfigEditorResult result = responseSchemaService.testConfiguration(dummyJsonObject, dummyJsonObject2);
        Assert.assertEquals(OK, result.getStatusCode());
    }

    @Test
    public void testRulesError() throws Exception {
        Mockito.when(httpProvider.post(eq(ResponseApplicationPaths.TEST_RULES.toString()), any()))
                .thenReturn(errorMessage);
        responseSchemaService = builder.build();
        ConfigEditorResult result = responseSchemaService.testConfigurations(dummyJsonObject, dummyJsonObject2);
        Assert.assertEquals(BAD_REQUEST, result.getStatusCode());
    }

    @Test
    public void testRulesException() throws Exception {
        Mockito.when(httpProvider.post(eq(ResponseApplicationPaths.TEST_RULES.toString()), any()))
                .thenThrow(new IOException());
        responseSchemaService = builder.build();
        ConfigEditorResult result = responseSchemaService.testConfigurations(dummyJsonObject, dummyJsonObject2);
        Assert.assertEquals(ERROR, result.getStatusCode());
    }

    @Test
    public void testRuleError() throws Exception {
        Mockito.when(httpProvider.post(eq(ResponseApplicationPaths.TEST_RULES.toString()), any()))
                .thenReturn(errorMessage);
        responseSchemaService = builder.build();
        ConfigEditorResult result = responseSchemaService.testConfiguration(dummyJsonObject, dummyJsonObject2);
        Assert.assertEquals(BAD_REQUEST, result.getStatusCode());
    }

    @Test
    public void getAdminConfigSchemaError() throws Exception {
        responseSchemaService = builder.build();
        ConfigEditorResult ret = responseSchemaService.getAdminConfigurationSchema();
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, ret.getStatusCode());
    }

    @Test
    public void validateAdminConfigError() throws Exception {
        responseSchemaService = builder.build();
        ConfigEditorResult ret = responseSchemaService.getAdminConfigurationSchema();
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, ret.getStatusCode());
    }

    @Test
    public void getImportersEmpty() throws Exception {
        responseSchemaService = builder.build();
        ConfigEditorResult ret = responseSchemaService.getImporters();
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertNotNull(ret.getAttributes().getConfigImporters());
        Assert.assertTrue(ret.getAttributes().getConfigImporters().isEmpty());
    }
}
