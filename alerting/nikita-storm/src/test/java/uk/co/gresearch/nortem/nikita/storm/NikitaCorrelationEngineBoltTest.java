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
import uk.co.gresearch.nortem.nikita.common.NikitaTags;
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

public class NikitaCorrelationEngineBoltTest {
    private static ObjectReader JSON_READER = new ObjectMapper()
            .readerFor(new TypeReference<Map<String, Object>>() {});

    /**
     * {
     *   "nikita:full_rule_name": "alert1_v3",
     *   "nikita:rule_name": "alert1",
     *   "correlation_key": "evil",
     *   "nikita:max_per_hour": 200,
     *   "nikita:test": "true",
     *   "source.type": "a",
     *   "nikita:max_per_day": 10000
     * }
     **/
    @Multiline
    public static String alert1;

    /**
     * {
     *   "nikita:full_rule_name": "alert1_v3",
     *   "nikita:rule_name": "alert2",
     *   "correlation_key": "evil",
     *   "sensor": "a",
     *   "nikita:max_per_hour": 200,
     *   "nikita:test": "true",
     *   "source.type": "a",
     *   "nikita:max_per_day": 10000
     * }
     **/
    @Multiline
    public static String alert2;


    /**
     * {
     *   "rules_version": 1,
     *   "tags": [
     *     {
     *       "tag_name": "detection:source",
     *       "tag_value": "nikita_correlation"
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

    private Tuple tuple;
    private OutputCollector collector;
    NikitaCorrelationEngineBolt nikitaCorrelationEngineBolt;
    NikitaStormAttributes stormAttributes;
    ZookeperAttributes zookeperAttributes;

    ZookeperConnector zookeperConnector;
    ZookeperConnectorFactory zookeperConnectorFactory;
    ArgumentCaptor<Values> argumentEmitCaptor;

    @Before
    public void setUp() throws Exception {
        stormAttributes = new NikitaStormAttributes();
        stormAttributes.setNikitaEngineCleanIntervalSec(1000);
        zookeperAttributes = new ZookeperAttributes();
        stormAttributes.setZookeperAttributes(zookeperAttributes);

        tuple = Mockito.mock(Tuple.class);
        collector = Mockito.mock(OutputCollector.class);
        argumentEmitCaptor = ArgumentCaptor.forClass(Values.class);
        zookeperConnectorFactory = Mockito.mock(ZookeperConnectorFactory.class);

        zookeperConnector = Mockito.mock(ZookeperConnector.class);
        when(zookeperConnectorFactory.createZookeperConnector(zookeperAttributes)).thenReturn(zookeperConnector);
        when(zookeperConnector.getData()).thenReturn(simpleCorrelationRules);

        when(tuple.getStringByField(eq(TupleFieldNames.EVENT.toString()))).thenReturn(alert1, alert2, alert1);
        when(collector.emit(eq(tuple), argumentEmitCaptor.capture())).thenReturn(new ArrayList<>());

        nikitaCorrelationEngineBolt = new NikitaCorrelationEngineBolt(stormAttributes, zookeperConnectorFactory);
        nikitaCorrelationEngineBolt.prepare(null, null, collector);
    }

    @Test
    public void testMatchRule() throws IOException {
        nikitaCorrelationEngineBolt.execute(tuple);
        nikitaCorrelationEngineBolt.execute(tuple);
        nikitaCorrelationEngineBolt.execute(tuple);
        verify(collector, times(3)).ack(eq(tuple));

        Values values = argumentEmitCaptor.getValue();
        Assert.assertNotNull(values);
        Assert.assertEquals(2, values.size());
        Assert.assertTrue(values.get(0) instanceof NikitaAlerts);
        Assert.assertTrue(values.get(1) instanceof NikitaExceptions);

        NikitaAlerts alerts = (NikitaAlerts)values.get(0);
        Assert.assertEquals(1, alerts.size());
        Assert.assertTrue(alerts.get(0).isVisibleAlert());
        Assert.assertFalse(alerts.get(0).isCorrelationAlert());
        Assert.assertEquals("test_rule_v1", alerts.get(0).getFullRuleName());
        Assert.assertEquals(Optional.empty(), alerts.get(0).getCorrelationKey());
        Assert.assertEquals(1000, alerts.get(0).getMaxDayMatches());
        Assert.assertEquals(500, alerts.get(0).getMaxHourMatches());

        Map<String, Object> parsed = JSON_READER.readValue(alerts.get(0).getAlertJson());
        Assert.assertEquals("nikita_correlation", parsed.get(NikitaTags.DETECTION_SOURCE_TAG_NAME.toString()));
        Assert.assertEquals("test_rule_v1", parsed.get(NikitaFields.FULL_RULE_NAME.getNikitaCorrelationName()));
        Assert.assertEquals(1000, parsed.get(NikitaFields.MAX_PER_DAY_FIELD.getNikitaCorrelationName()));
        Assert.assertEquals(500, parsed.get(NikitaFields.MAX_PER_HOUR_FIELD.getNikitaCorrelationName()));
        Assert.assertEquals("a", parsed.get(NortemMessageFields.SENSOR_TYPE.toString()));
        Assert.assertTrue(parsed.get(NikitaFields.PROCESSING_TIME.getNikitaCorrelationName()) instanceof Number);
    }

    @Test
    public void testException(){
        when(tuple.getStringByField(eq(TupleFieldNames.EVENT.toString())))
                .thenReturn("INVALID");

        nikitaCorrelationEngineBolt.execute(tuple);
        Values values = argumentEmitCaptor.getValue();
        verify(collector, times(1)).ack(eq(tuple));
        Assert.assertEquals(2, values.size());
        Assert.assertTrue(values.get(0) instanceof NikitaAlerts);
        Assert.assertTrue(values.get(1) instanceof NikitaExceptions);
        Assert.assertTrue(((NikitaAlerts)values.get(0)).isEmpty());
        Assert.assertEquals(1, ((NikitaExceptions)values.get(1)).size());
        Assert.assertTrue(((NikitaExceptions)values.get(1)).get(0).contains("JsonParseException"));
    }

    @Test
    public void testNoMatchRule() {
        when(tuple.getStringByField(eq(TupleFieldNames.EVENT.toString()))).thenReturn(
                alert1, alert1, alert1, alert1, alert1,
                alert1, alert1, alert1, alert1, alert1 );

        for (int i = 0; i < 10; i++) {
            nikitaCorrelationEngineBolt.execute(tuple);
        }
        verify(collector, times(10)).ack(eq(tuple));
        verify(collector, never()).emit(any());
    }
}
