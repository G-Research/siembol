package uk.co.gresearch.siembol.response.compiler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.gresearch.siembol.common.metrics.test.SiembolMetricsTestRegistrar;
import uk.co.gresearch.siembol.response.common.*;
import uk.co.gresearch.siembol.response.evaluators.fixed.FixedResultEvaluator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.co.gresearch.siembol.response.common.RespondingResult.StatusCode.OK;

public class RespondingCompilerImplTest {
    private final String evaluatorAttributes = """
            {
              "type" : "object",
              "description" : "Attributes for fixed evaluator",
              "title" : "fixed evaluator attributes",
              "properties" : {
                "evaluation_result" : {
                  "enum" : [ "match", "no_match", "filtered" ],
                  "type" : "string",
                  "description" : "Evaluation result returned by the evaluator"
                }
              },
              "required" : [ "evaluation_result" ]
            }
            """;

    private final String evaluatorNextAttributes = """
            {
              "type" : "object",
              "description" : "json path assignment",
              "title" : "json path assignment",
              "properties" : {
                "assignment_type" : {
                  "enum" : [ "match_always", "no_match_when_empty", "error_match_when_empty" ],
                  "type" : "string",
                  "description" : "The type of the assignment based on json path evaluation"
                },
                "field_name" : {
                  "type" : "string",
                  "description" : "The name of the field in which the non empty result of the json path will be stored"
                },
                "json_path" : {
                  "type" : "string",
                  "description" : "Json path ",
                  "minItems" : 1
                }
              },
              "required" : [ "assignment_type", "field_name", "json_path" ]
            }
            """;


    private final String testingRules = """
            {
              "rules_version": 1,
              "rules": [
                {
                  "rule_name": "test_rule",
                  "rule_version": 1,
                  "rule_author": "john",
                  "rule_description": "Test rule",
                  "evaluators": [
                    {
                      "evaluator_type": "b_first_evaluator",
                      "evaluator_attributes": {
                        "evaluation_result": "match"
                      }
                    },
                    {
                      "evaluator_type": "a_second_evaluator",
                      "evaluator_attributes": {
                        "assignment_type": "match_always",
                        "field_name": "test_field",
                        "json_path": "$..a"
                      }
                    }
                  ]
                }
              ]
            }
            """;

    private final String testingRule = """
            {
              "rule_name": "test_rule",
              "rule_version": 1,
              "rule_author": "john",
              "rule_description": "Test rule",
              "evaluators": [
                {
                  "evaluator_type": "b_first_evaluator",
                  "evaluator_attributes": {
                    "evaluation_result": "match"
                  }
                },
                {
                  "evaluator_type": "a_second_evaluator",
                  "evaluator_attributes": {
                    "assignment_type": "match_always",
                    "field_name": "test_field",
                    "json_path": "$..a"
                  }
                }
              ]
            }
            """;

    private final String testSpecification = """
            {
              "event": {
                "is_test": true
              }
            }
            """;

    private RespondingCompilerImpl compiler;
    private RespondingCompilerImpl.Builder builder;
    private SiembolMetricsTestRegistrar metricsTestRegistrar;

    private RespondingEvaluatorFactory evaluatorFactory;
    private RespondingResult evaluatorSchemaResult;
    private RespondingResult evaluatorTypeResult;
    private RespondingResult evaluatorNextSchemaResult;
    private RespondingResult evaluatorNextTypeResult;
    private RespondingResult evaluatorResult;
    private Evaluable evaluator;

    private RespondingEvaluatorFactory evaluatorFactoryNext;

    @Before
    public void setUp() throws Exception {
        metricsTestRegistrar = new SiembolMetricsTestRegistrar();
        builder = new RespondingCompilerImpl.Builder()
                .metricsRegistrar(metricsTestRegistrar);
        evaluatorTypeResult = RespondingResult.fromEvaluatorType("b_first_evaluator");
        evaluatorNextTypeResult = RespondingResult.fromEvaluatorType("a_second_evaluator");

        evaluatorSchemaResult = RespondingResult.fromAttributesSchema(evaluatorAttributes);
        evaluatorNextSchemaResult = RespondingResult.fromAttributesSchema(evaluatorNextAttributes);

        evaluator = new FixedResultEvaluator(ResponseEvaluationResult.MATCH);

        RespondingResultAttributes attributes = new RespondingResultAttributes();
        attributes.setRespondingEvaluator(evaluator);
        evaluatorResult = new RespondingResult(OK, attributes);

        evaluatorFactory = Mockito.mock(RespondingEvaluatorFactory.class);
        when(evaluatorFactory.getType()).thenReturn(evaluatorTypeResult);
        when(evaluatorFactory.getAttributesJsonSchema()).thenReturn(evaluatorSchemaResult);
        when(evaluatorFactory.createInstance(any())).thenReturn(evaluatorResult);
        when(evaluatorFactory.validateAttributes(any())).thenReturn(evaluatorResult);

        evaluatorFactoryNext = Mockito.mock(RespondingEvaluatorFactory.class);
        when(evaluatorFactoryNext.getType()).thenReturn(evaluatorNextTypeResult);
        when(evaluatorFactoryNext.getAttributesJsonSchema()).thenReturn(evaluatorNextSchemaResult);
        when(evaluatorFactoryNext.validateAttributes(any())).thenReturn(evaluatorResult);
        when(evaluatorFactoryNext.createInstance(any())).thenReturn(evaluatorResult);
    }

