package uk.co.gresearch.nortem.nikita.storm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.charithe.kafka.EphemeralKafkaBroker;
import com.github.charithe.kafka.KafkaJunitRule;
import org.adrianwalker.multilinestring.Multiline;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.test.TestingServer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.generated.StormTopology;
import org.apache.zookeeper.CreateMode;
import org.junit.*;

import java.util.Properties;
import java.util.concurrent.TimeUnit;
import static java.nio.charset.StandardCharsets.UTF_8;

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

    /**
     * {
     *   "nikita.engine": "nikita-correlation",
     *   "nikita.input.topic": "correlation.alerts",
     *   "nikita.correlation.output.topic": "nortem.alerts",
     *   "kafka.error.topic": "errors",
     *   "nikita.output.topic": "nortem.alerts",
     *   "nikita.engine.clean.interval.sec" : 100,
     *   "storm.attributes": {
     *     "first.pool.offset.strategy": "UNCOMMITTED_LATEST",
     *     "kafka.spout.properties": {
     *       "group.id": "nikita.correlation.reader",
     *       "security.protocol": "PLAINTEXT"
     *     }
     *   },
     *   "kafka.spout.num.executors": 1,
     *   "nikita.engine.bolt.num.executors": 3,
     *   "kafka.writer.bolt.num.executors": 1,
     *   "kafka.producer.properties": {
     *     "compression.type": "snappy",
     *     "security.protocol": "PLAINTEXT",
     *     "client.id": "test_producer"
     *   },
     *   "zookeeper.attributes": {
     *     "zk.path": "/correlated",
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

        if (zkClient.checkExists().forPath("/correlated") == null) {
            zkClient.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .forPath("/correlated", dummyRule.getBytes(UTF_8));
        }

        zkClient.setData().forPath("/correlated", dummyRule.getBytes(UTF_8));

        String bootstrapServer = String.format("127.0.0.1:%d", kafkaRule.helper().kafkaPort());
        NikitaStormAttributes attributes = new ObjectMapper()
                .readerFor(NikitaStormAttributes.class)
                .readValue(testConfig);

        attributes.getKafkaProducerProperties().put("bootstrap.servers", bootstrapServer);
        attributes.getStormAttributes().setBootstrapServers(bootstrapServer);
        attributes.getZookeperAttributes().setZkUrl(zkServer.getConnectString());


        StormTopology topology = NikitaStorm.createNikitaCorrelationTopology(attributes);
        LocalCluster cluster = new LocalCluster();
        Config config = new Config();
        config.setDebug(true);
        config.put(Config.TOPOLOGY_MESSAGE_TIMEOUT_SECS, 5);

        cluster.submitTopology("nikita-correlation", config, topology);
        TimeUnit.SECONDS.sleep(50);
        KafkaProducer<String, String> testProducer = kafkaRule.helper().createProducer(new StringSerializer(),
                new StringSerializer(),
                new Properties());

        for (int i = 1; i < 3; i++) {
            testProducer.send(new ProducerRecord<>("correlation.alerts","A", alertKeyA));
        }

        for (int i = 1; i < 3; i++) {
            testProducer.send(new ProducerRecord<>("correlation.alerts","D", alertKeyD));
        }

        for (int i = 1; i < 3; i++) {
            testProducer.send(new ProducerRecord<>("correlation.alerts","B", alertKeyB));
        }

        for (int i = 1; i < 3; i++) {
            testProducer.send(new ProducerRecord<>("correlation.alerts","C", alertKeyC));
        }

        for (int i = 1; i < 3; i++) {
            testProducer.send(new ProducerRecord<>("correlation.alerts","B", alertKeyB));
        }

        for (int i = 1; i < 3; i++) {
            testProducer.send(new ProducerRecord<>("correlation.alerts","C", alertKeyC));
        }

        for (int i = 1; i < 3; i++) {
            testProducer.send(new ProducerRecord<>("correlation.alerts","A", alertKeyA));
        }

        for (int i = 1; i < 3; i++) {
            testProducer.send(new ProducerRecord<>("correlation.alerts","D", alertKeyD));
        }

        testProducer.send(new ProducerRecord<>("correlation.alerts","INVALID"));
        TimeUnit.MINUTES.sleep(100);
        zkClient.close();
        zkServer.close();
        System.exit(0);
    }
}