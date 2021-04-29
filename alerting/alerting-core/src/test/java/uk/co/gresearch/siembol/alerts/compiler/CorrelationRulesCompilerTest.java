package uk.co.gresearch.siembol.alerts.compiler;

import org.adrianwalker.multilinestring.Multiline;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.gresearch.siembol.alerts.common.AlertingResult;

public class CorrelationRulesCompilerTest {
    /**
     *{
     *  "rules_version" :1,
     *  "tags" : [ { "tag_name" : "detection_source", "tag_value" : "siembol_correlation_alerts" } ],
     *  "rules" : [ {
     *      "tags" : [ { "tag_name" : "test", "tag_value" : "true" } ],
     *      "rule_protection": {
     *         "max_per_hour": 500,
     *         "max_per_day": 1000
     *       },
     *      "rule_name" : "test_rule",
     *      "rule_version" : 1,
     *      "rule_author" : "dummy",
     *      "rule_description": "Testing rule",
     *      "correlation_attributes" : {
     *          "time_unit" : "seconds",
     *          "time_window" : 500,
     *          "time_computation_type" : "processing_time",
     *          "alerts" : [
     *          {
     *              "alert" : "alert1",
     *              "threshold" : 5
     *          },
     *          {
     *              "alert" : "alert2",
     *              "threshold" : 5
     *         }]
     *      }
     *  }]
     *}
     **/
    @Multiline
    public static String rulesWithSimpleCorrelationRule;

    /**{
     *      "rule_name" : "test_rule_event_time",
     *      "rule_version" : 1,
     *      "rule_author" : "dummy",
     *      "rule_description": "Testing rule",
     *      "correlation_attributes" : {
     *          "time_unit" : "seconds",
     *          "time_window" : 500,
     *          "time_computation_type" : "event_time",
     *          "max_time_lag_in_sec": 30,
     *          "alerts" : [
     *          {
     *              "alert" : "alert1",
     *              "threshold" : 5
     *          },
     *          {
     *              "mandatory": true,
     *              "alert" : "alert2",
     *              "threshold" : 5
     *         }]
     *      }
     *  }
     **/
    @Multiline
    public static String simpleCorrelationRule;


    private AlertingCompiler compiler;

    @Before
    public void setUp() throws Exception {
        compiler  = AlertingCorrelationRulesCompiler.createAlertingCorrelationRulesCompiler();
    }

    @Test
    public void testGetSchema() {
        AlertingResult ret = compiler.getSchema();
        Assert.assertEquals(AlertingResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertNotNull(ret.getAttributes().getRulesSchema());
    }

    @Test
    public void validationRulesOK() {
        AlertingResult ret = compiler.validateRules(rulesWithSimpleCorrelationRule);
        Assert.assertEquals(AlertingResult.StatusCode.OK, ret.getStatusCode());
    }

    @Test
    public void validationRulesInvalidJson() {
        AlertingResult ret = compiler.validateRules("INVALID JSON");
        Assert.assertEquals(AlertingResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getMessage().contains("JsonParseException"));
    }


    @Test
    public void validationRulesMissingFields() {
        AlertingResult ret = compiler.validateRules(rulesWithSimpleCorrelationRule
                .replace("rule_name", "dummy"));
        Assert.assertEquals(AlertingResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes()
                .getMessage()
                .contains("error: object has missing required properties ([\"rule_name\"])"));
    }

    @Test
    public void validationRulesMissingFields2() {
        AlertingResult ret = compiler.validateRules(rulesWithSimpleCorrelationRule
                .replace("\"threshold\"", "\"dummy\""));
        Assert.assertEquals(AlertingResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes()
                .getMessage()
                .contains("error: object has missing required properties ([\"threshold\"])"));
    }

    @Test
    public void validationRuleOK() {
        AlertingResult ret = compiler.validateRule(simpleCorrelationRule);
        Assert.assertEquals(AlertingResult.StatusCode.OK, ret.getStatusCode());
    }

    @Test
    public void validationRuleInvalidJson() {
        AlertingResult ret = compiler.validateRule("INVALID JSON");
        Assert.assertEquals(AlertingResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getException().contains("JsonParseException"));
    }


    @Test
    public void validationRuleWrongFields() {
        AlertingResult ret = compiler.validateRule(simpleCorrelationRule
                .replace("rule_name", "dummy"));
        Assert.assertEquals(AlertingResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes()
                .getException()
                .contains("Unrecognized field \"dummy\""));
    }

    @Test
    public void validationRuleWrongFields2() {
        AlertingResult ret = compiler.validateRule(simpleCorrelationRule
                .replace("\"threshold\"", "\"dummy\""));
        Assert.assertEquals(AlertingResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes()
                .getException()
                .contains("Unrecognized field \"dummy\""));
    }

    @Test(expected = java.lang.UnsupportedOperationException.class)
    public void testRules() {
        compiler.testRules(rulesWithSimpleCorrelationRule, "dummy");
    }

    @Test(expected = java.lang.UnsupportedOperationException.class)
    public void testingRule() {
        compiler.testRule(simpleCorrelationRule, "dummy");
    }

}

