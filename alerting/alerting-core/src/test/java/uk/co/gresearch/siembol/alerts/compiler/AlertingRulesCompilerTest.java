package uk.co.gresearch.siembol.alerts.compiler;

import org.adrianwalker.multilinestring.Multiline;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.gresearch.siembol.alerts.common.EvaluationResult;
import uk.co.gresearch.siembol.alerts.common.AlertingResult;

public class AlertingRulesCompilerTest {
    /**
     *{
     *  "rules_version" :1,
     *  "tags" : [ { "tag_name" : "detection:source", "tag_value" : "alerts" } ],
     *  "rules" : [ {
     *      "rule_name" : "siembol_alert_generic",
     *      "rule_version" : 1,
     *      "rule_author" : "dummy",
     *      "rule_description": "Test rule - is_alert is equal to true",
     *      "source_type" : "*",
     *      "matchers" : [ {
     *          "matcher_type" : "REGEX_MATCH",
     *          "is_negated" : false,
     *          "field" : "is_alert",
     *          "data" : "(?i)true" },
     *          {
     *           "matcher_type": "REGEX_MATCH",
     *           "is_negated": false,
     *           "field": "source_type",
     *           "data": "(?<sensor>.*)"
     *         }
     *          ]
     *  }]
     *}
     **/
    @Multiline
    public static String alertRules;

    /**
     *{
     *      "rule_name" : "siembol_alert_generic",
     *      "rule_version" : 1,
     *      "rule_author" : "dummy",
     *      "rule_description": "Test rule - is_alert is equal to true",
     *      "source_type" : "*",
     *      "matchers" : [ {
     *          "matcher_type" : "REGEX_MATCH",
     *          "is_negated" : false,
     *          "field" : "is_alert",
     *          "data" : "(?i)true" },
     *          {
     *           "matcher_type": "REGEX_MATCH",
     *           "is_negated": false,
     *           "field": "source_type",
     *           "data": "(?<sensor>.*)"
     *         }]
     *}
     **/
    @Multiline
    public static String alertRule;

    /**
     *{
     *  "source_type" : "secret",
     *  "is_alert" : "TruE",
     *  "dummy_field_int" : 1,
     *  "dummy_field_boolean" : false
     *}
     **/
    @Multiline
    public static String goodAlert;

    private AlertingCompiler compiler;

    @Before
    public void setUp() throws Exception {
        compiler  = AlertingRulesCompiler.createAlertingRulesCompiler();
    }

    @Test
    public void testGetSchema() {
        AlertingResult ret = compiler.getSchema();

        Assert.assertEquals(AlertingResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertNotNull(ret.getAttributes().getRulesSchema());
    }

    @Test
    public void validationRulesOK() {
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
    public void validationRuleOK() {
        AlertingResult ret = compiler.validateRule(alertRule);
        Assert.assertEquals(AlertingResult.StatusCode.OK, ret.getStatusCode());
        AlertingResult matchResult = ret.getAttributes().getEngine().evaluate(goodAlert);
        Assert.assertEquals(AlertingResult.StatusCode.OK, matchResult.getStatusCode());
        Assert.assertEquals(EvaluationResult.MATCH, matchResult.getAttributes().getEvaluationResult());
        Assert.assertEquals(1, matchResult.getAttributes().getOutputEvents().size());
        Assert.assertEquals("secret", matchResult.getAttributes().getOutputEvents().get(0).get("sensor"));
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
    public void testRuleOK() {
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
    public void testRulesOK() {
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
}