    @Test
    public void testGetSchemaOneEvaluatorFactory() throws Exception {
        builder.addRespondingEvaluatorFactory(evaluatorFactory);
        compiler = builder.build();
        RespondingResult result = compiler.getSchema();
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getRulesSchema());
    }

    @Test
    public void testGetSchemaTwoEvaluatorFactories() throws Exception {
        builder.addRespondingEvaluatorFactory(evaluatorFactory);
        builder.addRespondingEvaluatorFactory(evaluatorFactoryNext);
        compiler = builder.build();
        RespondingResult result = compiler.getSchema();
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getRulesSchema());
        Assert.assertTrue(result.getAttributes().getRulesSchema().indexOf("a_second_evaluator")
                < result.getAttributes().getRulesSchema().indexOf("b_first_evaluator"));
    }

    @Test
    public void testGetEvaluatorFactories() throws Exception {
        builder.addRespondingEvaluatorFactory(evaluatorFactory);
        builder.addRespondingEvaluatorFactory(evaluatorFactoryNext);
        compiler = builder.build();
        RespondingResult result = compiler.getRespondingEvaluatorFactories();
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getRespondingEvaluatorFactories());
        Assert.assertEquals(2, result.getAttributes().getRespondingEvaluatorFactories().size());
    }

    @Test
    public void testCompileRules() throws Exception {
        builder.addRespondingEvaluatorFactory(evaluatorFactory);
        builder.addRespondingEvaluatorFactory(evaluatorFactoryNext);
        compiler = builder.build();
        RespondingResult result = compiler.compile(testingRules);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getResponseEngine());
        RespondingResultAttributes metadata = result.getAttributes().getResponseEngine()
                .getRulesMetadata().getAttributes();
        Assert.assertEquals(1, metadata.getRulesVersion().intValue());
        Assert.assertEquals(testingRules, metadata.getJsonRules());
        Assert.assertEquals(1, metadata.getNumberOfRules().intValue());
        Assert.assertNotNull(metadata.getCompiledTime());
    }

    @Test
    public void testCompileRulesUnsupportedEvaluator() throws Exception {
        builder.addRespondingEvaluatorFactory(evaluatorFactory);
        compiler = builder.build();
        RespondingResult result = compiler.compile(testingRules);
        Assert.assertEquals(RespondingResult.StatusCode.ERROR, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }

    @Test
    public void testCompileRulesFactoryCreateInstanceFails() throws Exception {
        builder.addRespondingEvaluatorFactory(evaluatorFactory);
        builder.addRespondingEvaluatorFactory(evaluatorFactoryNext);
        when(evaluatorFactoryNext.createInstance(any()))
                .thenReturn(RespondingResult.fromException(new IllegalStateException()));

        compiler = builder.build();
        RespondingResult result = compiler.compile(testingRules);
        Assert.assertEquals(RespondingResult.StatusCode.ERROR, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }


    @Test
    public void testValidateRules() throws Exception {
        builder.addRespondingEvaluatorFactory(evaluatorFactory);
        builder.addRespondingEvaluatorFactory(evaluatorFactoryNext);
        compiler = builder.build();
        RespondingResult result = compiler.validateConfigurations(testingRules);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
    }

    @Test
    public void testValidateRule() throws Exception {
        builder.addRespondingEvaluatorFactory(evaluatorFactory);
        builder.addRespondingEvaluatorFactory(evaluatorFactoryNext);
        compiler = builder.build();
        RespondingResult result = compiler.validateConfiguration(testingRule);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
    }

    @Test
    public void testCompileRulesInvalid() throws Exception {
        builder.addRespondingEvaluatorFactory(evaluatorFactory);
        builder.addRespondingEvaluatorFactory(evaluatorFactoryNext);
        compiler = builder.build();
        RespondingResult result = compiler.compile("INVALID");
        Assert.assertEquals(RespondingResult.StatusCode.ERROR, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }

    @Test
    public void testValidateRulesValidatorFails() throws Exception {
        builder.addRespondingEvaluatorFactory(evaluatorFactory);
        builder.addRespondingEvaluatorFactory(evaluatorFactoryNext);
        when(evaluatorFactoryNext.validateAttributes(any()))
                .thenReturn(RespondingResult.fromException(new IllegalStateException()));

        compiler = builder.build();
        RespondingResult result = compiler.validateConfigurations(testingRules);
        Assert.assertEquals(RespondingResult.StatusCode.ERROR, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }

    @Test
    public void testValidateRulesUnsupportedEvaluator() throws Exception {
        builder.addRespondingEvaluatorFactory(evaluatorFactory);

        compiler = builder.build();
        RespondingResult result = compiler.validateConfigurations(testingRules);
        Assert.assertEquals(RespondingResult.StatusCode.ERROR, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }

    @Test
    public void testValidateRulesInvalid() throws Exception {
        builder.addRespondingEvaluatorFactory(evaluatorFactory);
        builder.addRespondingEvaluatorFactory(evaluatorFactoryNext);
        compiler = builder.build();
        RespondingResult result = compiler.validateConfigurations("INVALID");
        Assert.assertEquals(RespondingResult.StatusCode.ERROR, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }

    @Test
    public void testValidateRuleInvalid() throws Exception {
        builder.addRespondingEvaluatorFactory(evaluatorFactory);
        builder.addRespondingEvaluatorFactory(evaluatorFactoryNext);
        compiler = builder.build();
        RespondingResult result = compiler.validateConfiguration("INVALID");
        Assert.assertEquals(RespondingResult.StatusCode.ERROR, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }

    @Test
    public void testTestingConfigurationsOkMatch() throws Exception {
        builder.addRespondingEvaluatorFactory(evaluatorFactory);
        builder.addRespondingEvaluatorFactory(evaluatorFactoryNext);
        compiler = builder.build();
        RespondingResult result = compiler.testConfigurations(testingRules, testSpecification);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getMessage());
        Assert.assertTrue(result.getAttributes().getMessage().contains("match"));
        Assert.assertTrue(result.getAttributes().getMessage().contains(ResponseFields.RULE_NAME.toString()));
        Assert.assertTrue(result.getAttributes().getMessage().contains(ResponseFields.FULL_RULE_NAME.toString()));
    }

    @Test
    public void testTestingConfigurationsOkFiltered() throws Exception {
        evaluatorResult.getAttributes().setRespondingEvaluator(new FixedResultEvaluator(ResponseEvaluationResult.FILTERED));
        builder.addRespondingEvaluatorFactory(evaluatorFactory);
        builder.addRespondingEvaluatorFactory(evaluatorFactoryNext);
        compiler = builder.build();
        RespondingResult result = compiler.testConfigurations(testingRules, testSpecification);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getMessage());
        Assert.assertTrue(result.getAttributes().getMessage().contains("filtered"));
        Assert.assertTrue(result.getAttributes().getMessage().contains(ResponseFields.RULE_NAME.toString()));
        Assert.assertTrue(result.getAttributes().getMessage().contains(ResponseFields.FULL_RULE_NAME.toString()));
    }

    @Test
    public void testTestingConfigurationsOkNoMatch() throws Exception {
        evaluatorResult.getAttributes().setRespondingEvaluator(new FixedResultEvaluator(ResponseEvaluationResult.NO_MATCH));
        builder.addRespondingEvaluatorFactory(evaluatorFactory);
        builder.addRespondingEvaluatorFactory(evaluatorFactoryNext);
        compiler = builder.build();
        RespondingResult result = compiler.testConfigurations(testingRules, testSpecification);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getMessage());
        Assert.assertTrue(result.getAttributes().getMessage().contains("No rule matches the alert"));
    }

    @Test
    public void testTestingConfigurationsThrowException() throws Exception {
        Evaluable evaluator = Mockito.mock(Evaluable.class);
        when(evaluator.evaluate(any()))
                .thenReturn(RespondingResult.fromException(new IllegalStateException("matcher exception")));
        evaluatorResult.getAttributes().setRespondingEvaluator(evaluator);
        builder.addRespondingEvaluatorFactory(evaluatorFactory);
        builder.addRespondingEvaluatorFactory(evaluatorFactoryNext);
        compiler = builder.build();
        RespondingResult result = compiler.testConfigurations(testingRules, testSpecification);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getMessage());
        Assert.assertTrue(result.getAttributes().getMessage()
                .contains("java.lang.IllegalStateException: matcher exception"));
    }
}
