package uk.co.gresearch.siembol.alerts.storm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.github.charithe.kafka.EphemeralKafkaBroker;
import com.github.charithe.kafka.KafkaJunitRule;
import org.adrianwalker.multilinestring.Multiline;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.generated.StormTopology;
import org.junit.*;
import org.mockito.Mockito;
import uk.co.gresearch.siembol.common.constants.SiembolMessageFields;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnector;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnectorFactory;
import uk.co.gresearch.siembol.alerts.common.AlertingFields;
import uk.co.gresearch.siembol.common.model.AlertingStormAttributesDto;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

public class AlertingStormApplicationTest {
    private static final ObjectReader JSON_PARSERS_CONFIG_READER = new ObjectMapper()
            .readerFor(AlertingStormAttributesDto.class);
    private static ObjectReader JSON_READER = new ObjectMapper()
            .readerFor(new TypeReference<Map<String, Object>>() {});

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
    private static String testRules;

    /**
     *{
     *  "source_type" : "secret",
     *  "is_alert" : "TruE",
     *  "dummy_field_int" : 1,
     *  "dummy_field_boolean" : false
     *}
     **/
    @Multiline
    private static String goodAlert;


    /**
     * {
     *   "alerts.engine": "siembol_alerts",
     *   "alerts.input.topics": [ "input" ],
     *   "alerts.correlation.output.topic": "correlation.alerts",
     *   "kafka.error.topic": "errors",
     *   "alerts.output.topic": "alerts",
     *   "storm.attributes": {
     *     "first.pool.offset.strategy": "EARLIEST",
     *     "kafka.spout.properties": {
     *       "group.id": "alerts.reader",
     *       "security.protocol": "PLAINTEXT"
     *     }
     *   },
     *   "kafka.spout.num.executors": 1,
     *   "alerts.engine.bolt.num.executors": 1,
     *   "kafka.writer.bolt.num.executors": 1,
     *   "kafka.producer.properties": {
     *     "compression.type": "snappy",
     *     "security.protocol": "PLAINTEXT",
     *     "client.id": "test_producer"
     *   },
     *   "zookeeper.attributes": {
     *     "zk.path": "rules",
     *     "zk.base.sleep.ms": 1000,
     *     "zk.max.retries": 10
     *   }
     * }
     **/
    @Multiline
    public static String testConfig;

    @ClassRule
    public static KafkaJunitRule kafkaRule = new KafkaJunitRule(EphemeralKafkaBroker.create());

    private ZooKeeperConnector rulesZooKeeperConnector;
    private ZooKeeperConnectorFactory zooKeeperConnectorFactory;
    private AlertingStormAttributesDto alertingStormAttributes;
    private StormTopology topology;

    @Before
    public void setUp() throws Exception {
        alertingStormAttributes = JSON_PARSERS_CONFIG_READER
                .readValue(testConfig);
        zooKeeperConnectorFactory = Mockito.mock(ZooKeeperConnectorFactory.class, withSettings().serializable());

        rulesZooKeeperConnector = Mockito.mock(ZooKeeperConnector.class, withSettings().serializable());
        when(zooKeeperConnectorFactory.createZookeeperConnector(alertingStormAttributes.getZookeperAttributes()))
                .thenReturn(rulesZooKeeperConnector);
        when(rulesZooKeeperConnector.getData()).thenReturn(testRules);

        String bootstrapServer = String.format("127.0.0.1:%d", kafkaRule.helper().kafkaPort());
        alertingStormAttributes.getStormAttributes().setBootstrapServers(bootstrapServer);
        alertingStormAttributes.getKafkaProducerProperties().getRawMap()
                .put("bootstrap.servers", bootstrapServer);

        kafkaRule.waitForStartup();
        topology = AlertingStorm.createTopology(alertingStormAttributes, zooKeeperConnectorFactory);
        LocalCluster cluster = new LocalCluster();
        Config config = new Config();
        config.put(Config.TOPOLOGY_DEBUG, true);
        config.put(Config.TOPOLOGY_MESSAGE_TIMEOUT_SECS, 50);
        cluster.submitTopology("test", config, topology);
    }

    @Test(timeout=200000)
    public void integrationTest() throws Exception {
        kafkaRule.helper().produceStrings("input", goodAlert.trim());
        List<String> outputEvent = kafkaRule.helper().consumeStrings("alerts", 1)
                .get(10, TimeUnit.SECONDS);
        Assert.assertNotNull(outputEvent);
        Assert.assertEquals(1, outputEvent.size());
        Map<String, Object> alert = JSON_READER.readValue(outputEvent.get(0));
        Assert.assertEquals("test_rule_v1", alert.get(AlertingFields.FULL_RULE_NAME.getAlertingName()));
        Assert.assertEquals(10000, alert.get(AlertingFields.MAX_PER_DAY_FIELD.getAlertingName()));
        Assert.assertEquals(100, alert.get(AlertingFields.MAX_PER_HOUR_FIELD.getAlertingName()));
        Assert.assertEquals("secret", alert.get(SiembolMessageFields.SENSOR_TYPE.toString()));
        Assert.assertEquals(1, alert.get("dummy_field_int"));
        Assert.assertEquals(false, alert.get("dummy_field_boolean"));

        kafkaRule.helper().produceStrings("input", "INVALID");
        List<String> errors = kafkaRule.helper().consumeStrings("errors", 1)
                .get(10, TimeUnit.SECONDS);
        Map<String, Object> error = JSON_READER.readValue(errors.get(0));
        Assert.assertNotNull(error);

        Assert.assertEquals("siembol_alerts", error.get("failed_sensor_type"));
        Assert.assertEquals("alerting_error", error.get("error_type"));
        Assert.assertEquals("error", error.get(SiembolMessageFields.SENSOR_TYPE.toString()));
        Assert.assertTrue(error.get("message").toString().contains("JsonParseException"));
    }
}