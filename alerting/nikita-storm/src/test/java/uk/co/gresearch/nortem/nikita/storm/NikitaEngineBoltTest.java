package uk.co.gresearch.nortem.nikita.storm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.adrianwalker.multilinestring.Multiline;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import uk.co.gresearch.nortem.common.constants.NortemMessageFields;
import uk.co.gresearch.nortem.common.zookeper.ZookeperAttributes;
import uk.co.gresearch.nortem.common.zookeper.ZookeperConnector;
import uk.co.gresearch.nortem.common.zookeper.ZookeperConnectorFactory;
import uk.co.gresearch.nortem.nikita.common.NikitaFields;
import uk.co.gresearch.nortem.nikita.storm.model.NikitaAlerts;
import uk.co.gresearch.nortem.nikita.storm.model.NikitaExceptions;
import uk.co.gresearch.nortem.nikita.storm.model.NikitaStormAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class NikitaEngineBoltTest {
    private static ObjectReader JSON_READER = new ObjectMapper()
            .readerFor(new TypeReference<Map<String, Object>>() {});

    /**
    *{
     *  "source.type" : "secret",
     *  "is_alert" : "TruE",
     *  "dummy_field_int" : 1,
     *  "dummy_field_boolean" : false
     *}
     **/
    @Multiline
    public static String event;


    /**
     *{
     *  "rules_version" :1,
     *  "tags" : [ { "tag_name" : "detection:source", "tag_value" : "nikita" } ],
     *  "rules" : [ {
     *      "rule_name" : "nortem_alert_generic",
     *      "rule_version" : 1,
     *      "rule_author" : "dummy",
     *      "rule_description": "Test rule - is_alert is equal to true",
     *      "source_type" : "*",
     *      "matchers" : [ {
     *          "matcher_type" : "REGEX_MATCH",
     *          "is_negated" : false,
     *          "field" : "is_alert",
     *          "data" : "(?i)true" },
     *          {
     *           "matcher_type": "REGEX_MATCH",
     *           "is_negated": false,
     *           "field": "source.type",
     *           "data": "(?<sensor>.*)"
     *         }
     *          ]
     *  }]
     *}
     **/
    @Multiline
    public static String simpleTestRules;

    /**
     * {
     *   "rules_version": 1,
     *   "tags": [
     *     {
     *       "tag_name": "detection:source",
     *       "tag_value": "nikita"
     *     }
     *   ],
     *   "rules": [
     *     {
     *       "rule_name": "nortem_alert_generic",
     *       "rule_version": 1,
     *       "rule_author": "dummy",
     *       "rule_description": "Test rule - is_alert is equal to true",
     *       "source_type": "*",
     *       "matchers": [
     *         {
     *           "matcher_type": "REGEX_MATCH",
     *           "is_negated": false,
     *           "field": "is_alert",
     *           "data": "(?i)true"
     *         },
     *         {
     *           "matcher_type": "REGEX_MATCH",
     *           "is_negated": false,
     *           "field": "source.type",
     *           "data": "(?<sensor>.*)"
     *         }
     *       ],
     *       "tags": [
     *         {
     *           "tag_name": "correlation_key",
     *           "tag_value": "${dummy_field_int}"
     *         }
     *       ]
     *     }
     *   ]
     * }
     **/
    @Multiline
    public static String rulesForCorrelation;

    private Tuple tuple;
    private OutputCollector collector;
    NikitaEngineBolt nikitaEngineBolt;
    NikitaStormAttributes stormAttributes;
    ZookeperAttributes zookeperAttributes;

    ZookeperConnector zookeperConnector;
    ZookeperConnectorFactory zookeperConnectorFactory;
    ArgumentCaptor<Values> argumentEmitCaptor;

    @Before
    public void setUp() throws Exception {
        stormAttributes = new NikitaStormAttributes();
        zookeperAttributes = new ZookeperAttributes();
        stormAttributes.setZookeperAttributes(zookeperAttributes);

        tuple = Mockito.mock(Tuple.class);
        collector = Mockito.mock(OutputCollector.class);
        argumentEmitCaptor = ArgumentCaptor.forClass(Values.class);
        zookeperConnectorFactory = Mockito.mock(ZookeperConnectorFactory.class);

        zookeperConnector = Mockito.mock(ZookeperConnector.class);
        when(zookeperConnectorFactory.createZookeperConnector(zookeperAttributes)).thenReturn(zookeperConnector);
        when(zookeperConnector.getData()).thenReturn(simpleTestRules);

        when(tuple.getStringByField(eq(TupleFieldNames.EVENT.toString()))).thenReturn(event.trim());
        when(collector.emit(eq(tuple), argumentEmitCaptor.capture())).thenReturn(new ArrayList<>());

        nikitaEngineBolt = new NikitaEngineBolt(stormAttributes, zookeperConnectorFactory);
        nikitaEngineBolt.prepare(null, null, collector);
    }

    @Test
    public void testMatchRule() throws IOException {
        nikitaEngineBolt.execute(tuple);
        Values values = argumentEmitCaptor.getValue();
        Assert.assertNotNull(values);
        Assert.assertEquals(2, values.size());
        Assert.assertTrue(values.get(0) instanceof NikitaAlerts);
        Assert.assertTrue(values.get(1) instanceof NikitaExceptions);

        NikitaAlerts alerts = (NikitaAlerts)values.get(0);
        Assert.assertEquals(1, alerts.size());
        Assert.assertTrue(alerts.get(0).isVisibleAlert());
        Assert.assertEquals("nortem_alert_generic_v1", alerts.get(0).getFullRuleName());
        Assert.assertEquals(Optional.empty(), alerts.get(0).getCorrelationKey());
        Assert.assertEquals(100, alerts.get(0).getMaxDayMatches());
        Assert.assertEquals(30, alerts.get(0).getMaxHourMatches());

        Map<String, Object> parsed = JSON_READER.readValue(alerts.get(0).getAlertJson());
        Assert.assertEquals("nortem_alert_generic_v1", parsed.get(NikitaFields.FULL_RULE_NAME.getNikitaName()));
        Assert.assertEquals(100, parsed.get(NikitaFields.MAX_PER_DAY_FIELD.getNikitaName()));
        Assert.assertEquals(30, parsed.get(NikitaFields.MAX_PER_HOUR_FIELD.getNikitaName()));
        Assert.assertEquals("secret", parsed.get(NortemMessageFields.SENSOR_TYPE.toString()));
        Assert.assertEquals("secret", parsed.get("sensor"));
        Assert.assertEquals(1, parsed.get("dummy_field_int"));
        Assert.assertEquals(false, parsed.get("dummy_field_boolean"));
        verify(collector, times(1)).ack(eq(tuple));
    }

    @Test
    public void testNoMatchRule() {
        when(tuple.getStringByField(eq(TupleFieldNames.EVENT.toString())))
                .thenReturn(event.replaceAll("is_alert", "unknown"));

        nikitaEngineBolt.execute(tuple);
        verify(collector, never()).emit(any());
        verify(collector, times(1)).ack(eq(tuple));
    }

    @Test
    public void testMatchRuleCorrelation() throws IOException {
        when(zookeperConnector.getData()).thenReturn(rulesForCorrelation);
        nikitaEngineBolt.prepare(null, null, collector);

        nikitaEngineBolt.execute(tuple);
        Values values = argumentEmitCaptor.getValue();
        Assert.assertNotNull(values);
        Assert.assertEquals(2, values.size());
        Assert.assertTrue(values.get(0) instanceof NikitaAlerts);
        Assert.assertTrue(values.get(1) instanceof NikitaExceptions);

        NikitaAlerts alerts = (NikitaAlerts)values.get(0);
        Assert.assertEquals(1, alerts.size());
        Assert.assertTrue(alerts.get(0).isCorrelationAlert());
        Assert.assertFalse(alerts.get(0).isVisibleAlert());

        Assert.assertEquals("nortem_alert_generic_v1", alerts.get(0).getFullRuleName());
        Assert.assertEquals("1", alerts.get(0).getCorrelationKey().get());
        Assert.assertEquals(100, alerts.get(0).getMaxDayMatches());
        Assert.assertEquals(30, alerts.get(0).getMaxHourMatches());

        Map<String, Object> parsed = JSON_READER.readValue(alerts.get(0).getAlertJson());
        Assert.assertEquals("nortem_alert_generic_v1", parsed.get(NikitaFields.FULL_RULE_NAME.getNikitaName()));
        Assert.assertEquals(100, parsed.get(NikitaFields.MAX_PER_DAY_FIELD.getNikitaName()));
        Assert.assertEquals(30, parsed.get(NikitaFields.MAX_PER_HOUR_FIELD.getNikitaName()));
        Assert.assertEquals("secret", parsed.get(NortemMessageFields.SENSOR_TYPE.toString()));
        Assert.assertEquals("secret", parsed.get("sensor"));
        Assert.assertEquals(1, parsed.get("dummy_field_int"));
        Assert.assertEquals(false, parsed.get("dummy_field_boolean"));
        verify(collector, times(1)).ack(eq(tuple));
    }

    @Test
    public void testException(){
        when(tuple.getStringByField(eq(TupleFieldNames.EVENT.toString())))
                .thenReturn("INVALID");

        nikitaEngineBolt.execute(tuple);
        Values values = argumentEmitCaptor.getValue();
        verify(collector, times(1)).ack(eq(tuple));
        Assert.assertEquals(2, values.size());
        Assert.assertTrue(values.get(0) instanceof NikitaAlerts);
        Assert.assertTrue(values.get(1) instanceof NikitaExceptions);
        Assert.assertTrue(((NikitaAlerts)values.get(0)).isEmpty());
        Assert.assertEquals(1, ((NikitaExceptions)values.get(1)).size());
        Assert.assertTrue(((NikitaExceptions)values.get(1)).get(0).contains("JsonParseException"));
    }
}
