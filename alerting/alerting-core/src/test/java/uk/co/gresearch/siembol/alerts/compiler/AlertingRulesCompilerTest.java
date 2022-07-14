package uk.co.gresearch.siembol.alerts.compiler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.gresearch.siembol.alerts.common.CompositeAlertingEngine;
import uk.co.gresearch.siembol.alerts.common.EvaluationResult;
import uk.co.gresearch.siembol.alerts.common.AlertingResult;
import uk.co.gresearch.siembol.alerts.engine.AlertingEngineImpl;

import java.util.Arrays;
import java.util.List;

public class AlertingRulesCompilerTest {
    private final String alertRules = """
            {
              "rules_version" :1,
              "tags" : [ { "tag_name" : "detection_source", "tag_value" : "alerts" } ],
              "rules" : [ {
                  "rule_name" : "siembol_alert_generic",
                  "rule_version" : 1,
                  "rule_author" : "dummy",
                  "rule_description": "Test rule - is_alert is equal to true",
                  "source_type" : "*",
                  "matchers" : [ {
                        "is_enabled" : true,
                        "matcher_type" : "REGEX_MATCH",
                        "is_negated" : false,
                        "field" : "is_alert",
                        "data" : "(?i)true"
                      },
                      {
                       "is_enabled" : true,
                       "matcher_type": "REGEX_MATCH",
                       "is_negated": false,
                       "field": "source_type",
                       "data": "(?<sensor>.*)"
                     }
                  ]
              }]
            }
            """;

    private final String alertRule = """
            {
              "rule_name": "siembol_alert_generic",
              "rule_version": 1,
              "rule_author": "dummy",
              "rule_description": "Test rule - is_alert is equal to true",
              "source_type": "*",
              "matchers": [
                {
                  "is_enabled": true,
                  "matcher_type": "REGEX_MATCH",
                  "is_negated": false,
                  "field": "source_type",
                  "data": "(?<sensor>.*)"
                },
                {
                  "is_enabled": true,
                  "matcher_type": "REGEX_MATCH",
                  "is_negated": false,
                  "field": "is_alert",
                  "data": "(?i)true"
                },
                {
                  "is_enabled": true,
                  "matcher_type": "NUMERIC_COMPARE",
                  "is_negated": false,
                  "field": "dummy_field_int",
                  "compare_type" : "equal",
                  "expression" : "1"
                }
              ]
            }
            """;

    private final String goodAlert = """
            {
              "source_type" : "secret",
              "is_alert" : "TruE",
              "dummy_field_int" : 1,
              "dummy_field_boolean" : false
            }
            """;

    private final String goodAlertWithSecret = """
            {
              "source_type" : "secret",
              "is_alert" : "TruE",
              "dummy_field_int" : 1,
              "dummy_field_boolean" : false,
              "is_secret" : "true"
            }
            """;

    private final String ruleWithCompositeMatchers = """
            {
              "rule_name": "siembol_alert_generic_with_composite_matchers",
              "rule_version": 1,
              "rule_author": "dummy",
              "rule_description": "Test rule with composite matchers",
              "source_type": "*",
              "matchers": [
                {
                  "is_enabled" : true,
                  "matcher_type": "CONTAINS",
                  "is_negated": false,
                  "field": "is_alert",
                  "data": "true",
                  "case_insensitive" : true,
                  "starts_with" : true,
                  "ends_with" : true
                },
                {
                  "is_enabled" : true,
                  "matcher_type": "REGEX_MATCH",
                  "is_negated": false,
                  "field": "source_type",
                  "data": "(?<sensor>.*)"
                },
                {
                  "is_enabled" : true,
                  "matcher_type": "COMPOSITE_OR",
                  "is_negated": false,
                  "matchers": [
                    {
                      "is_enabled" : true,
                      "matcher_type": "REGEX_MATCH",
                      "is_negated": false,
                      "field": "is_secret",
                      "data": "(?i)true"
                    },
                    {
                      "is_enabled" : true,
                      "matcher_type": "COMPOSITE_AND",
                      "is_negated": false,
                      "matchers": [
                        {
                          "is_enabled":true ,
                          "matcher_type": "REGEX_MATCH",
                          "is_negated": false,
                          "field": "is_public",
                          "data": "(?i)true"
                        },
                        {
                          "is_enabled":true,
                          "matcher_type": "IS_IN_SET",
                          "is_negated": false,
                          "field": "is_detected",
                          "data": "yes",
                          "case_insensitive" : true
                        }
                      ]
                    }
                  ]
                }
              ]
            }
            """;

    private AlertingCompiler compiler;

    @Before
    public void setUp() throws Exception {
        compiler = AlertingRulesCompiler.createAlertingRulesCompiler();
    }

