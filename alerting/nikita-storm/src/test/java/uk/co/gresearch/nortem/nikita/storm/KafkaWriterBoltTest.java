package uk.co.gresearch.nortem.nikita.storm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.github.charithe.kafka.EphemeralKafkaBroker;
import com.github.charithe.kafka.KafkaJunitRule;
import org.adrianwalker.multilinestring.Multiline;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.tuple.Tuple;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;

import uk.co.gresearch.nortem.common.constants.NortemMessageFields;
import uk.co.gresearch.nortem.nikita.storm.model.NikitaAlert;
import uk.co.gresearch.nortem.nikita.storm.model.NikitaAlerts;
import uk.co.gresearch.nortem.nikita.storm.model.NikitaExceptions;
import uk.co.gresearch.nortem.nikita.storm.model.NikitaStormAttributes;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class KafkaWriterBoltTest {
    private static final ObjectReader JSON_PARSERS_CONFIG_READER = new ObjectMapper()
            .readerFor(NikitaStormAttributes.class);
    private static final ObjectReader JSON_MAP_READER = new ObjectMapper()
            .readerFor(new TypeReference<Map<String, Object>>() {});

    /**
     *{
     *   "nikita.engine": "nikita",
     *   "nikita.input.topic": "enrichmnents",
     *   "nikita.correlation.output.topic": "correlation.alerts",
     *   "kafka.error.topic": "errors",
     *   "nikita.output.topic": "alerts",
     *   "storm.attributes": {
     *     "first.pool.offset.strategy": "EARLIEST",
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
     *   }
     * }
     **/
    @Multiline
    public static String nikitaStormConfig;

    /**
     * {
     *   "ip_src_addr": "1.2.3.4",
     *   "b": 1,
     *   "is_alert": "true",
     *   "source.type": "test",
     *   "detection:source": "nikita",
     *   "nikita:full_rule_name": "alert1_v1",
     *   "nikita:rule_name": "alert1",
     *   "nikita:max_per_day": 1,
     *   "nikita:max_per_hour": 1
     * }
     **/
    @Multiline
    public static String nikitaAlertStr;

    /**
     * {
     *   "ip_src_addr": "1.2.3.4",
     *   "b": 1,
     *   "is_alert": "true",
     *   "source.type": "test",
     *   "detection:source": "nikita",
     *   "nikita:full_rule_name": "alert1_v1",
     *   "nikita:rule_name": "alert1",
     *   "nikita:max_per_day": 1,
     *   "nikita:max_per_hour": 1,
     *   "correlation_key" : "evil"
     * }
     **/
    @Multiline
    public static String nikitaAlertCorrelationStr;


    @ClassRule
    public static KafkaJunitRule kafkaRule = new KafkaJunitRule(EphemeralKafkaBroker.create());

    private NikitaStormAttributes attributes;
    private KafkaWriterBolt writerBolt;
    private String bootstrapServer;
    private Tuple tuple;
    private OutputCollector collector;
    private NikitaAlerts nikitaAlerts;
    private NikitaExceptions nikitaExceptions;
    private Map<String, Object> alertMap;

    @Before
    public void setUp() throws Exception {
        attributes = JSON_PARSERS_CONFIG_READER
                .readValue(nikitaStormConfig);
        alertMap = JSON_MAP_READER.readValue(nikitaAlertStr);
        bootstrapServer = String.format("127.0.0.1:%d", kafkaRule.helper().kafkaPort());
        attributes.getKafkaProducerProperties().put("bootstrap.servers", bootstrapServer);

        collector = Mockito.mock(OutputCollector.class);
        nikitaAlerts = new NikitaAlerts();
        nikitaExceptions = new NikitaExceptions();
        tuple = Mockito.mock(Tuple.class);
        when(tuple.getValueByField(eq(TupleFieldNames.NIKITA_MATCHES.toString()))).thenReturn(nikitaAlerts);
        when(tuple.getValueByField(eq(TupleFieldNames.NIKITA_EXCEPTIONS.toString()))).thenReturn(nikitaExceptions);

        kafkaRule.waitForStartup();
        writerBolt = new KafkaWriterBolt(attributes);
        writerBolt.prepare(null, null, collector);
    }

    @Test
    public void testNikitaAlertsOK() throws Exception {
        NikitaAlert alert = new NikitaAlert(alertMap, nikitaAlertStr);
        nikitaAlerts.add(alert);
        writerBolt.execute(tuple);
        List<String> outputAlert = kafkaRule.helper().consumeStrings("alerts", 1)
                .get(10, TimeUnit.SECONDS);
        Assert.assertNotNull(outputAlert);
        Assert.assertEquals(1, outputAlert.size());
        Assert.assertEquals(nikitaAlertStr.trim(), outputAlert.get(0).trim());
    }

    @Test
    public void testNikitaAlertReachProtectionThreshold() throws Exception {
        NikitaAlert alert = new NikitaAlert(alertMap, nikitaAlertStr);
        nikitaAlerts.add(alert);
        nikitaAlerts.add(alert);
        writerBolt.execute(tuple);
        List<String> outputAlert = kafkaRule.helper().consumeStrings("alerts", 1)
                .get(10, TimeUnit.SECONDS);
        Assert.assertNotNull(outputAlert);
        Assert.assertEquals(1, outputAlert.size());
        Assert.assertEquals(nikitaAlertStr.trim(), outputAlert.get(0).trim());

        List<String> outputExceptions = kafkaRule.helper().consumeStrings("errors", 1)
                .get(10, TimeUnit.SECONDS);
        Assert.assertNotNull(outputExceptions);
        Assert.assertEquals(1, outputExceptions.size());
        Map<String, Object> parsedException = JSON_MAP_READER.readValue(outputExceptions.get(0));
        Assert.assertEquals("nikita", parsedException.get("failed_sensor_type"));
        Assert.assertEquals("alerting_error", parsedException.get("error_type"));
        Assert.assertEquals("error", parsedException.get(NortemMessageFields.SENSOR_TYPE.toString()));
        Assert.assertTrue(parsedException.get("message").toString().contains("The rule: alert1_v1 reaches the limit"));
    }

    @Test
    public void testNikitaExceptionsOK() throws Exception {
        nikitaExceptions.add("dummy");
        writerBolt.execute(tuple);
        List<String> outputExceptions = kafkaRule.helper().consumeStrings("errors", 1)
                .get(10, TimeUnit.SECONDS);

        Assert.assertNotNull(outputExceptions);
        Assert.assertEquals(1, outputExceptions.size());
        Map<String, Object> parsedException = JSON_MAP_READER.readValue(outputExceptions.get(0));
        Assert.assertEquals("nikita", parsedException.get("failed_sensor_type"));
        Assert.assertEquals("alerting_error", parsedException.get("error_type"));
        Assert.assertEquals("error", parsedException.get(NortemMessageFields.SENSOR_TYPE.toString()));
        Assert.assertEquals("dummy", parsedException.get("message"));
    }

    @Test
    public void testNikitaCorrelationAlertOK() throws Exception {
        alertMap = JSON_MAP_READER.readValue(nikitaAlertCorrelationStr);
        NikitaAlert alert = new NikitaAlert(alertMap, nikitaAlertCorrelationStr);
        nikitaAlerts.add(alert);
        writerBolt.execute(tuple);
        List<String> outputAlert = kafkaRule.helper().consumeStrings("correlation.alerts", 1)
                .get(10, TimeUnit.SECONDS);
        Assert.assertNotNull(outputAlert);
        Assert.assertEquals(1, outputAlert.size());
        Assert.assertEquals(nikitaAlertCorrelationStr.trim(), outputAlert.get(0).trim());
    }
}
