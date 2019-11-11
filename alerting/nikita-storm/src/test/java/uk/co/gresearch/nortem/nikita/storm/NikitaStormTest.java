package uk.co.gresearch.nortem.nikita.storm;

import com.salesforce.kafka.test.KafkaBrokers;
import com.salesforce.kafka.test.junit4.SharedKafkaTestResource;
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
import static org.apache.kafka.common.security.auth.SecurityProtocol.PLAINTEXT;

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

    @ClassRule
    public static final SharedKafkaTestResource sharedKafkaTestResource =
            new SharedKafkaTestResource()
                    .withBrokerProperty("auto.create.topics.enable", "true");

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

        KafkaBrokers brokers = sharedKafkaTestResource.getKafkaBrokers();

        StringBuilder sb = new StringBuilder();
        brokers.forEach(x -> {
            sb.append("127.0.0.1");
            sb.append(x.getConnectString().substring(x.getConnectString().lastIndexOf(':')));
        });

        NikitaStormAttributes attributes = new NikitaStormAttributes();
        attributes.setNikitaInputTopic("enrichmnents");
        attributes.setGroupId("nikita-test");
        attributes.setBootstrapServers(sb.toString());
        attributes.setSecurityProtocol(PLAINTEXT.toString());
        attributes.setNikitaOutputTopic("nortem.alerts");
        attributes.setNikitaCorrelationOutputTopic("correlation.alerts");
        attributes.setKafkaErrorTopic("errors");

        attributes.setKafkaSpoutNumExecutors(1);
        attributes.setNikitaEngineBoltNumExecutors(1);
        attributes.setKafkaWriterBoltNumExecutors(1);

        attributes.setSessionTimeoutMs(300000);
        attributes.setClientId("my_client_id");
        attributes.setZkUrl(zkServer.getConnectString());
        attributes.setZkPathNikitaRules("/nortem/rules");
        attributes.setZkBaseSleepMs(1000);
        attributes.setZkMaxRetries(10);
        attributes.setNikitaEngine("nikita");
        attributes.setFirstPollOffsetStrategy("UNCOMMITTED_LATEST");

        TestProducer testProducer = new TestProducer(attributes);

        StormTopology topology = NikitaStorm.createTopology(attributes);
        LocalCluster cluster = new LocalCluster();
        Config config = new Config();
        config.setDebug(true);
        config.put(Config.TOPOLOGY_MESSAGE_TIMEOUT_SECS, 5);

        cluster.submitTopology("KafkaStormSample", config, topology);
        TimeUnit.SECONDS.sleep(50);

        testProducer.sendMessage("INVALID");
        for (int i = 1; i < 102; i++) {
            testProducer.sendMessage(goodAlert);
        }

        zkClient.setData().forPath("/nortem/rules", isAlertRules2.getBytes(UTF_8));
        for (int i = 1; i < 102; i++) {
            testProducer.sendMessage(goodAlert);
        }

        zkClient.setData().forPath("/nortem/rules", "INVALID".getBytes(UTF_8));
        for (int i = 1; i < 102; i++) {
            testProducer.sendMessage(goodAlert);
        }

        for (int i = 1; i < 100; i++) {
            testProducer.sendMessage(goodEventNoAlert);
        }

        testProducer.sendMessage("INVALID");
        TimeUnit.MINUTES.sleep(100);
        zkClient.close();
        zkServer.close();
        System.exit(0);
    }
}

