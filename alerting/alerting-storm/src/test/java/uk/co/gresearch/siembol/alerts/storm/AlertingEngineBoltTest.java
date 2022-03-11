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
import uk.co.gresearch.siembol.common.metrics.storm.StormMetricsRegistrarFactory;
import uk.co.gresearch.siembol.common.metrics.test.SiembolMetricsTestRegistrar;
import uk.co.gresearch.siembol.common.metrics.test.StormMetricsTestRegistrarFactoryImpl;
import uk.co.gresearch.siembol.common.model.ZooKeeperAttributesDto;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperCompositeConnector;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperCompositeConnectorFactory;
import uk.co.gresearch.siembol.alerts.common.AlertingFields;
import uk.co.gresearch.siembol.alerts.storm.model.AlertMessages;
import uk.co.gresearch.siembol.alerts.storm.model.ExceptionMessages;
import uk.co.gresearch.siembol.common.model.AlertingStormAttributesDto;

import java.io.IOException;
import java.util.*;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class AlertingEngineBoltTest {
    private static final ObjectReader JSON_READER = new ObjectMapper()
            .readerFor(new TypeReference<Map<String, Object>>() {
            });

    private final String event = """
            {
              "source_type" : "secret",
              "is_alert" : "TruE",
              "dummy_field_int" : 1,
              "dummy_field_boolean" : false
            }
            """;

    private final String simpleTestRules = """
            {
              "rules_version" :1,
              "tags" : [ { "tag_name" : "detection_source", "tag_value" : "siembol_alerts" } ],
              "rules" : [ {
                  "rule_name" : "siembol_alert_generic",
                  "rule_version" : 1,
                  "rule_author" : "dummy",
                  "rule_description": "Test rule - is_alert is equal to true",
                  "source_type" : "*",
                  "matchers" : [ {
                      "matcher_type" : "REGEX_MATCH",
                      "is_negated" : false,
                      "field" : "is_alert",
                      "data" : "(?i)true" },
                      {
                       "matcher_type": "REGEX_MATCH",
                       "is_negated": false,
                       "field": "source_type",
                       "data": "(?<sensor>.*)"
                     }
                      ]
              }]
            }
            """;

    private final String rulesForCorrelation = """
            {
              "rules_version": 1,
              "tags": [
                {
                  "tag_name": "detection_source",
                  "tag_value": "siembol_alerts"
                }
              ],
              "rules": [
                {
                  "rule_name": "siembol_alert_generic",
                  "rule_version": 1,
                  "rule_author": "dummy",
                  "rule_description": "Test rule - is_alert is equal to true",
                  "source_type": "*",
                  "matchers": [
                    {
                      "matcher_type": "REGEX_MATCH",
                      "is_negated": false,
                      "field": "is_alert",
                      "data": "(?i)true"
                    },
                    {
                      "matcher_type": "REGEX_MATCH",
                      "is_negated": false,
                      "field": "source_type",
                      "data": "(?<sensor>.*)"
                    }
                  ],
                  "tags": [
                    {
                      "tag_name": "correlation_key",
                      "tag_value": "${dummy_field_int}"
                    }
                  ]
                }
              ]
            }
            """;

    private Tuple tuple;
    private OutputCollector collector;
    private AlertingEngineBolt AlertingEngineBolt;
    private AlertingStormAttributesDto stormAttributes;
    private ZooKeeperAttributesDto zookeperAttributes;

    private ZooKeeperCompositeConnector zooKeeperConnector;
    private ZooKeeperCompositeConnectorFactory zooKeeperConnectorFactory;
    private ArgumentCaptor<Values> argumentEmitCaptor;
    private StormMetricsTestRegistrarFactoryImpl metricsTestRegistrarFactory;

    @Before
    public void setUp() throws Exception {
        stormAttributes = new AlertingStormAttributesDto();
        zookeperAttributes = new ZooKeeperAttributesDto();
        stormAttributes.setZookeperAttributes(zookeperAttributes);

        tuple = Mockito.mock(Tuple.class);
        collector = Mockito.mock(OutputCollector.class);
        argumentEmitCaptor = ArgumentCaptor.forClass(Values.class);
        zooKeeperConnectorFactory = Mockito.mock(ZooKeeperCompositeConnectorFactory.class);

        zooKeeperConnector = Mockito.mock(ZooKeeperCompositeConnector.class);
        when(zooKeeperConnectorFactory.createZookeeperConnector(zookeperAttributes)).thenReturn(zooKeeperConnector);
        when(zooKeeperConnector.getData()).thenReturn(Collections.singletonList(simpleTestRules));

        when(tuple.getStringByField(eq(TupleFieldNames.EVENT.toString()))).thenReturn(event.trim());
        when(collector.emit(eq(tuple), argumentEmitCaptor.capture())).thenReturn(new ArrayList<>());

        metricsTestRegistrarFactory = new StormMetricsTestRegistrarFactoryImpl();

        AlertingEngineBolt = new AlertingEngineBolt(stormAttributes,
                zooKeeperConnectorFactory,
                metricsTestRegistrarFactory::createSiembolMetricsRegistrar);
        AlertingEngineBolt.prepare(null, null, collector);
    }

    @Test
    public void testMatchRule() throws IOException {
        AlertingEngineBolt.execute(tuple);
        Values values = argumentEmitCaptor.getValue();
        Assert.assertNotNull(values);
        Assert.assertEquals(2, values.size());
        Assert.assertTrue(values.get(0) instanceof AlertMessages);
        Assert.assertTrue(values.get(1) instanceof ExceptionMessages);

        AlertMessages alerts = (AlertMessages) values.get(0);
        Assert.assertEquals(1, alerts.size());
        Assert.assertTrue(alerts.get(0).isVisibleAlert());
        Assert.assertEquals("siembol_alert_generic_v1", alerts.get(0).getFullRuleName());
        Assert.assertEquals(Optional.empty(), alerts.get(0).getCorrelationKey());
        Assert.assertEquals(100, alerts.get(0).getMaxDayMatches());
        Assert.assertEquals(30, alerts.get(0).getMaxHourMatches());

        Map<String, Object> parsed = JSON_READER.readValue(alerts.get(0).getAlertJson());
        Assert.assertEquals("siembol_alert_generic_v1",
                parsed.get(AlertingFields.FULL_RULE_NAME.getAlertingName()));
        Assert.assertEquals(100, parsed.get(AlertingFields.MAX_PER_DAY_FIELD.getAlertingName()));
        Assert.assertEquals(30, parsed.get(AlertingFields.MAX_PER_HOUR_FIELD.getAlertingName()));
        Assert.assertEquals("secret", parsed.get(SiembolMessageFields.SENSOR_TYPE.toString()));
        Assert.assertEquals("secret", parsed.get("sensor"));
        Assert.assertEquals(1, parsed.get("dummy_field_int"));
        Assert.assertEquals(false, parsed.get("dummy_field_boolean"));
        verify(collector, times(1)).ack(eq(tuple));
    }

    @Test
    public void testNoMatchRule() {
        when(tuple.getStringByField(eq(TupleFieldNames.EVENT.toString())))
                .thenReturn(event.replaceAll("is_alert", "unknown"));

        AlertingEngineBolt.execute(tuple);
        verify(collector, never()).emit(ArgumentMatchers.any());
        verify(collector, times(1)).ack(eq(tuple));
    }

    @Test
    public void testMatchRuleCorrelation() throws IOException {
        when(zooKeeperConnector.getData()).thenReturn(Collections.singletonList(rulesForCorrelation));
        AlertingEngineBolt.prepare(null, null, collector);

        AlertingEngineBolt.execute(tuple);
        Values values = argumentEmitCaptor.getValue();
        Assert.assertNotNull(values);
        Assert.assertEquals(2, values.size());
        Assert.assertTrue(values.get(0) instanceof AlertMessages);
        Assert.assertTrue(values.get(1) instanceof ExceptionMessages);

        AlertMessages alerts = (AlertMessages) values.get(0);
        Assert.assertEquals(1, alerts.size());
        Assert.assertTrue(alerts.get(0).isCorrelationAlert());
        Assert.assertFalse(alerts.get(0).isVisibleAlert());

        Assert.assertEquals("siembol_alert_generic_v1", alerts.get(0).getFullRuleName());
        Assert.assertTrue(alerts.get(0).getCorrelationKey().isPresent());
        Assert.assertEquals("1", alerts.get(0).getCorrelationKey().get());
        Assert.assertEquals(100, alerts.get(0).getMaxDayMatches());
        Assert.assertEquals(30, alerts.get(0).getMaxHourMatches());

        Map<String, Object> parsed = JSON_READER.readValue(alerts.get(0).getAlertJson());
        Assert.assertEquals("siembol_alert_generic_v1",
                parsed.get(AlertingFields.FULL_RULE_NAME.getAlertingName()));
        Assert.assertEquals(100, parsed.get(AlertingFields.MAX_PER_DAY_FIELD.getAlertingName()));
        Assert.assertEquals(30, parsed.get(AlertingFields.MAX_PER_HOUR_FIELD.getAlertingName()));
        Assert.assertEquals("secret", parsed.get(SiembolMessageFields.SENSOR_TYPE.toString()));
        Assert.assertEquals("secret", parsed.get("sensor"));
        Assert.assertEquals(1, parsed.get("dummy_field_int"));
        Assert.assertEquals(false, parsed.get("dummy_field_boolean"));
        verify(collector, times(1)).ack(eq(tuple));
    }

    @Test
    public void testException() {
        when(tuple.getStringByField(eq(TupleFieldNames.EVENT.toString())))
                .thenReturn("INVALID");

        AlertingEngineBolt.execute(tuple);
        Values values = argumentEmitCaptor.getValue();
        verify(collector, times(1)).ack(eq(tuple));
        Assert.assertEquals(2, values.size());
        Assert.assertTrue(values.get(0) instanceof AlertMessages);
        Assert.assertTrue(values.get(1) instanceof ExceptionMessages);
        Assert.assertTrue(((AlertMessages) values.get(0)).isEmpty());
        Assert.assertEquals(1, ((ExceptionMessages) values.get(1)).size());
        Assert.assertTrue(((ExceptionMessages) values.get(1)).get(0).contains("JsonParseException"));
    }
}
