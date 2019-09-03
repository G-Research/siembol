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
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.admin.AdminClientConfig.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.CLIENT_ID_CONFIG;
import static org.apache.kafka.common.security.auth.SecurityProtocol.PLAINTEXT;
import static uk.co.gresearch.nortem.nikita.storm.NikitaEngineBolt.*;
import static uk.co.gresearch.nortem.nikita.storm.NikitaStorm.*;

public class NikitaCorrelationTest {
    /**
     *{
     *   "rules_version" :1,
     *   "tags" : [{
     *      "tag_name" : "detection:source",
     *      "tag_value" : "nikita_correlation"
     *   }],
     *   "rules_protection": {
     *      "max_per_hour": 1,
     *      "max_per_day": 2
     *   },
     *   "rules" : [
     *   {
     *      "rule_description": "Dummy testing rule",
     *      "rule_version": 1,
     *      "rule_name": "fake_rule",
     *      "rule_author": "dummy",
     *      "rule_protection": {
     *          "max_per_hour": 30,
     *          "max_per_day": 100
     *      },
     *      "tags": [
     *      {
     *          "tag_name": "test",
     *          "tag_value": "thing"
     *     }],
     *     "correlation_attributes" : {
     *          "time_unit" : "minutes",
     *          "time_window" : 1,
     *          "time_computation_type" : "event_time",
     *          "max_time_lag_in_sec": 30,
     *          "alerts" : [{
     * 			    "alert" : "alert1",
     * 			    "threshold" : 5
     * 			}]
     *      }
     *   }]
     * }
     **/
    @Multiline
    public static String dummyRule;

    /**
     *{
     *  "nikita:full_rule_name" : "alert1_v1",
     *  "nikita:rule_name" : "alert1",
     *  "nikita:max_per_day" : 1000,
     *  "nikita:max_per_hour" : 200,
     *  "correlation_key": "A",
     *  "dummy_field_boolean" : false
     *}
     **/
    @Multiline
    public static String alertKeyA;

    /**
     *{
     *  "nikita:full_rule_name" : "alert1_v1",
     *  "nikita:rule_name" : "alert1",
     *  "nikita:max_per_day" : 1000,
     *  "nikita:max_per_hour" : 200,
     *  "correlation_key": "B",
     *  "dummy_field_boolean" : false
     *}
     **/
    @Multiline
    public static String alertKeyB;

    /**
     *{
     *  "nikita:full_rule_name" : "alert1_v1",
     *  "nikita:rule_name" : "alert1",
     *  "nikita:max_per_day" : 1000,
     *  "nikita:max_per_hour" : 200,
     *  "correlation_key": "C",
     *  "dummy_field_boolean" : false
     *}
     **/
    @Multiline
    public static String alertKeyC;

    /**
     *{
     *  "nikita:full_rule_name" : "alert1_v1",
     *  "nikita:rule_name" : "alert1",
     *  "nikita:max_per_day" : 1000,
     *  "nikita:max_per_hour" : 200,
     *  "correlation_key": "D",
     *  "dummy_field_boolean" : false
     *}
     **/
    @Multiline
    public static String alertKeyD;

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

        if (zkClient.checkExists().forPath("/correlated") == null) {
            zkClient.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .forPath("/correlated", dummyRule.getBytes(UTF_8));
        }

        zkClient.setData().forPath("/correlated", dummyRule.getBytes(UTF_8));
        KafkaBrokers brokers = sharedKafkaTestResource.getKafkaBrokers();

        StringBuilder sb = new StringBuilder();
        brokers.forEach(x -> {
            sb.append("127.0.0.1");
            sb.append(x.getConnectString().substring(x.getConnectString().lastIndexOf(':')));
        });

        NikitaStormAttributes attributes = new NikitaStormAttributes();
        attributes.setNikitaInputTopic("nortem.correlation.alerts");
        attributes.setGroupId("nikita-test");
        attributes.setBootstrapServers(sb.toString());
        attributes.setSecurityProtocol(PLAINTEXT.toString());
        attributes.setNikitaOutputTopic("nortem.alerts");
        attributes.setNikitaCorrelationOutputTopic("nortem.correlation.alerts");
        attributes.setKafkaErrorTopic("errors");

        attributes.setKafkaSpoutNumExecutors(1);
        attributes.setNikitaEngineBoltNumExecutors(3);
        attributes.setKafkaWriterBoltNumExecutors(1);

        attributes.setSessionTimeoutMs(300000);
        attributes.setClientId("my_client_id");
        attributes.setZkUrl(zkServer.getConnectString());
        attributes.setZkPathNikitaRules("/correlated");
        attributes.setZkBaseSleepMs(1000);
        attributes.setZkMaxRetries(10);
        attributes.setNikitaEngine("nikita-correlation");

        attributes.setNikitaEngineCleanIntervalSec(100);
        TestProducer testProducer = new TestProducer(attributes);

        StormTopology topology = NikitaStorm.createNikitaCorrelationTopology(attributes);
        LocalCluster cluster = new LocalCluster();
        Config config = new Config();
        config.setDebug(true);
        config.put(Config.TOPOLOGY_MESSAGE_TIMEOUT_SECS, 5);

        cluster.submitTopology("nikita-correlation", config, topology);
        TimeUnit.SECONDS.sleep(50);

        for (int i = 1; i < 3; i++) {
            testProducer.sendMessage("A", alertKeyA);
        }

        for (int i = 1; i < 3; i++) {
            testProducer.sendMessage("D", alertKeyD);
        }

        for (int i = 1; i < 3; i++) {
            testProducer.sendMessage("B", alertKeyB);
        }

        for (int i = 1; i < 3; i++) {
            testProducer.sendMessage("C", alertKeyC);
        }

        for (int i = 1; i < 3; i++) {
            testProducer.sendMessage("B", alertKeyB);
        }

        for (int i = 1; i < 3; i++) {
            testProducer.sendMessage("C", alertKeyC);
        }

        for (int i = 1; i < 3; i++) {
            testProducer.sendMessage("A", alertKeyA);
        }

        for (int i = 1; i < 3; i++) {
            testProducer.sendMessage("D", alertKeyD);
        }

        testProducer.sendMessage("INVALID");
        TimeUnit.MINUTES.sleep(100);
        zkClient.close();
        zkServer.close();
        System.exit(0);

    }
}