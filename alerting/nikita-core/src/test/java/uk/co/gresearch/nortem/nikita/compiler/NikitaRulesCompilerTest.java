package uk.co.gresearch.nortem.nikita.compiler;

import org.adrianwalker.multilinestring.Multiline;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.gresearch.nortem.nikita.common.EvaluationResult;
import uk.co.gresearch.nortem.nikita.common.NikitaResult;

public class NikitaRulesCompilerTest {
    /**
     *{
     *  "rules_version" :1,
     *  "tags" : [ { "tag_name" : "detection:source", "tag_value" : "nikita" } ],
     *  "rules" : [ {
     *      "rule_name" : "nortem_alert_generic",
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
     *           "field": "source.type",
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
     *      "rule_name" : "nortem_alert_generic",
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
     *           "field": "source.type",
     *           "data": "(?<sensor>.*)"
     *         }]
     *}
     **/
    @Multiline
    public static String alertRule;

    /**
     *{
     *  "source.type" : "secret",
     *  "is_alert" : "TruE",
     *  "dummy_field_int" : 1,
     *  "dummy_field_boolean" : false
     *}
     **/
    @Multiline
    public static String goodAlert;

    private NikitaCompiler compiler;

    @Before
    public void setUp() throws Exception {
        compiler  = NikitaRulesCompiler.createNikitaRulesCompiler();
    }

    @Test
    public void testGetSchema() {
        NikitaResult ret = compiler.getSchema();

        Assert.assertEquals(NikitaResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertNotNull(ret.getAttributes().getRulesSchema());
    }

    @Test
    public void validationRulesOK() {
        NikitaResult ret = compiler.validateRules(alertRules);
        Assert.assertEquals(NikitaResult.StatusCode.OK, ret.getStatusCode());
        NikitaResult matchResult = ret.getAttributes().getEngine().evaluate(goodAlert);
        Assert.assertEquals(NikitaResult.StatusCode.OK, matchResult.getStatusCode());
        Assert.assertEquals(EvaluationResult.MATCH, matchResult.getAttributes().getEvaluationResult());
        Assert.assertEquals(1, matchResult.getAttributes().getOutputEvents().size());
        Assert.assertEquals("secret", matchResult.getAttributes().getOutputEvents().get(0).get("sensor"));
    }

    @Test
    public void validationRulesInvalidJson() {
        NikitaResult ret = compiler.validateRules("INVALID JSON");
        Assert.assertEquals(NikitaResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getMessage().contains("JsonParseException"));
    }

    @Test
    public void validationRulesMissingFields() {
        NikitaResult ret = compiler.validateRules(alertRules.replace("rule_name", "dummy"));
        Assert.assertEquals(NikitaResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes()
                .getMessage()
                .contains("error: object has missing required properties ([\"rule_name\"])"));
    }

    @Test
    public void validationRuleOK() {
        NikitaResult ret = compiler.validateRule(alertRule);
        Assert.assertEquals(NikitaResult.StatusCode.OK, ret.getStatusCode());
        NikitaResult matchResult = ret.getAttributes().getEngine().evaluate(goodAlert);
        Assert.assertEquals(NikitaResult.StatusCode.OK, matchResult.getStatusCode());
        Assert.assertEquals(EvaluationResult.MATCH, matchResult.getAttributes().getEvaluationResult());
        Assert.assertEquals(1, matchResult.getAttributes().getOutputEvents().size());
        Assert.assertEquals("secret", matchResult.getAttributes().getOutputEvents().get(0).get("sensor"));
    }

    @Test
    public void validationRuleInvalidJson() {
        NikitaResult ret = compiler.validateRule("INVALID JSON");
        Assert.assertEquals(NikitaResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getException().contains("JsonParseException"));
    }

    @Test
    public void validationRuleMissingFields() {
        NikitaResult ret = compiler.validateRules(alertRule.replace("rule_name", "dummy"));
        Assert.assertEquals(NikitaResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes()
                .getMessage()
                .contains("error: object has missing required properties"));
    }

    @Test
    public void testRuleOK() {
        NikitaResult ret = compiler.testRule(alertRule, goodAlert);
        Assert.assertEquals(NikitaResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertFalse(ret.getAttributes().getMessage().isEmpty());
    }

    @Test
    public void testingRuleInvalidJsonRule() {
        NikitaResult ret = compiler.testRule("INVALID JSON", goodAlert);
        Assert.assertEquals(NikitaResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getException().contains("JsonParseException"));
    }

    @Test
    public void testingRuleInvalidJsonAlert() {
        NikitaResult ret = compiler.testRule(alertRule, "INVALID");
        Assert.assertEquals(NikitaResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getException().contains("JsonParseException"));
    }

    @Test
    public void testingRuleMissingFields() {
        NikitaResult ret = compiler.testRule(
                alertRule.replace("rule_name", "dummy"),
                goodAlert);

        Assert.assertEquals(NikitaResult.StatusCode.ERROR, ret.getStatusCode());
    }

    @Test
    public void testRulesOK() {
        NikitaResult ret = compiler.testRules(alertRules, goodAlert);
        Assert.assertEquals(NikitaResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertFalse(ret.getAttributes().getMessage().isEmpty());
    }

    @Test
    public void testingRulesInvalidJsonRule() {
        NikitaResult ret = compiler.testRules("INVALID JSON", goodAlert);
        Assert.assertEquals(NikitaResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getMessage().contains("JsonParseException"));
    }

    @Test
    public void testingRulesInvalidJsonAlert() {
        NikitaResult ret = compiler.testRules(alertRules, "INVALID");
        Assert.assertEquals(NikitaResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getException().contains("JsonParseException"));
    }

    @Test
    public void testingRulesMissingFields() {
        NikitaResult ret = compiler.testRules(
                alertRule.replace("rule_name", "dummy"),
                goodAlert);

        Assert.assertEquals(NikitaResult.StatusCode.ERROR, ret.getStatusCode());
    }
}

