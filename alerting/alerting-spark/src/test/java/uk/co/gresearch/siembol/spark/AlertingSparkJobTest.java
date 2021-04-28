package uk.co.gresearch.siembol.spark;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.junit.*;

import java.util.ArrayList;


public class AlertingSparkJobTest {
    /**
     *{
     *  "rules_version" :1,
     *  "tags" : [ { "tag_name" : "detection_source", "tag_value" : "siembol_alerts" } ],
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

    /**
     *{
     *  "source_type" : "secret",
     *  "dummy_field_int" : 1,
     *  "dummy_field_boolean" : false
     *}
     **/
    @Multiline
    public static String eventWithoutAlert;

    private JavaSparkContext sc;
    private AlertingSparkJob job;
    private int maxResult = 100;

    @Before
    public void setup() {
        sc = new JavaSparkContext(new SparkConf().setAppName("test").setMaster("local"));
    }

    @After
    public void down() {
        sc.close();
    }

    @Test
    @Ignore
    public void trivialRddTest() throws Exception {
        ArrayList<String> events = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            events.add(goodAlert);
            events.add("INVALID");
        }
        JavaRDD<String> eventsRdd = sc.parallelize(events);
        job = new AlertingSparkJob.Builder()
                .sparkContext(sc)
                .sourceType("dummy")
                .toDate("dummy")
                .fromDate("dummy")
                .logPath("dummy")
                .rdd(eventsRdd)
                .alertingRules(isAlertRules)
                .maxResult(maxResult)
                .build();

        AlertingSparkResult result = job.eval();
        Assert.assertEquals(200, result.getMatchesTotal());
        Assert.assertEquals(200, result.getExceptionsTotal());
        Assert.assertEquals(maxResult, result.getMatches().size());
        Assert.assertEquals(maxResult, result.getExceptions().size());
    }

    @Test
    @Ignore
    public void trivialRddTestNoMatch() throws Exception {
        ArrayList<String> events = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            events.add(eventWithoutAlert);
        }
        JavaRDD<String> eventsRdd = sc.parallelize(events);
        job = new AlertingSparkJob.Builder()
                .sparkContext(sc)
                .sourceType("dummy")
                .toDate("dummy")
                .fromDate("dummy")
                .logPath("dummy")
                .rdd(eventsRdd)
                .alertingRules(isAlertRules)
                .maxResult(maxResult)
                .build();

        AlertingSparkResult result = job.eval();
        Assert.assertEquals(0, result.getMatchesTotal());
        Assert.assertEquals(0, result.getExceptionsTotal());
        Assert.assertEquals(0, result.getMatches().size());
        Assert.assertEquals(0, result.getExceptions().size());
    }

    @Test
    @Ignore
    public void hdfsRddTest() throws Exception {
        ArrayList<String> events = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            events.add(goodAlert);
            events.add("INVALID");
        }
        sc.parallelize(events);
        job = new AlertingSparkJob.Builder()
                .sparkContext(sc)
                .suffix("txt")
                .sourceType("dummy")
                .fromDate("2019-06-19")
                .toDate("2019-06-20")
                .logPath("e:/hdfs/apps/siembol/indexing/rotated/")
                .alertingRules(isAlertRules)
                .maxResult(maxResult)
                .build();

        AlertingSparkResult result = job.eval();
        Assert.assertFalse(result.isEmpty());
    }
}
