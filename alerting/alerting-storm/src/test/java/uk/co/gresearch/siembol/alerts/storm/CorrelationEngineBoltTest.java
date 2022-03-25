package uk.co.gresearch.siembol.alerts.storm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import uk.co.gresearch.siembol.common.constants.SiembolMessageFields;
import uk.co.gresearch.siembol.common.metrics.SiembolMetrics;
import uk.co.gresearch.siembol.common.metrics.test.StormMetricsTestRegistrarFactoryImpl;
import uk.co.gresearch.siembol.common.model.ZooKeeperAttributesDto;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperCompositeConnector;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperCompositeConnectorFactory;
import uk.co.gresearch.siembol.alerts.common.AlertingFields;
import uk.co.gresearch.siembol.alerts.common.AlertingTags;
import uk.co.gresearch.siembol.alerts.storm.model.AlertMessages;
import uk.co.gresearch.siembol.alerts.storm.model.ExceptionMessages;
import uk.co.gresearch.siembol.common.model.AlertingStormAttributesDto;

import java.io.IOException;
import java.util.*;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class CorrelationEngineBoltTest {
    private static final ObjectReader JSON_READER = new ObjectMapper()
            .readerFor(new TypeReference<Map<String, Object>>() {
            });

    private final String alert1 = """
            {
              "siembol_alerts_full_rule_name": "alert1_v3",
              "siembol_alerts_rule_name": "alert1",
              "correlation_key": "evil",
              "siembol_alerts_max_per_hour": 200,
              "siembol_alerts_test": "true",
              "source_type": "a",
              "siembol_alerts_max_per_day": 10000
            }
            """;

    private final String alert2 = """
            {
              "siembol_alerts_full_rule_name": "alert1_v3",
              "siembol_alerts_rule_name": "alert2",
              "correlation_key": "evil",
              "sensor": "a",
              "siembol_alerts_max_per_hour": 200,
              "siembol_alerts_test": "true",
              "source_type": "a",
              "siembol_alerts_max_per_day": 10000
            }
            """;

    private final String simpleCorrelationRules = """
             {
               "rules_version": 1,
               "tags": [
                 {
                   "tag_name": "detection_source",
                   "tag_value": "siembol_correlation_alerts_instance"
                 }
               ],
               "rules": [
                 {
                   "tags": [
                     {
                       "tag_name": "test",
                       "tag_value": "true"
                     }
                   ],
                   "rule_protection": {
                     "max_per_hour": 500,
                     "max_per_day": 1000
                   },
                   "rule_name": "test_rule",
                   "rule_version": 1,
                   "rule_author": "dummy",
                   "rule_description": "Testing rule",
                   "correlation_attributes": {
                     "time_unit": "seconds",
                     "time_window": 500,
                     "time_computation_type": "processing_time",
                     "alerts": [
                       {
                         "alert": "alert1",
                         "threshold": 2
                       },
                       {
                         "alert": "alert2",
                         "threshold": 1
                       }
                     ]
                   }
                 }
               ]
             }
            }
            """;


    private Tuple tuple;
    private OutputCollector collector;
    private CorrelationAlertingEngineBolt correlationAlertingEngineBolt;
    private AlertingStormAttributesDto stormAttributes;
    private ZooKeeperAttributesDto zooKeeperAttributes;

    private ArgumentCaptor<Runnable> zooKeeperCallback;
    private ZooKeeperCompositeConnector zooKeeperConnector;
    private ZooKeeperCompositeConnectorFactory zooKeeperConnectorFactory;
    private ArgumentCaptor<Values> argumentEmitCaptor;
    private StormMetricsTestRegistrarFactoryImpl metricsTestRegistrarFactory;

    @Before
    public void setUp() throws Exception {
        stormAttributes = new AlertingStormAttributesDto();
        stormAttributes.setAlertingEngineCleanIntervalSec(1000);
        zooKeeperAttributes = new ZooKeeperAttributesDto();
        stormAttributes.setZookeperAttributes(zooKeeperAttributes);

        tuple = Mockito.mock(Tuple.class);
        collector = Mockito.mock(OutputCollector.class);
        argumentEmitCaptor = ArgumentCaptor.forClass(Values.class);
        zooKeeperConnectorFactory = Mockito.mock(ZooKeeperCompositeConnectorFactory.class);

        zooKeeperConnector = Mockito.mock(ZooKeeperCompositeConnector.class);
        when(zooKeeperConnectorFactory.createZookeeperConnector(zooKeeperAttributes)).thenReturn(zooKeeperConnector);
        when(zooKeeperConnector.getData()).thenReturn(Arrays.asList(simpleCorrelationRules));

        zooKeeperCallback = ArgumentCaptor.forClass(Runnable.class);
        doNothing().when(zooKeeperConnector).addCacheListener(zooKeeperCallback.capture());

        when(tuple.getStringByField(eq(TupleFieldNames.EVENT.toString()))).thenReturn(alert1, alert2, alert1);
        when(collector.emit(eq(tuple), argumentEmitCaptor.capture())).thenReturn(new ArrayList<>());
        metricsTestRegistrarFactory = new StormMetricsTestRegistrarFactoryImpl();

        correlationAlertingEngineBolt = new CorrelationAlertingEngineBolt(stormAttributes,
                zooKeeperConnectorFactory,
                metricsTestRegistrarFactory);
        correlationAlertingEngineBolt.prepare(null, null, collector);

        Assert.assertEquals(1,
                metricsTestRegistrarFactory.getCounterValue(SiembolMetrics.ALERTING_RULES_UPDATE.getMetricName()));
    }

    @Test
    public void testMatchRule() throws IOException {
        correlationAlertingEngineBolt.execute(tuple);
        correlationAlertingEngineBolt.execute(tuple);
        correlationAlertingEngineBolt.execute(tuple);
        verify(collector, times(3)).ack(eq(tuple));

        Values values = argumentEmitCaptor.getValue();
        Assert.assertNotNull(values);
        Assert.assertEquals(2, values.size());
        Assert.assertTrue(values.get(0) instanceof AlertMessages);
        Assert.assertTrue(values.get(1) instanceof ExceptionMessages);

        AlertMessages alerts = (AlertMessages) values.get(0);
        Assert.assertEquals(1, alerts.size());
        Assert.assertTrue(alerts.get(0).isVisibleAlert());
        Assert.assertFalse(alerts.get(0).isCorrelationAlert());
        Assert.assertEquals("test_rule_v1", alerts.get(0).getFullRuleName());
        Assert.assertEquals(Optional.empty(), alerts.get(0).getCorrelationKey());
        Assert.assertEquals(1000, alerts.get(0).getMaxDayMatches());
        Assert.assertEquals(500, alerts.get(0).getMaxHourMatches());

        Map<String, Object> parsed = JSON_READER.readValue(alerts.get(0).getAlertJson());
        Assert.assertEquals("siembol_correlation_alerts_instance",
                parsed.get(AlertingTags.DETECTION_SOURCE_TAG_NAME.toString()));
        Assert.assertEquals("test_rule_v1", parsed.get(AlertingFields.FULL_RULE_NAME.getCorrelationAlertingName()));
        Assert.assertEquals(1000, parsed.get(AlertingFields.MAX_PER_DAY_FIELD.getCorrelationAlertingName()));
        Assert.assertEquals(500, parsed.get(AlertingFields.MAX_PER_HOUR_FIELD.getCorrelationAlertingName()));
        Assert.assertEquals("a", parsed.get(SiembolMessageFields.SENSOR_TYPE.toString()));
        Assert.assertTrue(parsed.get(AlertingFields.PROCESSING_TIME.getCorrelationAlertingName()) instanceof Number);
    }

    @Test
    public void testException() {
        when(tuple.getStringByField(eq(TupleFieldNames.EVENT.toString())))
                .thenReturn("INVALID");

        correlationAlertingEngineBolt.execute(tuple);
        Values values = argumentEmitCaptor.getValue();
        verify(collector, times(1)).ack(eq(tuple));
        Assert.assertEquals(2, values.size());
        Assert.assertTrue(values.get(0) instanceof AlertMessages);
        Assert.assertTrue(values.get(1) instanceof ExceptionMessages);
        Assert.assertTrue(((AlertMessages) values.get(0)).isEmpty());
        Assert.assertEquals(1, ((ExceptionMessages) values.get(1)).size());
        Assert.assertTrue(((ExceptionMessages) values.get(1)).get(0).contains("JsonParseException"));
    }

    @Test
    public void testNoMatchRule() {
        when(tuple.getStringByField(eq(TupleFieldNames.EVENT.toString()))).thenReturn(
                alert1, alert1, alert1, alert1, alert1,
                alert1, alert1, alert1, alert1, alert1);

        for (int i = 0; i < 10; i++) {
            correlationAlertingEngineBolt.execute(tuple);
        }
        verify(collector, times(10)).ack(eq(tuple));
        verify(collector, never()).emit(ArgumentMatchers.any());
    }

    @Test
    public void updateOk() {
        zooKeeperCallback.getValue().run();
        Assert.assertEquals(2,
                metricsTestRegistrarFactory.getCounterValue(SiembolMetrics.ALERTING_RULES_UPDATE.getMetricName()));

        zooKeeperCallback.getValue().run();
        Assert.assertEquals(3,
                metricsTestRegistrarFactory.getCounterValue(SiembolMetrics.ALERTING_RULES_UPDATE.getMetricName()));
        verify(zooKeeperConnector, times(3)).getData();

    }

    @Test
    public void updateError() {
        when(zooKeeperConnector.getData()).thenReturn(Collections.singletonList("INVALID"));
        zooKeeperCallback.getValue().run();
        Assert.assertEquals(1,
                metricsTestRegistrarFactory.getCounterValue(SiembolMetrics.ALERTING_RULES_UPDATE.getMetricName()));
        Assert.assertEquals(1,
                metricsTestRegistrarFactory.getCounterValue(SiembolMetrics.ALERTING_RULES_ERROR_UPDATE.getMetricName()));
    }
}
