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
import uk.co.gresearch.siembol.common.zookeper.ZookeperConnector;
import uk.co.gresearch.siembol.common.zookeper.ZookeperConnectorFactory;
import uk.co.gresearch.siembol.alerts.common.AlertingFields;
import uk.co.gresearch.siembol.alerts.storm.model.AlertingStormAttributes;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

public class CorrelationAlertingTest {
    private static final ObjectReader JSON_PARSERS_CONFIG_READER = new ObjectMapper()
            .readerFor(AlertingStormAttributes.class);
    private static ObjectReader JSON_READER = new ObjectMapper()
            .readerFor(new TypeReference<Map<String, Object>>() {});

    /**
     * {
     *   "siembol_alerts_full_rule_name": "alert1_v3",
     *   "siembol_alerts_rule_name": "alert1",
     *   "correlation_key": "evil",
     *   "siembol_alerts_max_per_hour": 200,
     *   "siembol_alerts_test": "true",
     *   "source_type": "a",
     *   "siembol_alerts_max_per_day": 10000
     * }
     **/
    @Multiline
    public static String alert1;

    /**
     * {
     *   "siembol_alerts_full_rule_name": "alert1_v3",
     *   "siembol_alerts_rule_name": "alert2",
     *   "correlation_key": "evil",
     *   "sensor": "a",
     *   "siembol_alerts_max_per_hour": 200,
     *   "siembol_alerts_test": "true",
     *   "source_type": "a",
     *   "siembol_alerts_max_per_day": 10000
     * }
     **/
    @Multiline
    public static String alert2;


    /**
     * {
     *   "rules_version": 1,
     *   "tags": [
     *     {
     *       "tag_name": "detection_source",
     *       "tag_value": "siembol_correlation_alerts"
     *     }
     *   ],
     *   "rules": [
     *     {
     *       "tags": [
     *         {
     *           "tag_name": "test",
     *           "tag_value": "true"
     *         }
     *       ],
     *       "rule_protection": {
     *         "max_per_hour": 500,
     *         "max_per_day": 1000
     *       },
     *       "rule_name": "test_rule",
     *       "rule_version": 1,
     *       "rule_author": "dummy",
     *       "rule_description": "Testing rule",
     *       "correlation_attributes": {
     *         "time_unit": "seconds",
     *         "time_window": 500,
     *         "time_computation_type": "processing_time",
     *         "alerts": [
     *           {
     *             "alert": "alert1",
     *             "threshold": 2
     *           },
     *           {
     *             "alert": "alert2",
     *             "threshold": 1
     *           }
     *         ]
     *       }
     *     }
     *   ]
     * }
     *}
     **/
    @Multiline
    public static String simpleCorrelationRules;


    /**
     * {
     *   "alerts.engine": "siembol_correlation_alerts",
     *   "alerts.input.topic": "input",
     *   "alerts.correlation.output.topic": "correlation.alerts",
     *   "kafka.error.topic": "errors",
     *   "alerts.output.topic": "alerts",
     *   "alerts.engine.clean.interval.sec" : 2,
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

    private ZookeperConnector rulesZookeperConnector;
    private ZookeperConnectorFactory zookeperConnectorFactory;
    private AlertingStormAttributes alertingStormAttributes;
    private StormTopology topology;

    @Before
    public void setUp() throws Exception {
        alertingStormAttributes = JSON_PARSERS_CONFIG_READER
                .readValue(testConfig);
        zookeperConnectorFactory = Mockito.mock(ZookeperConnectorFactory.class, withSettings().serializable());

        rulesZookeperConnector = Mockito.mock(ZookeperConnector.class, withSettings().serializable());
        when(zookeperConnectorFactory.createZookeperConnector(alertingStormAttributes.getZookeperAttributes()))
                .thenReturn(rulesZookeperConnector);
        when(rulesZookeperConnector.getData()).thenReturn(simpleCorrelationRules);

        String bootstrapServer = String.format("127.0.0.1:%d", kafkaRule.helper().kafkaPort());
        alertingStormAttributes.getStormAttributes().setBootstrapServers(bootstrapServer);
        alertingStormAttributes.getKafkaProducerProperties()
                .put("bootstrap.servers", bootstrapServer);

        kafkaRule.waitForStartup();
        topology = AlertingStorm.createCorrelationAlertingTopology(alertingStormAttributes, zookeperConnectorFactory);
        LocalCluster cluster = new LocalCluster();
        Config config = new Config();
        config.put(Config.TOPOLOGY_DEBUG, true);
        config.put(Config.TOPOLOGY_MESSAGE_TIMEOUT_SECS, 50);
        cluster.submitTopology("test", config, topology);
    }

    @Test
    public void integrationTest() throws Exception {
        kafkaRule.helper().produceStrings("input", alert1.trim());
        kafkaRule.helper().produceStrings("input", alert2.trim());
        kafkaRule.helper().produceStrings("input", alert1.trim());

        List<String> outputEvent = kafkaRule.helper().consumeStrings("alerts", 1)
                .get(10, TimeUnit.SECONDS);
        Assert.assertNotNull(outputEvent);
        Assert.assertEquals(1, outputEvent.size());
        Map<String, Object> alert = JSON_READER.readValue(outputEvent.get(0));
        Assert.assertEquals("test_rule_v1", alert.get(AlertingFields.FULL_RULE_NAME.getCorrelationAlertingName()));
        Assert.assertEquals(1000, alert.get(AlertingFields.MAX_PER_DAY_FIELD.getCorrelationAlertingName()));
        Assert.assertEquals(500, alert.get(AlertingFields.MAX_PER_HOUR_FIELD.getCorrelationAlertingName()));
        Assert.assertEquals("a", alert.get(SiembolMessageFields.SENSOR_TYPE.toString()));

        kafkaRule.helper().produceStrings("input", "INVALID");
        List<String> errors = kafkaRule.helper().consumeStrings("errors", 1)
                .get(10, TimeUnit.SECONDS);
        Map<String, Object> error = JSON_READER.readValue(errors.get(0));
        Assert.assertNotNull(error);

        Assert.assertEquals("siembol_correlation_alerts", error.get("failed_sensor_type"));
        Assert.assertEquals("alerting_error", error.get("error_type"));
        Assert.assertEquals("error", error.get(SiembolMessageFields.SENSOR_TYPE.toString()));
        Assert.assertTrue(error.get("message").toString().contains("JsonParseException"));
    }
}