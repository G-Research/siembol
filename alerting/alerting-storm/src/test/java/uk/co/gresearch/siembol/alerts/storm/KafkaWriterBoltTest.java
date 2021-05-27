package uk.co.gresearch.siembol.alerts.storm;

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

import uk.co.gresearch.siembol.alerts.common.AlertingEngineType;
import uk.co.gresearch.siembol.common.constants.SiembolMessageFields;
import uk.co.gresearch.siembol.alerts.storm.model.AlertMessage;
import uk.co.gresearch.siembol.alerts.storm.model.AlertMessages;
import uk.co.gresearch.siembol.alerts.storm.model.ExceptionMessages;
import uk.co.gresearch.siembol.common.model.AlertingStormAttributesDto;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class KafkaWriterBoltTest {
    private static final ObjectReader JSON_PARSERS_CONFIG_READER = new ObjectMapper()
            .readerFor(AlertingStormAttributesDto.class);
    private static final ObjectReader JSON_MAP_READER = new ObjectMapper()
            .readerFor(new TypeReference<Map<String, Object>>() {});

    /**
     *{
     *   "alerts.engine": "siembol_alerts",
     *   "alerts.input.topics": [ "enrichmnents" ],
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
     *   }
     * }
     **/
    @Multiline
    public static String alertingStormConfig;

    /**
     * {
     *   "ip_src_addr": "1.2.3.4",
     *   "b": 1,
     *   "is_alert": "true",
     *   "source_type": "test",
     *   "detection_source": "alerts",
     *   "siembol_alerts_full_rule_name": "alert1_v1",
     *   "siembol_alerts_rule_name": "alert1",
     *   "siembol_alerts_max_per_day": 1,
     *   "siembol_alerts_max_per_hour": 1
     * }
     **/
    @Multiline
    public static String AlertMessageStr;

    /**
     * {
     *   "ip_src_addr": "1.2.3.4",
     *   "b": 1,
     *   "is_alert": "true",
     *   "source_type": "test",
     *   "detection_source": "alerts",
     *   "siembol_alerts_full_rule_name": "alert1_v1",
     *   "siembol_alerts_rule_name": "alert1",
     *   "siembol_alerts_max_per_day": 1,
     *   "siembol_alerts_max_per_hour": 1,
     *   "correlation_key" : "evil"
     * }
     **/
    @Multiline
    public static String AlertMessageCorrelationStr;


    @ClassRule
    public static KafkaJunitRule kafkaRule = new KafkaJunitRule(EphemeralKafkaBroker.create());

    private AlertingStormAttributesDto attributes;
    private KafkaWriterBolt writerBolt;
    private String bootstrapServer;
    private Tuple tuple;
    private OutputCollector collector;
    private AlertMessages AlertMessages;
    private ExceptionMessages exceptionMessages;
    private Map<String, Object> alertMap;

    @Before
    public void setUp() throws Exception {
        attributes = JSON_PARSERS_CONFIG_READER
                .readValue(alertingStormConfig);
        alertMap = JSON_MAP_READER.readValue(AlertMessageStr);
        bootstrapServer = String.format("127.0.0.1:%d", kafkaRule.helper().kafkaPort());
        attributes.getKafkaProducerProperties().getRawMap().put("bootstrap.servers", bootstrapServer);

        collector = Mockito.mock(OutputCollector.class);
        AlertMessages = new AlertMessages();
        exceptionMessages = new ExceptionMessages();
        tuple = Mockito.mock(Tuple.class);
        when(tuple.getValueByField(eq(TupleFieldNames.ALERTING_MATCHES.toString()))).thenReturn(AlertMessages);
        when(tuple.getValueByField(eq(TupleFieldNames.ALERTING_EXCEPTIONS.toString()))).thenReturn(exceptionMessages);

        kafkaRule.waitForStartup();
        writerBolt = new KafkaWriterBolt(attributes);
        writerBolt.prepare(null, null, collector);
    }

    @Test
    public void testAlertMessagesOK() throws Exception {
        AlertMessage alert = new AlertMessage(AlertingEngineType.SIEMBOL_ALERTS, alertMap, AlertMessageStr);
        AlertMessages.add(alert);
        writerBolt.execute(tuple);
        List<String> outputAlert= kafkaRule.helper().consumeStrings("alerts", 1)
                .get(10, TimeUnit.SECONDS);
        Assert.assertNotNull(outputAlert);
        Assert.assertEquals(1, outputAlert.size());
        Assert.assertEquals(AlertMessageStr.trim(), outputAlert.get(0).trim());
    }

    @Test
    public void testAlertMessageReachProtectionThreshold() throws Exception {
        AlertMessage alert = new AlertMessage(AlertingEngineType.SIEMBOL_ALERTS, alertMap, AlertMessageStr);
        AlertMessages.add(alert);
        AlertMessages.add(alert);
        writerBolt.execute(tuple);
        List<String> outputAlert = kafkaRule.helper().consumeStrings("alerts", 1)
                .get(10, TimeUnit.SECONDS);
        Assert.assertNotNull(outputAlert);
        Assert.assertEquals(1, outputAlert.size());
        Assert.assertEquals(AlertMessageStr.trim(), outputAlert.get(0).trim());

        List<String> outputExceptions = kafkaRule.helper().consumeStrings("errors", 1)
                .get(10, TimeUnit.SECONDS);
        Assert.assertNotNull(outputExceptions);
        Assert.assertEquals(1, outputExceptions.size());
        Map<String, Object> parsedException = JSON_MAP_READER.readValue(outputExceptions.get(0));
        Assert.assertEquals("siembol_alerts", parsedException.get("failed_sensor_type"));
        Assert.assertEquals("alerting_error", parsedException.get("error_type"));
        Assert.assertEquals("error", parsedException.get(SiembolMessageFields.SENSOR_TYPE.toString()));
        Assert.assertTrue(parsedException.get("message").toString().contains("The rule: alert1_v1 reaches the limit"));
    }

    @Test
    public void testAlertingExceptionsOK() throws Exception {
        exceptionMessages.add("dummy");
        writerBolt.execute(tuple);
        List<String> outputExceptions = kafkaRule.helper().consumeStrings("errors", 1)
                .get(10, TimeUnit.SECONDS);

        Assert.assertNotNull(outputExceptions);
        Assert.assertEquals(1, outputExceptions.size());
        Map<String, Object> parsedException = JSON_MAP_READER.readValue(outputExceptions.get(0));
        Assert.assertEquals("siembol_alerts", parsedException.get("failed_sensor_type"));
        Assert.assertEquals("alerting_error", parsedException.get("error_type"));
        Assert.assertEquals("error", parsedException.get(SiembolMessageFields.SENSOR_TYPE.toString()));
        Assert.assertEquals("dummy", parsedException.get("message"));
    }

    @Test
    public void testAlertingCorrelationAlertOK() throws Exception {
        alertMap = JSON_MAP_READER.readValue(AlertMessageCorrelationStr);
        AlertMessage alert = new AlertMessage(AlertingEngineType.SIEMBOL_ALERTS,
                alertMap,
                AlertMessageCorrelationStr);
        AlertMessages.add(alert);
        writerBolt.execute(tuple);
        List<String> outputAlert = kafkaRule.helper().consumeStrings("correlation.alerts", 1)
                .get(10, TimeUnit.SECONDS);
        Assert.assertNotNull(outputAlert);
        Assert.assertEquals(1, outputAlert.size());
        Assert.assertEquals(AlertMessageCorrelationStr.trim(), outputAlert.get(0).trim());
    }
}
