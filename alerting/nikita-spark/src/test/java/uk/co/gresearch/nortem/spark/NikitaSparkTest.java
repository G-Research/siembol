package uk.co.gresearch.nortem.spark;

import org.adrianwalker.multilinestring.Multiline;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Base64;

public class NikitaSparkTest {
    /**
     *{
     * "source_type" : "secret",
     * "from_date" : "2019-06-18",
     * "to_date" : "2019-06-19",
     * "rules" :{
     *  "rules_version" :1,
     *  "tags" : [ { "tag_name" : "detection:source", "tag_value" : "nikita" } ],
     *  "rules" : [ {
     *      "rule_name" : "test_rule",
     *      "rule_version" : 1,
     *      "rule_author" : "dummy",
     *      "rule_protection" : {
     *          "max_per_hour" : 100,
     *          "max_per_day" : 10000
     *      },
     *      "rule_description": "Testing rule",
     *      "source_type" : "*",
     *      "matchers" : [ {
     *          "matcher_type" : "REGEX_MATCH",
     *          "is_negated" : false,
     *          "field" : "is_alert",
     *          "data" : "(?i)true" }
     *          ]
     *  }]
     *}
     *}
     **/
    @Multiline
    public static String testAttributes;

    @Test
    @Ignore
    public void testSpecification() throws Exception {
        String encoded = Base64.getEncoder().encodeToString(testAttributes.getBytes());
        String[] arg = new String[1];
        arg[0] = encoded;
        NikitaSpark.main(arg);
    }
}
