package uk.co.gresearch.nortem.nikita.storm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.charithe.kafka.EphemeralKafkaBroker;
import com.github.charithe.kafka.KafkaJunitRule;
import org.adrianwalker.multilinestring.Multiline;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.test.TestingServer;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.generated.StormTopology;
import org.apache.zookeeper.CreateMode;
import org.junit.*;

import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;

public class NikitaStormTest {
    /**
     *{
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
     *  "rules_version" :2,
     *  "tags" : [ { "tag_name" : "detection:source", "tag_value" : "nikita" } ],
     *  "rules" : [ {
     *      "rule_name" : "test_rule",
     *      "rule_version" : 2,
     *      "rule_author" : "dummy",
     *      "rule_protection" : {
     *          "max_per_hour" : 100,
     *          "max_per_day" : 10000
     *      },
     *      "tags" : [ { "tag_name" : "correlation_key", "tag_value" : "${source.type}" }, { "tag_name" : "correlation_alert_visible", "tag_value" : "TruE" } ],
     *      "rule_description": "Test rule - is_alert is equal to true",
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
    public static String isAlertRules2;

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

    /**
     *{
     *  "dummy_field_int" : 1,
     *  "dummy_field_boolean" : false
     *}
     **/
    @Multiline
    public static String goodEventNoAlert;

    /**
     * {
     *   "nikita.engine": "nikita",
     *   "nikita.input.topic": "enrichmnents",
     *   "nikita.correlation.output.topic": "correlation.alerts",
     *   "kafka.error.topic": "errors",
     *   "nikita.output.topic": "nortem.alerts",
     *   "storm.attributes": {
     *     "first.pool.offset.strategy": "UNCOMMITTED_LATEST",
     *     "kafka.spout.properties": {
     *       "group.id": "nikita.reader",
     *       "security.protocol": "PLAINTEXT"
     *     }
     *   },
     *   "kafka.spout.num.executors": 1,
     *   "nikita.engine.bolt.num.executors": 1,
     *   "kafka.writer.bolt.num.executors": 1,
     *   "kafka.producer.properties": {
     *     "compression.type": "snappy",
     *     "security.protocol": "PLAINTEXT",
     *     "client.id": "test_producer"
     *   },
     *   "zookeeper.attributes": {
     *     "zk.path": "/nortem/rules",
     *     "zk.base.sleep.ms": 1000,
     *     "zk.max.retries": 10
     *   }
     * }
     **/
    @Multiline
    public static String testConfig;

    @ClassRule
    public static KafkaJunitRule kafkaRule = new KafkaJunitRule(EphemeralKafkaBroker.create());

    @Ignore
    @Test
    public void initTestProducer() throws Exception {
        TestingServer zkServer = new TestingServer(6666, true);

        CuratorFramework zkClient = CuratorFrameworkFactory
                .newClient(zkServer.getConnectString(), new RetryNTimes(3, 1000));
        zkClient.start();

        if (zkClient.checkExists().forPath("/nortem/rules") == null) {
            zkClient.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .forPath("/nortem/rules", isAlertRules.getBytes(UTF_8));
        }

        zkClient.setData().forPath("/nortem/rules", isAlertRules.getBytes(UTF_8));
        String bootstrapServer = String.format("127.0.0.1:%d", kafkaRule.helper().kafkaPort());

        NikitaStormAttributes attributes = new ObjectMapper()
                .readerFor(NikitaStormAttributes.class)
                .readValue(testConfig);

        attributes.getKafkaProducerProperties().put("bootstrap.servers", bootstrapServer);
        attributes.getStormAttributes().setBootstrapServers(bootstrapServer);
        attributes.getZookeperAttributes().setZkUrl(zkServer.getConnectString());

        StormTopology topology = NikitaStorm.createTopology(attributes);
        LocalCluster cluster = new LocalCluster();
        Config config = new Config();
        config.setDebug(true);
        config.put(Config.TOPOLOGY_MESSAGE_TIMEOUT_SECS, 5);

        cluster.submitTopology("KafkaStormSample", config, topology);
        TimeUnit.SECONDS.sleep(50);

        kafkaRule.helper().produceStrings("enrichmnents", "INVALID");
        for (int i = 1; i < 102; i++) {
            kafkaRule.helper().produceStrings("enrichmnents", goodAlert);
        }

        zkClient.setData().forPath("/nortem/rules", isAlertRules2.getBytes(UTF_8));
        for (int i = 1; i < 102; i++) {
            kafkaRule.helper().produceStrings("enrichmnents", goodAlert);
        }

        zkClient.setData().forPath("/nortem/rules", "INVALID".getBytes(UTF_8));
        for (int i = 1; i < 102; i++) {
            kafkaRule.helper().produceStrings("enrichmnents", goodAlert);
        }

        for (int i = 1; i < 100; i++) {
            kafkaRule.helper().produceStrings("enrichmnents", goodEventNoAlert);
        }

        kafkaRule.helper().produceStrings("enrichmnents", "INVALID");
        TimeUnit.MINUTES.sleep(100);
        zkClient.close();
        zkServer.close();
        System.exit(0);
    }
}