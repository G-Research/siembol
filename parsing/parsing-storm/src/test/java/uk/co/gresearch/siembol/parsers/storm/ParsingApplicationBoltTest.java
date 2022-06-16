package uk.co.gresearch.siembol.parsers.storm;

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
import org.mockito.Mockito;
import uk.co.gresearch.siembol.common.constants.SiembolMessageFields;
import uk.co.gresearch.siembol.common.metrics.SiembolMetrics;
import uk.co.gresearch.siembol.common.metrics.test.StormMetricsTestRegistrarFactoryImpl;
import uk.co.gresearch.siembol.common.model.StormParsingApplicationAttributesDto;
import uk.co.gresearch.siembol.common.storm.KafkaWriterMessages;
import uk.co.gresearch.siembol.common.model.ZooKeeperAttributesDto;
import uk.co.gresearch.siembol.common.storm.SiembolMetricsCounters;
import uk.co.gresearch.siembol.common.testing.TestingZooKeeperConnectorFactory;
import uk.co.gresearch.siembol.parsers.application.factory.ParsingApplicationFactoryAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class ParsingApplicationBoltTest {
    private static final ObjectReader JSON_READER = new ObjectMapper()
            .readerFor(new TypeReference<Map<String, Object>>() {
            });
    private final String log = """
            RAW_LOG
            """;

    private final String jsonLog = """
            {
              "a" : "dummy",
              "aa" : 2,
              "aaa" : true,
              "aaaa" : 4,
              "aaaaa" : "dummy",
              "aaaaaa" : "dummy",
              "aaaaaaa" : 7,
              "aaaaaaaa" : "dummy",
              "aaaaaaaaa" : false,
              "aaaaaaaaaa" : 2,
              "aaaaaaaaaaa" : 11,
              "aaaaaaaaaaaa" : true,
              "aaaaaaaaaaaaa" : 13,
              "aaaaaaaaaaaaaa" : "dummy",
              "aaaaaaaaaaaaaaa" : true
            }
            """;

    private final String metadata = """
             {"is_metadata" : true}
            """;


    private final String simpleSingleApplicationParser = """
            {
               "parsing_app_name": "test",
               "parsing_app_version": 1,
               "parsing_app_author": "dummy",
               "parsing_app_description": "Description of parser application",
               "parsing_app_settings": {
                 "input_topics": [
                   "secret"
                 ],
                 "parse_metadata" : false,
                 "error_topic": "error",
                 "max_num_fields" : 15,
                 "max_field_size" : 10,
                 "original_string_topic" : "truncated",
                 "input_parallelism": 1,
                 "parsing_parallelism": 2,
                 "output_parallelism": 3,
                 "parsing_app_type": "single_parser"
               },
               "parsing_settings": {
                 "single_parser": {
                   "parser_name": "single",
                   "output_topic": "output"
                 }
               }
             }
            """;


    private final String testParsersConfigs = """
             {
               "parsers_version": 1,
               "parsers_configurations": [
                 {
                   "parser_description": "for testing single app parser",
                   "parser_version": 2,
                   "parser_name": "single",
                   "parser_author": "dummy",
                   "parser_attributes": {
                     "parser_type": "generic"
                   }
                 }
               ]
             }
            """;

    private final String testParsersConfigsJsonExtractor = """
            {
              "parsers_version": 2,
              "parsers_configurations": [
                {
                  "parser_name": "single",
                  "parser_version": 1,
                  "parser_author": "dummy",
                  "parser_attributes": {
                    "parser_type": "generic"
                  },
                  "parser_extractors": [
                    {
                      "is_enabled": true,
                      "extractor_type": "json_extractor",
                      "name": "test",
                      "field": "original_string",
                      "attributes": {
                        "skip_empty_values": true,
                        "should_overwrite_fields": true,
                        "should_remove_field": false
                      }
                    }
                  ]
                }
              ]
            }
            """;

    private final String testParsersConfigsFiltered = """
             {
               "parsers_version": 2,
               "parsers_configurations": [
                 {
                   "parser_description": "for testing single app parser",
                   "parser_version": 3,
                   "parser_name": "single",
                   "parser_author": "dummy",
                   "parser_attributes": {
                     "parser_type": "generic"
                   },
                    "transformations" : [
                     {
                         "transformation_type": "filter_message",
                          "attributes": {
                             "message_filter" : {
                                 "matchers" : [
                                 {
                                     "field_name" : "original_string",
                                     "pattern" : ".*",
                                     "negated" : false
                               }]
                          }}}]
                     }
               ]
             }
            """;

    private Tuple tuple;
    private OutputCollector collector;
    private ParsingApplicationBolt parsingApplicationBolt;
    private ParsingApplicationFactoryAttributes parsingAttributes;
    private ZooKeeperAttributesDto zooKeeperAttributes;
    private final String parsersPath = "parsers";
    private StormParsingApplicationAttributesDto attributes;
    private TestingZooKeeperConnectorFactory zooKeeperConnectorFactory;
    private ArgumentCaptor<Values> argumentEmitCaptor;
    private StormMetricsTestRegistrarFactoryImpl metricsTestRegistrarFactory;

    @Before
    public void setUp() {
        parsingAttributes = new ParsingApplicationFactoryAttributes();
        parsingAttributes.setApplicationParserSpecification(simpleSingleApplicationParser);


        zooKeeperAttributes = new ZooKeeperAttributesDto();
        zooKeeperAttributes.setZkPath(parsersPath);
        attributes = new StormParsingApplicationAttributesDto();
        attributes.setZookeeperAttributes(zooKeeperAttributes);

        tuple = Mockito.mock(Tuple.class);
        collector = Mockito.mock(OutputCollector.class);
        argumentEmitCaptor = ArgumentCaptor.forClass(Values.class);
        zooKeeperConnectorFactory = new TestingZooKeeperConnectorFactory();
        zooKeeperConnectorFactory.setData(parsersPath, testParsersConfigs);

        when(tuple.getStringByField(eq(ParsingApplicationTuples.METADATA.toString()))).thenReturn(metadata);
        when(tuple.getValueByField(eq(ParsingApplicationTuples.LOG.toString()))).thenReturn(log.trim().getBytes());


        when(collector.emit(eq(tuple), argumentEmitCaptor.capture())).thenReturn(new ArrayList<>());
        metricsTestRegistrarFactory = new StormMetricsTestRegistrarFactoryImpl();

        parsingApplicationBolt = new ParsingApplicationBolt(attributes,
                parsingAttributes,
                zooKeeperConnectorFactory,
                metricsTestRegistrarFactory);
        parsingApplicationBolt.prepare(null, null, collector);
    }

    @Test
    public void parsedOk() throws IOException {
        parsingApplicationBolt.execute(tuple);
        Values values = argumentEmitCaptor.getValue();
        Assert.assertNotNull(values);
        Assert.assertEquals(2, values.size());
        Assert.assertTrue(values.get(0) instanceof KafkaWriterMessages);
        KafkaWriterMessages messages = (KafkaWriterMessages)values.get(0);
        Assert.assertEquals(1, messages.size());
        Assert.assertEquals("output", messages.get(0).getTopic());

        Map<String, Object> parsed = JSON_READER.readValue(messages.get(0).getMessage());
        Assert.assertEquals("RAW_LOG", parsed.get(SiembolMessageFields.ORIGINAL.toString()));
        Assert.assertEquals("single", parsed.get(SiembolMessageFields.SENSOR_TYPE.toString()));
        Assert.assertNull(parsed.get("metadata_is_metadata"));

        Assert.assertTrue(values.get(1) instanceof SiembolMetricsCounters);
        var counters = (SiembolMetricsCounters)values.get(1);
        Assert.assertTrue(counters.contains(SiembolMetrics.PARSING_APP_PARSED_MESSAGES.getMetricName()));
        Assert.assertTrue(counters.contains(SiembolMetrics.PARSING_SOURCE_TYPE_PARSED_MESSAGES
                .getMetricName("single")));
        Assert.assertEquals(1,
                metricsTestRegistrarFactory.getCounterValue(SiembolMetrics.PARSING_CONFIGS_UPDATE.getMetricName()));
    }

    @Test
    public void filteredOk() throws Exception {
        Assert.assertEquals(1,
                metricsTestRegistrarFactory.getCounterValue(SiembolMetrics.PARSING_CONFIGS_UPDATE.getMetricName()));

        zooKeeperConnectorFactory.getZooKeeperConnector(parsersPath).setData(testParsersConfigsFiltered);

        Assert.assertEquals(2,
                metricsTestRegistrarFactory.getCounterValue(SiembolMetrics.PARSING_CONFIGS_UPDATE.getMetricName()));

        parsingApplicationBolt.execute(tuple);
        Assert.assertEquals(1, metricsTestRegistrarFactory
                .getCounterValue(SiembolMetrics.PARSING_APP_FILTERED_MESSAGES.getMetricName()));
        Assert.assertEquals(1, metricsTestRegistrarFactory
                .getCounterValue(SiembolMetrics.PARSING_SOURCE_TYPE_FILTERED_MESSAGES
                        .getMetricName("single")));


        verify(collector, never()).emit(eq(tuple), argumentEmitCaptor.capture());
    }

    @Test
    public void removedFieldsTruncatedOriginalString() throws Exception {
        Assert.assertEquals(1,
                metricsTestRegistrarFactory.getCounterValue(SiembolMetrics.PARSING_CONFIGS_UPDATE.getMetricName()));

        zooKeeperConnectorFactory.getZooKeeperConnector(parsersPath).setData(testParsersConfigsJsonExtractor);

        Assert.assertEquals(2,
                metricsTestRegistrarFactory.getCounterValue(SiembolMetrics.PARSING_CONFIGS_UPDATE.getMetricName()));

        when(tuple.getValueByField(eq(ParsingApplicationTuples.LOG.toString())))
                .thenReturn(jsonLog.trim().getBytes());


        parsingApplicationBolt.execute(tuple);
        Values values = argumentEmitCaptor.getValue();
        Assert.assertNotNull(values);
        Assert.assertEquals(2, values.size());
        Assert.assertTrue(values.get(0) instanceof KafkaWriterMessages);
        KafkaWriterMessages messages = (KafkaWriterMessages)values.get(0);
        Assert.assertEquals(2, messages.size());
        Assert.assertEquals("output", messages.get(0).getTopic());
        Assert.assertEquals("truncated", messages.get(1).getTopic());

        Assert.assertEquals(jsonLog.trim(), messages.get(1).getMessage());

        Map<String, Object> parsed = JSON_READER.readValue(messages.get(0).getMessage());
        Assert.assertEquals(15, parsed.size());
        Assert.assertTrue(parsed.containsKey(SiembolMessageFields.ORIGINAL.getName()));
        Assert.assertTrue(parsed.containsKey(SiembolMessageFields.SENSOR_TYPE.getName()));
        Assert.assertTrue(parsed.containsKey(SiembolMessageFields.TIMESTAMP.getName()));
        Assert.assertTrue(parsed.containsKey(SiembolMessageFields.GUID.getName()));
        Assert.assertTrue(parsed.containsKey(SiembolMessageFields.PARSING_TIME.getName()));

        Assert.assertFalse(parsed.containsKey("aaaaaaaaaaa"));
        Assert.assertFalse(parsed.containsKey("aaaaaaaaaaaa"));
        Assert.assertFalse(parsed.containsKey("aaaaaaaaaaaaa"));
        Assert.assertFalse(parsed.containsKey("aaaaaaaaaaaaaa"));
        Assert.assertFalse(parsed.containsKey("aaaaaaaaaaaaaaa"));

        Assert.assertEquals(10, parsed.get(SiembolMessageFields.ORIGINAL.toString()).toString().length());
        Assert.assertEquals("single", parsed.get(SiembolMessageFields.SENSOR_TYPE.toString()));



        Assert.assertTrue(values.get(1) instanceof SiembolMetricsCounters);
        var counters = (SiembolMetricsCounters)values.get(1);
        Assert.assertTrue(counters.contains(SiembolMetrics.PARSING_APP_PARSED_MESSAGES.getMetricName()));
        Assert.assertTrue(counters.contains(SiembolMetrics.PARSING_SOURCE_TYPE_PARSED_MESSAGES
                .getMetricName("single")));
        Assert.assertTrue(counters.contains(SiembolMetrics.PARSING_SOURCE_TYPE_REMOVED_FIELDS_MESSAGES
                .getMetricName("single")));
        Assert.assertTrue(counters.contains(SiembolMetrics.PARSING_SOURCE_TYPE_TRUNCATED_FIELDS_MESSAGES
                .getMetricName("single")));
        Assert.assertTrue(counters.contains(SiembolMetrics.PARSING_SOURCE_TYPE_TRUNCATED_ORIGINAL_STRING_MESSAGES
                .getMetricName("single")));
        Assert.assertTrue(counters.contains(SiembolMetrics.PARSING_SOURCE_TYPE_SENT_ORIGINAL_STRING_MESSAGES
                .getMetricName("single")));

    }
    @Test
    public void truncatedOriginalStringOk() throws Exception {
        when(tuple.getValueByField(eq(ParsingApplicationTuples.LOG.toString())))
                .thenReturn("123456789abcdefgh".getBytes());
        parsingApplicationBolt.execute(tuple);

        Values values = argumentEmitCaptor.getValue();
        Assert.assertNotNull(values);
        Assert.assertEquals(2, values.size());
        Assert.assertTrue(values.get(0) instanceof KafkaWriterMessages);
        KafkaWriterMessages messages = (KafkaWriterMessages)values.get(0);
        Assert.assertEquals(2, messages.size());
        Assert.assertEquals("output", messages.get(0).getTopic());
        Assert.assertEquals("truncated", messages.get(1).getTopic());

        Map<String, Object> parsed = JSON_READER.readValue(messages.get(0).getMessage());
        Assert.assertEquals("123456789a", parsed.get(SiembolMessageFields.ORIGINAL.toString()));
        Assert.assertEquals("single", parsed.get(SiembolMessageFields.SENSOR_TYPE.toString()));
        Assert.assertEquals("123456789abcdefgh", messages.get(1).getMessage());


        Assert.assertTrue(values.get(1) instanceof SiembolMetricsCounters);
        var counters = (SiembolMetricsCounters)values.get(1);
        Assert.assertTrue(counters.contains(SiembolMetrics.PARSING_APP_PARSED_MESSAGES.getMetricName()));
        Assert.assertTrue(counters.contains(SiembolMetrics.PARSING_SOURCE_TYPE_PARSED_MESSAGES
                .getMetricName("single")));
        Assert.assertTrue(counters.contains(SiembolMetrics.PARSING_SOURCE_TYPE_TRUNCATED_FIELDS_MESSAGES
                .getMetricName("single")));
        Assert.assertTrue(counters.contains(SiembolMetrics.PARSING_SOURCE_TYPE_TRUNCATED_ORIGINAL_STRING_MESSAGES
                .getMetricName("single")));
        Assert.assertTrue(counters.contains(SiembolMetrics.PARSING_SOURCE_TYPE_SENT_ORIGINAL_STRING_MESSAGES
                .getMetricName("single")));
    }

    @Test
    public void exceptionMetadata() throws Exception {
        parsingAttributes.setApplicationParserSpecification(simpleSingleApplicationParser.replace(
                "\"parse_metadata\" : false", "\"parse_metadata\" : true"
        ));
        parsingApplicationBolt = new ParsingApplicationBolt(attributes,
                parsingAttributes,
                zooKeeperConnectorFactory,
                metricsTestRegistrarFactory);
        parsingApplicationBolt.prepare(null, null, collector);

        when(tuple.getStringByField(eq(ParsingApplicationTuples.METADATA.toString()))).thenReturn("INVALID");
        parsingApplicationBolt.execute(tuple);
        Values values = argumentEmitCaptor.getValue();
        Assert.assertNotNull(values);
        Assert.assertEquals(2, values.size());
        Assert.assertTrue(values.get(0) instanceof KafkaWriterMessages);
        KafkaWriterMessages messages = (KafkaWriterMessages)values.get(0);
        Assert.assertEquals(1, messages.size());
        Assert.assertEquals("error", messages.get(0).getTopic());

        Map<String, Object> parsed = JSON_READER.readValue(messages.get(0).getMessage());
        Assert.assertEquals("RAW_LOG", parsed.get("raw_message"));
        Assert.assertEquals("error", parsed.get(SiembolMessageFields.SENSOR_TYPE.toString()));
        Assert.assertEquals("parser_error", parsed.get("error_type"));

        Assert.assertTrue(values.get(1) instanceof SiembolMetricsCounters);
        var counters = (SiembolMetricsCounters)values.get(1);
        Assert.assertTrue(counters.contains(SiembolMetrics.PARSING_APP_ERROR_MESSAGES.getMetricName()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void exceptionNullData() {
        when(tuple.getValueByField(eq(ParsingApplicationTuples.LOG.toString()))).thenReturn(null);
        parsingApplicationBolt.execute(tuple);
    }

    @Test
    public void parsedMetadataOk() throws Exception {
        parsingAttributes.setApplicationParserSpecification(simpleSingleApplicationParser.replace(
                "\"parse_metadata\" : false", "\"parse_metadata\" : true"
        ));

        parsingApplicationBolt = new ParsingApplicationBolt(attributes,
                parsingAttributes,
                zooKeeperConnectorFactory,
                metricsTestRegistrarFactory);
        parsingApplicationBolt.prepare(null, null, collector);
        parsingApplicationBolt.execute(tuple);
        Values values = argumentEmitCaptor.getValue();
        Assert.assertNotNull(values);
        Assert.assertEquals(2, values.size());
        Assert.assertTrue(values.get(0) instanceof KafkaWriterMessages);
        KafkaWriterMessages messages = (KafkaWriterMessages)values.get(0);
        Assert.assertEquals(1, messages.size());
        Assert.assertEquals("output", messages.get(0).getTopic());

        Map<String, Object> parsed = JSON_READER.readValue(messages.get(0).getMessage());
        Assert.assertEquals("RAW_LOG", parsed.get(SiembolMessageFields.ORIGINAL.toString()));
        Assert.assertEquals("single", parsed.get(SiembolMessageFields.SENSOR_TYPE.toString()));
        Assert.assertEquals(true, parsed.get("metadata_is_metadata"));

        Assert.assertTrue(values.get(1) instanceof SiembolMetricsCounters);
        var counters = (SiembolMetricsCounters)values.get(1);
        Assert.assertTrue(counters.contains(SiembolMetrics.PARSING_APP_PARSED_MESSAGES.getMetricName()));
        Assert.assertTrue(counters.contains(SiembolMetrics.PARSING_SOURCE_TYPE_PARSED_MESSAGES
                .getMetricName("single")));
    }

    @Test(expected = IllegalStateException.class)
    public void wrongParserconfigInit() {
        zooKeeperConnectorFactory = new TestingZooKeeperConnectorFactory();
        zooKeeperConnectorFactory.setData(parsersPath, "INVALID");
        parsingApplicationBolt = new ParsingApplicationBolt(attributes,
                parsingAttributes,
                zooKeeperConnectorFactory,
                metricsTestRegistrarFactory);
        parsingApplicationBolt.prepare(null, null, collector);
    }

    @Test
    public void wrongParserconfigUpdate() throws Exception {
        Assert.assertEquals(1,
                metricsTestRegistrarFactory.getCounterValue(SiembolMetrics.PARSING_CONFIGS_UPDATE.getMetricName()));

        zooKeeperConnectorFactory.getZooKeeperConnector(parsersPath).setData("INVALID");

        Assert.assertEquals(1,
                metricsTestRegistrarFactory.getCounterValue(SiembolMetrics.PARSING_CONFIGS_UPDATE.getMetricName()));
        Assert.assertEquals(1, metricsTestRegistrarFactory
                .getCounterValue(SiembolMetrics.PARSING_CONFIGS_ERROR_UPDATE.getMetricName()));

        parsingApplicationBolt.execute(tuple);
        Values values = argumentEmitCaptor.getValue();
        Assert.assertNotNull(values);
        Assert.assertEquals(2, values.size());
        Assert.assertTrue(values.get(0) instanceof KafkaWriterMessages);
        KafkaWriterMessages messages = (KafkaWriterMessages)values.get(0);
        Assert.assertEquals(1, messages.size());
        Assert.assertEquals("output", messages.get(0).getTopic());

        Map<String, Object> parsed = JSON_READER.readValue(messages.get(0).getMessage());
        Assert.assertEquals("RAW_LOG", parsed.get(SiembolMessageFields.ORIGINAL.toString()));
        Assert.assertEquals("single", parsed.get(SiembolMessageFields.SENSOR_TYPE.toString()));
        Assert.assertNull(parsed.get("metadata_is_metadata"));

        Assert.assertTrue(values.get(1) instanceof SiembolMetricsCounters);
        var counters = (SiembolMetricsCounters)values.get(1);
        Assert.assertTrue(counters.contains(SiembolMetrics.PARSING_APP_PARSED_MESSAGES.getMetricName()));
        Assert.assertTrue(counters.contains(SiembolMetrics.PARSING_SOURCE_TYPE_PARSED_MESSAGES
                .getMetricName("single")));
    }
}
