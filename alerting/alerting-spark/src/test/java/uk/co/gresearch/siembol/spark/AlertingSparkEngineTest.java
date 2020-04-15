package uk.co.gresearch.siembol.spark;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Assert;
import org.junit.Test;

public class AlertingSparkEngineTest {
    /**
     *{
     *  "rules_version" :1,
     *  "tags" : [ { "tag_name" : "detection:source", "tag_value" : "alerts" } ],
     *  "rules" : [ {
     *      "rule_name" : "test_rule",
     *      "rule_version" : 1,
     *      "rule_author" : "dummy",
     *      "rule_protection" : {
     *          "max_per_hour" : 100,
     *          "max_per_day" : 10000
     *      },
     *      "rule_description": "test rule - is_alert is equal to true",
     *      "source_type" : "*",
     *      "matchers" : [ {
     *          "matcher_type" : "REGEX_MATCH",
     *          "is_negated" : false,
     *          "field" : "is_alert",
     *          "data" : "(?i)true" }
     *          ]
     *  }]
     *}
     **/
    @Multiline
    public static String isAlertRules;

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


    @Test
    public void serializableTest() throws Exception {
        AlertingSparkEngine engine = new AlertingSparkEngine(isAlertRules);
        AlertingSparkResult ret = engine.eval(goodAlert, 100);


        byte[] blob = SerializationUtils.serialize(engine);
        Assert.assertTrue(blob.length > 0);
        AlertingSparkEngine clone = SerializationUtils.clone(engine);
        AlertingSparkResult retClone = clone.eval(goodAlert, 100);

        Assert.assertEquals(ret.getMatchesTotal(), retClone.getMatchesTotal());
        Assert.assertEquals(ret.getExceptionsTotal(), retClone.getExceptionsTotal());
        Assert.assertTrue(ret.getExceptions().equals(retClone.getExceptions()));
        Assert.assertTrue(ret.getMatches().equals(retClone.getMatches()));
    }
}
