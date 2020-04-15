package uk.co.gresearch.siembol.response.compiler;

import org.adrianwalker.multilinestring.Multiline;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.gresearch.siembol.response.common.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class RespondingCompilerImplTest {
    /**
     * {
     *   "type" : "object",
     *   "description" : "Attributes for fixed evaluator",
     *   "title" : "fixed evaluator attributes",
     *   "properties" : {
     *     "evaluation_result" : {
     *       "enum" : [ "match", "no_match", "filtered" ],
     *       "type" : "string",
     *       "description" : "Evaluation result returned by the evaluator"
     *     }
     *   },
     *   "required" : [ "evaluation_result" ]
     * }
     */
    @Multiline
    public static String evaluatorAttributes;

    /**
     * {
     *   "type" : "object",
     *   "description" : "json path assignment",
     *   "title" : "json path assignment",
     *   "properties" : {
     *     "assignment_type" : {
     *       "enum" : [ "match_always", "no_match_when_empty", "error_match_when_empty" ],
     *       "type" : "string",
     *       "description" : "The type of the assigment based on json path evaluation"
     *     },
     *     "field_name" : {
     *       "type" : "string",
     *       "description" : "The name of the field in which the non empty result of the json path will be stored"
     *     },
     *     "json_path" : {
     *       "type" : "string",
     *       "description" : "Json path ",
     *       "minItems" : 1
     *     }
     *   },
     *   "required" : [ "assignment_type", "field_name", "json_path" ]
     * }
     */
    @Multiline
    public static String evaluatorNextAttributes;

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
     *           "evaluator_type": "first_evaluator",
     *           "evaluator_attributes": {
     *             "evaluation_result": "match"
     *           }
     *         },
     *         {
     *           "evaluator_type": "second_evaluator",
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
     *       "evaluator_type": "first_evaluator",
     *       "evaluator_attributes": {
     *         "evaluation_result": "match"
     *       }
     *     },
     *     {
     *       "evaluator_type": "second_evaluator",
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

    private RespondingCompilerImpl compiler;
    private RespondingCompilerImpl.Builder builder;
    private MetricFactory metricFactory;
    private RespondingEvaluatorFactory evaluatorFactory;
    private RespondingResult evaluatorSchemaResult;
    private RespondingResult evaluatorTypeResult;
    private RespondingResult evaluatorNextSchemaResult;
    private RespondingResult evaluatorNextTypeResult;
    private Evaluable evaluator;

    private RespondingEvaluatorFactory evaluatorFactoryNext;

    @Before
    public void setUp() throws Exception {
        metricFactory = new TestMetricFactory();
        builder = new RespondingCompilerImpl.Builder()
                .metricFactory(metricFactory);
        evaluatorTypeResult = RespondingResult.fromEvaluatorType("first_evaluator");
        evaluatorNextTypeResult = RespondingResult.fromEvaluatorType("second_evaluator");

        evaluatorSchemaResult = RespondingResult.fromAttributesSchema(evaluatorAttributes);
        evaluatorNextSchemaResult = RespondingResult.fromAttributesSchema(evaluatorNextAttributes);

        evaluator = Mockito.mock(Evaluable.class);

        RespondingResultAttributes attributes = new RespondingResultAttributes();
        attributes.setRespondingEvaluator(evaluator);
        RespondingResult evaluatorResult = new RespondingResult(RespondingResult.StatusCode.OK, attributes);

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
    public void testgetSchemaOneEvaluatorFactory() throws Exception {
        builder.addRespondingEvaluatorFactory(evaluatorFactory);
        compiler = builder.build();
        RespondingResult result = compiler.getSchema();
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getRulesSchema());
    }

    @Test
    public void testgetSchemaTwoEvaluatorFactories() throws Exception {
        builder.addRespondingEvaluatorFactory(evaluatorFactory);
        builder.addRespondingEvaluatorFactory(evaluatorFactoryNext);
        compiler = builder.build();
        RespondingResult result = compiler.getSchema();
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getRulesSchema());
    }

    @Test
    public void testGetEvaluatorFactories() throws Exception {
        builder.addRespondingEvaluatorFactory(evaluatorFactory);
        builder.addRespondingEvaluatorFactory(evaluatorFactoryNext);
        compiler = builder.build();
        RespondingResult result = compiler.getRespondingEvaluatorFactories();
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getRespondingEvaluatorFactories());
        Assert.assertEquals(2, result.getAttributes().getRespondingEvaluatorFactories().size());
    }

    @Test
    public void testGetEvaluatorValidators() throws Exception {
        builder.addRespondingEvaluatorFactory(evaluatorFactory);
        builder.addRespondingEvaluatorFactory(evaluatorFactoryNext);
        compiler = builder.build();
        RespondingResult result = compiler.getRespondingEvaluatorValidators();
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getRespondingEvaluatorValidators());
        Assert.assertEquals(2, result.getAttributes().getRespondingEvaluatorValidators().size());
    }

    @Test
    public void testCompileRules() throws Exception {
        builder.addRespondingEvaluatorFactory(evaluatorFactory);
        builder.addRespondingEvaluatorFactory(evaluatorFactoryNext);
        compiler = builder.build();
        RespondingResult result = compiler.compile(testingRules);
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getResponseEngine());
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
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
    }

    @Test
    public void testValidateRule() throws Exception {
        builder.addRespondingEvaluatorFactory(evaluatorFactory);
        builder.addRespondingEvaluatorFactory(evaluatorFactoryNext);
        compiler = builder.build();
        RespondingResult result = compiler.validateConfiguration(testingRule);
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
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

}
