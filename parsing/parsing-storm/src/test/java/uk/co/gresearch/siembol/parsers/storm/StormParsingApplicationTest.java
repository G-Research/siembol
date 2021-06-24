package uk.co.gresearch.siembol.parsers.storm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.charithe.kafka.EphemeralKafkaBroker;
import com.github.charithe.kafka.KafkaJunitRule;
import org.adrianwalker.multilinestring.Multiline;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.generated.StormTopology;
import org.junit.*;
import org.mockito.Mockito;
import uk.co.gresearch.siembol.common.constants.SiembolMessageFields;
import uk.co.gresearch.siembol.common.model.StormParsingApplicationAttributesDto;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnector;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnectorFactory;
import uk.co.gresearch.siembol.parsers.application.factory.ParsingApplicationFactoryAttributes;
import uk.co.gresearch.siembol.parsers.application.factory.ParsingApplicationFactoryImpl;
import uk.co.gresearch.siembol.parsers.application.factory.ParsingApplicationFactoryResult;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

public class StormParsingApplicationTest {
    /**
     *RAW_LOG
     **/
    @Multiline
    public static String log;

    /**
     *{
     *   "parsing_app_name": "test",
     *   "parsing_app_version": 1,
     *   "parsing_app_author": "dummy",
     *   "parsing_app_description": "Description of parser application",
     *   "parsing_app_settings": {
     *     "input_topics": [
     *       "input"
     *     ],
     *     "parse_metadata" : false,
     *     "error_topic": "error",
     *     "input_parallelism": 1,
     *     "parsing_parallelism": 1,
     *     "output_parallelism": 1,
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

    /**
     * {
     *   "client.id.prefix": "test_writer",
     *   "group.id.prefix": "test_reader",
     *   "zookeeper.attributes": {
     *     "zk.path": "/parserconfigs",
     *     "zk.base.sleep.ms": 1000,
     *     "zk.max.retries": 10
     *   },
     *   "kafka.batch.writer.attributes": {
     *     "batch.size": 1,
     *     "producer.properties": {
     *       "security.protocol": "PLAINTEXT"
     *     }
     *   },
     *   "storm.attributes": {
     *     "first.pool.offset.strategy": "EARLIEST",
     *     "kafka.spout.properties": {
     *       "security.protocol": "PLAINTEXT"
     *     },
     *     "storm.config": {
     *       "session.timeout.ms": 100000
     *     }
     *   }
     * }
     **/
    @Multiline
    public static String stormSettings;

    @ClassRule
    public static KafkaJunitRule kafkaRule = new KafkaJunitRule(EphemeralKafkaBroker.create());

    private ParsingApplicationFactoryAttributes parsingAttributes;
    private StormParsingApplicationAttributesDto stormAttributes;
    private ZooKeeperConnector zooKeeperConnector;
    private ZooKeeperConnectorFactory zooKeeperConnectorFactory;
    private StormTopology topology;

    @Before
    public void setUp() throws Exception {
       stormAttributes = new ObjectMapper()
                .readerFor(StormParsingApplicationAttributesDto.class)
                .readValue(stormSettings);

        ParsingApplicationFactoryResult result = new ParsingApplicationFactoryImpl()
                .create(simpleSingleApplicationParser);
        Assert.assertEquals(ParsingApplicationFactoryResult.StatusCode.OK, result.getStatusCode());

        parsingAttributes = result.getAttributes();
        parsingAttributes.setApplicationParserSpecification(simpleSingleApplicationParser);


        zooKeeperConnector = Mockito.mock(ZooKeeperConnector.class, withSettings().serializable());
        when(zooKeeperConnector.getData()).thenReturn(testParsersConfigs);

        zooKeeperConnectorFactory = Mockito.mock(ZooKeeperConnectorFactory.class, withSettings().serializable());
        when(zooKeeperConnectorFactory.createZookeeperConnector(
                stormAttributes.getZookeeperAttributes()))
                .thenReturn(zooKeeperConnector);


        String bootstrapServer = String.format("127.0.0.1:%d", kafkaRule.helper().kafkaPort());
        stormAttributes.getStormAttributes().setBootstrapServers(bootstrapServer);
        stormAttributes.getKafkaBatchWriterAttributes().getProducerProperties().getRawMap()
                .put("bootstrap.servers", bootstrapServer);

        kafkaRule.waitForStartup();
        topology = StormParsingApplication.createTopology(stormAttributes,
                parsingAttributes,
                zooKeeperConnectorFactory);

        LocalCluster cluster = new LocalCluster();
        Config config = new Config();
        config.put(Config.TOPOLOGY_DEBUG, true);
        config.put(Config.TOPOLOGY_MESSAGE_TIMEOUT_SECS, 50);
        cluster.submitTopology("test", config, topology);
    }

    @Test(timeout=200000)
    public void testParseOK() throws Exception {
        kafkaRule.helper().produceStrings("input", log.trim());

        List<String> outputEvent = kafkaRule.helper().consumeStrings("output", 1)
                .get(20, TimeUnit.SECONDS);
        Assert.assertNotNull(outputEvent);
        Assert.assertEquals(1, outputEvent.size());
        Map<String, Object> parsedEvent = new ObjectMapper()
                .readerFor(new TypeReference<Map<String, Object>>() {})
                .readValue(outputEvent.get(0));
        Assert.assertEquals("RAW_LOG", parsedEvent.get(SiembolMessageFields.ORIGINAL.toString()));
        Assert.assertEquals("single", parsedEvent.get(SiembolMessageFields.SENSOR_TYPE.toString()));
        Assert.assertTrue(parsedEvent.get(SiembolMessageFields.GUID.toString()) instanceof String);
        Assert.assertTrue(parsedEvent.get(SiembolMessageFields.TIMESTAMP.toString()) instanceof Number);
        Assert.assertTrue(parsedEvent.get(SiembolMessageFields.PARSING_TIME.toString()) instanceof Number);
    }
}