    @Test
    public void getSchema() {
        AlertingResult ret = compiler.getSchema();

        Assert.assertEquals(AlertingResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertNotNull(ret.getAttributes().getRulesSchema());
    }

    @Test
    public void validationRulesOk() {
        AlertingResult ret = compiler.validateRules(alertRules);
        Assert.assertEquals(AlertingResult.StatusCode.OK, ret.getStatusCode());
        AlertingResult matchResult = ret.getAttributes().getEngine().evaluate(goodAlert);
        Assert.assertEquals(AlertingResult.StatusCode.OK, matchResult.getStatusCode());
        Assert.assertEquals(EvaluationResult.MATCH, matchResult.getAttributes().getEvaluationResult());
        Assert.assertEquals(1, matchResult.getAttributes().getOutputEvents().size());
        Assert.assertEquals("secret", matchResult.getAttributes().getOutputEvents().get(0).get("sensor"));
    }

    @Test
    public void validationRulesInvalidJson() {
        AlertingResult ret = compiler.validateRules("INVALID JSON");
        Assert.assertEquals(AlertingResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getMessage().contains("JsonParseException"));
    }

    @Test
    public void validationRulesMissingFields() {
        AlertingResult ret = compiler.validateRules(alertRules.replace("rule_name", "dummy"));
        Assert.assertEquals(AlertingResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes()
                .getMessage()
                .contains("error: object has missing required properties ([\"rule_name\"])"));
    }

    @Test
    public void validationRuleDisabledFirstMatcherOk() {
        AlertingResult ret = compiler.validateRule(alertRule.replaceFirst("\"is_enabled\": true",
                "\"is_enabled\" : false"));
        Assert.assertEquals(AlertingResult.StatusCode.OK, ret.getStatusCode());
        AlertingResult matchResult = ret.getAttributes().getEngine().evaluate(goodAlert);
        Assert.assertEquals(AlertingResult.StatusCode.OK, matchResult.getStatusCode());
        Assert.assertEquals(EvaluationResult.MATCH, matchResult.getAttributes().getEvaluationResult());
        Assert.assertEquals(1, matchResult.getAttributes().getOutputEvents().size());
        Assert.assertNull(matchResult.getAttributes().getOutputEvents().get(0).get("sensor"));
    }

    @Test
    public void validationRuleOk() {
        AlertingResult ret = compiler.validateRule(alertRule);
        Assert.assertEquals(AlertingResult.StatusCode.OK, ret.getStatusCode());
        AlertingResult matchResult = ret.getAttributes().getEngine().evaluate(goodAlert);
        Assert.assertEquals(AlertingResult.StatusCode.OK, matchResult.getStatusCode());
        Assert.assertEquals(EvaluationResult.MATCH, matchResult.getAttributes().getEvaluationResult());
        Assert.assertEquals(1, matchResult.getAttributes().getOutputEvents().size());
        Assert.assertEquals("secret", matchResult.getAttributes().getOutputEvents().get(0).get("sensor"));
    }

    @Test
    public void validationRuleInvalidRegex() {
        AlertingResult ret = compiler.validateRule(alertRule.replace("<sensor>", "<sensor"));
        Assert.assertEquals(AlertingResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getException().contains("PatternSyntaxException"));
    }

    @Test
    public void validationRuleDisabledMatchers() {
        AlertingResult ret = compiler.validateRule(alertRule.replaceAll("\"is_enabled\": true",
                "\"is_enabled\" : false"));
        Assert.assertEquals(AlertingResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getException().contains("Empty matchers in a rule"));
    }

    @Test
    public void validationRuleInvalidJson() {
        AlertingResult ret = compiler.validateRule("INVALID JSON");
        Assert.assertEquals(AlertingResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getException().contains("JsonParseException"));
    }

    @Test
    public void validationRuleMissingFields() {
        AlertingResult ret = compiler.validateRules(alertRule.replace("rule_name", "dummy"));
        Assert.assertEquals(AlertingResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes()
                .getMessage()
                .contains("error: object has missing required properties"));
    }

    @Test
    public void testRuleOk() {
        AlertingResult ret = compiler.testRule(alertRule, goodAlert);
        Assert.assertEquals(AlertingResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertFalse(ret.getAttributes().getMessage().isEmpty());
    }

    @Test
    public void testingRuleInvalidJsonRule() {
        AlertingResult ret = compiler.testRule("INVALID JSON", goodAlert);
        Assert.assertEquals(AlertingResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getException().contains("JsonParseException"));
    }
    @Test
    public void testingRuleInvalidRegex() {
        AlertingResult ret = compiler.testRule(alertRule.replace("<sensor>", "<sensor"), goodAlert);
        Assert.assertEquals(AlertingResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getException().contains("PatternSyntaxException"));
    }

    @Test
    public void testingRuleInvalidJsonAlert() {
        AlertingResult ret = compiler.testRule(alertRule, "INVALID");
        Assert.assertEquals(AlertingResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getException().contains("JsonParseException"));
    }

    @Test
    public void testingRuleMissingFields() {
        AlertingResult ret = compiler.testRule(
                alertRule.replace("rule_name", "dummy"),
                goodAlert);

        Assert.assertEquals(AlertingResult.StatusCode.ERROR, ret.getStatusCode());
    }

    @Test
    public void testingRuleDisabledMatchers() {
        AlertingResult ret = compiler.testRule(alertRule.replace("\"is_enabled\": true",
                "\"is_enabled\" : false"), goodAlert);
        Assert.assertEquals(AlertingResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getException().contains("Empty matchers in a rule"));
    }

    @Test
    public void testRulesOk() {
        AlertingResult ret = compiler.testRules(alertRules, goodAlert);
        Assert.assertEquals(AlertingResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertFalse(ret.getAttributes().getMessage().isEmpty());
    }

    @Test
    public void testingRulesInvalidJsonRule() {
        AlertingResult ret = compiler.testRules("INVALID JSON", goodAlert);
        Assert.assertEquals(AlertingResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getMessage().contains("JsonParseException"));
    }

    @Test
    public void testingRulesInvalidJsonAlert() {
        AlertingResult ret = compiler.testRules(alertRules, "INVALID");
        Assert.assertEquals(AlertingResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getException().contains("JsonParseException"));
    }

    @Test
    public void testingRulesMissingFields() {
        AlertingResult ret = compiler.testRules(
                alertRule.replace("rule_name", "dummy"),
                goodAlert);

        Assert.assertEquals(AlertingResult.StatusCode.ERROR, ret.getStatusCode());
    }

    @Test
    public void validationRuleWithCompositeMatchersOk() {
        AlertingResult ret = compiler.validateRule(ruleWithCompositeMatchers);
        Assert.assertEquals(AlertingResult.StatusCode.OK, ret.getStatusCode());
    }

    @Test
    public void validationRuleWithCompositeMatchersInvalid() {
        AlertingResult ret = compiler.validateRule(ruleWithCompositeMatchers.replace("\"matchers\"",
                "\"invalid_matchers\""));
        Assert.assertEquals(AlertingResult.StatusCode.ERROR, ret.getStatusCode());
    }

    @Test
    public void validationRuleWithCompositeDisabled() {
        AlertingResult ret = compiler.validateRule(ruleWithCompositeMatchers.replace("\"is_enabled\":true",
                "\"is_enabled\" : false"));
        Assert.assertEquals(AlertingResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getException().contains("Empty matchers in the composite matcher"));
    }

    @Test
    public void compileRulesWithCompositeMatcher() {
        String rules = "{ \"rules_version\" : 1, \"tags\" : [], \"rules\" : [" + ruleWithCompositeMatchers + "]}";
        AlertingResult compileResult = compiler.compile(rules);

        Assert.assertEquals(AlertingResult.StatusCode.OK, compileResult.getStatusCode());
        Assert.assertNotNull(compileResult.getAttributes().getEngine());

        AlertingResult matchResult1 = compileResult.getAttributes().getEngine().evaluate(goodAlert);
        Assert.assertEquals(AlertingResult.StatusCode.OK, matchResult1.getStatusCode());
        Assert.assertEquals(EvaluationResult.NO_MATCH, matchResult1.getAttributes().getEvaluationResult());

        AlertingResult matchResult2 = compileResult.getAttributes().getEngine().evaluate(goodAlertWithSecret);
        Assert.assertEquals(AlertingResult.StatusCode.OK, matchResult2.getStatusCode());
        Assert.assertEquals(EvaluationResult.MATCH, matchResult2.getAttributes().getEvaluationResult());
        Assert.assertEquals(1, matchResult2.getAttributes().getOutputEvents().size());
        Assert.assertEquals("secret", matchResult2.getAttributes().getOutputEvents().get(0).get("sensor"));
    }

    @Test
    public void compileRulesListSizeOne() {
        AlertingResult compileResult = compiler.compile(List.of(alertRules));
        Assert.assertEquals(AlertingResult.StatusCode.OK, compileResult.getStatusCode());
        Assert.assertNotNull(compileResult.getAttributes().getEngine());
        Assert.assertTrue(compileResult.getAttributes().getEngine() instanceof AlertingEngineImpl);
    }

    @Test
    public void compileRulesListSizeTwo() {
        AlertingResult compileResult = compiler.compile(Arrays.asList(alertRules, alertRules));
        Assert.assertEquals(AlertingResult.StatusCode.OK, compileResult.getStatusCode());
        Assert.assertNotNull(compileResult.getAttributes().getEngine());
        Assert.assertTrue(compileResult.getAttributes().getEngine() instanceof CompositeAlertingEngine);
    }

    @Test
    public void compileRulesListInvalid() {
        AlertingResult compileResult = compiler.compile(Arrays.asList(alertRules, "INVALID"));
        Assert.assertEquals(AlertingResult.StatusCode.ERROR, compileResult.getStatusCode());
    }
}

