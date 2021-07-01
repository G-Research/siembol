package uk.co.gresearch.siembol.parsers.storm;

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
import uk.co.gresearch.siembol.common.constants.SiembolMessageFields;
import uk.co.gresearch.siembol.common.model.StormParsingApplicationAttributesDto;
import uk.co.gresearch.siembol.common.storm.KafkaBatchWriterMessages;
import uk.co.gresearch.siembol.common.model.ZooKeeperAttributesDto;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnector;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnectorFactory;
import uk.co.gresearch.siembol.parsers.application.factory.ParsingApplicationFactoryAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class ParsingApplicationBoltTest {
    private static ObjectReader JSON_READER = new ObjectMapper()
            .readerFor(new TypeReference<Map<String, Object>>() {});
    /**
     *RAW_LOG
     **/
    @Multiline
    public static String log;

    /**
     * {"is_metadata" : true}
     **/
    @Multiline
    public static String metadata;

    /**
     *{
     *   "parsing_app_name": "test",
     *   "parsing_app_version": 1,
     *   "parsing_app_author": "dummy",
     *   "parsing_app_description": "Description of parser application",
     *   "parsing_app_settings": {
     *     "input_topics": [
     *       "secret"
     *     ],
     *     "parse_metadata" : false,
     *     "error_topic": "error",
     *     "input_parallelism": 1,
     *     "parsing_parallelism": 2,
     *     "output_parallelism": 3,
     *     "parsing_app_type": "single_parser"
     *   },
     *   "parsing_settings": {
     *     "single_parser": {
     *       "parser_name": "single",
     *       "output_topic": "output"
     *     }
     *   }
     * }
     **/
    @Multiline
    public static String simpleSingleApplicationParser;

    /**
     * {
     *   "parsers_version": 1,
     *   "parsers_configurations": [
     *     {
     *       "parser_description": "for testing single app parser",
     *       "parser_version": 2,
     *       "parser_name": "single",
     *       "parser_author": "dummy",
     *       "parser_attributes": {
     *         "parser_type": "generic"
     *       }
     *     }
     *   ]
     * }
     **/
    @Multiline
    public static String testParsersConfigs;


    private Tuple tuple;
    private OutputCollector collector;
    ParsingApplicationBolt parsingApplicationBolt;
    ParsingApplicationFactoryAttributes parsingAttributes;
    ZooKeeperAttributesDto zookeperAttributes;
    StormParsingApplicationAttributesDto attributes;
    ZooKeeperConnector zooKeeperConnector;
    ZooKeeperConnectorFactory zooKeeperConnectorFactory;
    ArgumentCaptor<Values> argumentEmitCaptor;

    @Before
    public void setUp() throws Exception {
        parsingAttributes = new ParsingApplicationFactoryAttributes();
        parsingAttributes.setApplicationParserSpecification(simpleSingleApplicationParser);


        zookeperAttributes = new ZooKeeperAttributesDto();
        attributes = new StormParsingApplicationAttributesDto();
        attributes.setZookeeperAttributes(zookeperAttributes);

        tuple = Mockito.mock(Tuple.class);
        collector = Mockito.mock(OutputCollector.class);
        argumentEmitCaptor = ArgumentCaptor.forClass(Values.class);
        zooKeeperConnectorFactory = Mockito.mock(ZooKeeperConnectorFactory.class);

        zooKeeperConnector = Mockito.mock(ZooKeeperConnector.class);
        when(zooKeeperConnectorFactory.createZookeeperConnector(zookeperAttributes)).thenReturn(zooKeeperConnector);
        when(zooKeeperConnector.getData()).thenReturn(testParsersConfigs);

        when(tuple.getStringByField(eq(ParsingApplicationTuples.METADATA.toString()))).thenReturn(metadata);
        when(tuple.getValueByField(eq(ParsingApplicationTuples.LOG.toString()))).thenReturn(log.trim().getBytes());


        when(collector.emit(eq(tuple), argumentEmitCaptor.capture())).thenReturn(new ArrayList<>());

        parsingApplicationBolt = new ParsingApplicationBolt(attributes, parsingAttributes, zooKeeperConnectorFactory);
        parsingApplicationBolt.prepare(null, null, collector);
    }

    @Test
    public void testMatchRule() throws IOException {
       parsingApplicationBolt.execute(tuple);
        Values values = argumentEmitCaptor.getValue();
        Assert.assertNotNull(values);
        Assert.assertEquals(1, values.size());
        Assert.assertTrue(values.get(0) instanceof KafkaBatchWriterMessages);
        KafkaBatchWriterMessages messages = (KafkaBatchWriterMessages)values.get(0);
        Assert.assertEquals(1, messages.size());
        Assert.assertEquals("output", messages.get(0).getTopic());

        Map<String, Object> parsed = JSON_READER.readValue(messages.get(0).getMessage());
        Assert.assertEquals("RAW_LOG", parsed.get(SiembolMessageFields.ORIGINAL.toString()));
        Assert.assertEquals("single", parsed.get(SiembolMessageFields.SENSOR_TYPE.toString()));
        Assert.assertNull(parsed.get("metadata_is_metadata"));
    }

    @Test
    public void testExceptionMetadata() throws Exception {
        parsingAttributes.setApplicationParserSpecification(simpleSingleApplicationParser.replace(
                "\"parse_metadata\" : false", "\"parse_metadata\" : true"
        ));
        parsingApplicationBolt = new ParsingApplicationBolt(attributes, parsingAttributes, zooKeeperConnectorFactory);
        parsingApplicationBolt.prepare(null, null, collector);

        when(tuple.getStringByField(eq(ParsingApplicationTuples.METADATA.toString()))).thenReturn("INVALID");
        parsingApplicationBolt.execute(tuple);
        Values values = argumentEmitCaptor.getValue();
        Assert.assertNotNull(values);
        Assert.assertEquals(1, values.size());
        Assert.assertTrue(values.get(0) instanceof KafkaBatchWriterMessages);
        KafkaBatchWriterMessages messages = (KafkaBatchWriterMessages)values.get(0);
        Assert.assertEquals(1, messages.size());
        Assert.assertEquals("error", messages.get(0).getTopic());

        Map<String, Object> parsed = JSON_READER.readValue(messages.get(0).getMessage());
        Assert.assertEquals("RAW_LOG", parsed.get("raw_message"));
        Assert.assertEquals("error", parsed.get(SiembolMessageFields.SENSOR_TYPE.toString()));
        Assert.assertEquals("parser_error", parsed.get("error_type"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExceptionNullData() {
        when(tuple.getValueByField(eq(ParsingApplicationTuples.LOG.toString()))).thenReturn(null);
        parsingApplicationBolt.execute(tuple);
    }

    @Test
    public void testMetadata() throws Exception {
        parsingAttributes.setApplicationParserSpecification(simpleSingleApplicationParser.replace(
                "\"parse_metadata\" : false", "\"parse_metadata\" : true"
        ));

        parsingApplicationBolt = new ParsingApplicationBolt(attributes, parsingAttributes, zooKeeperConnectorFactory);
        parsingApplicationBolt.prepare(null, null, collector);
        parsingApplicationBolt.execute(tuple);
        Values values = argumentEmitCaptor.getValue();
        Assert.assertNotNull(values);
        Assert.assertEquals(1, values.size());
        Assert.assertTrue(values.get(0) instanceof KafkaBatchWriterMessages);
        KafkaBatchWriterMessages messages = (KafkaBatchWriterMessages)values.get(0);
        Assert.assertEquals(1, messages.size());
        Assert.assertEquals("output", messages.get(0).getTopic());

        Map<String, Object> parsed = JSON_READER.readValue(messages.get(0).getMessage());
        Assert.assertEquals("RAW_LOG", parsed.get(SiembolMessageFields.ORIGINAL.toString()));
        Assert.assertEquals("single", parsed.get(SiembolMessageFields.SENSOR_TYPE.toString()));
        Assert.assertEquals(true, parsed.get("metadata_is_metadata"));
    }

    @Test(expected = IllegalStateException.class)
    public void testExceptionData() throws Exception {
        when(zooKeeperConnector.getData()).thenReturn("INVALID");
        parsingApplicationBolt = new ParsingApplicationBolt(attributes, parsingAttributes, zooKeeperConnectorFactory);
        parsingApplicationBolt.prepare(null, null, collector);
    }
}
