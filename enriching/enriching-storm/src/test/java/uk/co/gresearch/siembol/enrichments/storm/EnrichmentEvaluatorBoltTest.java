package uk.co.gresearch.siembol.enrichments.storm;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import uk.co.gresearch.siembol.common.metrics.SiembolMetrics;
import uk.co.gresearch.siembol.common.metrics.test.StormMetricsTestRegistrarFactoryImpl;
import uk.co.gresearch.siembol.common.model.StormEnrichmentAttributesDto;
import uk.co.gresearch.siembol.common.model.ZooKeeperAttributesDto;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnectorFactory;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnector;
import uk.co.gresearch.siembol.enrichments.storm.common.*;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class EnrichmentEvaluatorBoltTest {
    private final String event = """
            {"a" : "tmp_string", "b" : 1, "is_alert" : "true", "source_type" : "test"}
            """;

    private final String testRules = """
            {
              "rules_version": 1,
              "rules": [
                {
                  "rule_name": "test_rule",
                  "rule_version": 1,
                  "rule_author": "john",
                  "rule_description": "Test rule",
                  "source_type": "*",
                  "matchers": [
                    {
                      "matcher_type": "REGEX_MATCH",
                      "is_negated": false,
                      "field": "is_alert",
                      "data": "(?i)true"
                    }
                  ],
                  "table_mapping": {
                    "table_name": "test_table",
                    "joining_key": "${a}",
                    "tags": [
                      {
                        "tag_name": "is_test_tag",
                        "tag_value": "true"
                      }
                    ],
                    "enriching_fields": [
                      {
                        "table_field_name": "dns_name",
                        "event_field_name": "siembol:enrichments:dns"
                      }
                    ]
                  }
                }
              ]
              }
            """;

    private Tuple tuple;
    private OutputCollector collector;
    private EnrichmentEvaluatorBolt enrichmentEvaluatorBolt;
    private ZooKeeperAttributesDto zooKeeperAttributes;
    private StormEnrichmentAttributesDto attributes;
    private ArgumentCaptor<Runnable> zooKeeperCallback;
    private ZooKeeperConnector zooKeeperConnector;
    private ZooKeeperConnectorFactory zooKeeperConnectorFactory;
    private ArgumentCaptor<Values> argumentEmitCaptor;
    private StormMetricsTestRegistrarFactoryImpl metricsTestRegistrarFactory;

    @Before
    public void setUp() throws Exception {
        zooKeeperAttributes = new ZooKeeperAttributesDto();
        attributes = new StormEnrichmentAttributesDto();
        attributes.setEnrichingRulesZookeperAttributes(zooKeeperAttributes);

        tuple = Mockito.mock(Tuple.class);
        collector = Mockito.mock(OutputCollector.class);
        argumentEmitCaptor = ArgumentCaptor.forClass(Values.class);
        zooKeeperConnectorFactory = Mockito.mock(ZooKeeperConnectorFactory.class);

        zooKeeperConnector = Mockito.mock(ZooKeeperConnector.class);
        when(zooKeeperConnectorFactory.createZookeeperConnector(zooKeeperAttributes)).thenReturn(zooKeeperConnector);
        when(zooKeeperConnector.getData()).thenReturn(testRules);

        zooKeeperCallback = ArgumentCaptor.forClass(Runnable.class);
        doNothing().when(zooKeeperConnector).addCacheListener(zooKeeperCallback.capture());

        when(tuple.getStringByField(eq(EnrichmentTuples.EVENT.toString()))).thenReturn(event);
        when(collector.emit(eq(tuple), argumentEmitCaptor.capture())).thenReturn(new ArrayList<>());

        metricsTestRegistrarFactory = new StormMetricsTestRegistrarFactoryImpl();

        enrichmentEvaluatorBolt = new EnrichmentEvaluatorBolt(attributes,
                zooKeeperConnectorFactory,
                metricsTestRegistrarFactory);
        enrichmentEvaluatorBolt.prepare(null, null, collector);

        Assert.assertEquals(1,
                metricsTestRegistrarFactory.getCounterValue(SiembolMetrics.ENRICHMENT_RULES_UPDATE.getMetricName()));
    }

    @Test
    public void testMatchRule() {
        enrichmentEvaluatorBolt.execute(tuple);
        Values values = argumentEmitCaptor.getValue();
        Assert.assertNotNull(values);
        Assert.assertEquals(3, values.size());
        Assert.assertTrue(values.get(0) instanceof String);
        Assert.assertTrue(values.get(1) instanceof EnrichmentCommands);
        Assert.assertTrue(values.get(2) instanceof EnrichmentExceptions);
        Assert.assertEquals(event, values.get(0));
        EnrichmentCommands commands = (EnrichmentCommands)values.get(1);
        Assert.assertEquals(1, commands.size());
        Assert.assertEquals("tmp_string", commands.get(0).getKey());
        Assert.assertEquals("tmp_string", commands.get(0).getKey());
        Assert.assertEquals(1, commands.get(0).getTags().size());
        Assert.assertEquals(1, commands.get(0).getEnrichmentFields().size());
        Assert.assertTrue(((EnrichmentExceptions)values.get(2)).isEmpty());
    }

    @Test
    public void testNoMatchRule() {
        when(tuple.getStringByField(eq(EnrichmentTuples.EVENT.toString()))).thenReturn("{}");
        enrichmentEvaluatorBolt.execute(tuple);
        Values values = argumentEmitCaptor.getValue();
        Assert.assertNotNull(values);
        Assert.assertEquals(3, values.size());
        Assert.assertTrue(values.get(0) instanceof String);
        Assert.assertTrue(values.get(1) instanceof EnrichmentCommands);
        Assert.assertTrue(values.get(2) instanceof EnrichmentExceptions);
        Assert.assertTrue(((EnrichmentCommands)values.get(1)).isEmpty());
        Assert.assertTrue(((EnrichmentExceptions)values.get(2)).isEmpty());
    }

    @Test
    public void testExceptionRule() {
        when(tuple.getStringByField(eq(EnrichmentTuples.EVENT.toString()))).thenReturn("INVALID");
        enrichmentEvaluatorBolt.execute(tuple);
        Values values = argumentEmitCaptor.getValue();
        Assert.assertNotNull(values);
        Assert.assertEquals(3, values.size());
        Assert.assertTrue(values.get(0) instanceof String);
        Assert.assertTrue(values.get(1) instanceof EnrichmentCommands);
        Assert.assertTrue(values.get(2) instanceof EnrichmentExceptions);
        Assert.assertTrue(((EnrichmentCommands)values.get(1)).isEmpty());
        Assert.assertFalse(((EnrichmentExceptions)values.get(2)).isEmpty());
        Assert.assertEquals(1, ((EnrichmentExceptions)values.get(2)).size());
        Assert.assertTrue(((EnrichmentExceptions)values.get(2)).get(0).contains("JsonParseException"));
    }

    @Test
    public void updateOk() {
        zooKeeperCallback.getValue().run();
        Assert.assertEquals(2,
                metricsTestRegistrarFactory.getCounterValue(SiembolMetrics.ENRICHMENT_RULES_UPDATE.getMetricName()));

        zooKeeperCallback.getValue().run();
        Assert.assertEquals(3,
                metricsTestRegistrarFactory.getCounterValue(SiembolMetrics.ENRICHMENT_RULES_UPDATE.getMetricName()));
        verify(zooKeeperConnector, times(3)).getData();

    }

    @Test
    public void updateError() {
        when(zooKeeperConnector.getData()).thenReturn("INVALID");
        zooKeeperCallback.getValue().run();
        Assert.assertEquals(1,
                metricsTestRegistrarFactory.getCounterValue(SiembolMetrics.ENRICHMENT_RULES_UPDATE.getMetricName()));
        Assert.assertEquals(1, metricsTestRegistrarFactory
                .getCounterValue(SiembolMetrics.ENRICHMENT_RULES_ERROR_UPDATE.getMetricName()));
    }
}
