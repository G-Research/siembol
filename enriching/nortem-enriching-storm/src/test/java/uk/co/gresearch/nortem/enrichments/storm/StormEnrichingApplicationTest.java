package uk.co.gresearch.nortem.enrichments.storm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.github.charithe.kafka.EphemeralKafkaBroker;
import com.github.charithe.kafka.KafkaJunitRule;
import org.adrianwalker.multilinestring.Multiline;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.generated.StormTopology;
import org.junit.*;
import org.mockito.Mockito;
import uk.co.gresearch.nortem.common.filesystem.ByteArrayFileSystem;
import uk.co.gresearch.nortem.common.filesystem.NortemFileSystem;
import uk.co.gresearch.nortem.common.filesystem.NortemFileSystemFactory;
import uk.co.gresearch.nortem.common.zookeper.ZookeperConnectorFactory;
import uk.co.gresearch.nortem.common.zookeper.ZookeperConnector;
import uk.co.gresearch.nortem.enrichments.storm.common.StormEnrichmentAttributes;


import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

public class StormEnrichingApplicationTest {
    private static final ObjectReader JSON_PARSERS_CONFIG_READER = new ObjectMapper()
            .readerFor(StormEnrichmentAttributes.class);

    /**
     * {
     *   "topology.name": "testing",
     *   "kafka.spout.num.executors": 1,
     *   "enriching.engine.bolt.num.executors": 1,
     *   "memory.enriching.bolt.num.executors": 1,
     *   "merging.bolt.num.executors": 1,
     *   "kafka.writer.bolt.num.executors": 1,
     *   "enriching.output.topic": "output",
     *   "enriching.error.topic": "error",
     *   "enriching.tables.hdfs.uri": "hdfs://secret",
     *   "enriching.rules.zookeeper.attributes": {
     *     "zk.path": "/enrichment/rules",
     *     "zk.base.sleep.ms": 1000,
     *     "zk.max.retries": 10
     *   },
     *   "enriching.tables.zookeeper.attributes": {
     *     "zk.path": "/enrichment/tables",
     *     "zk.base.sleep.ms": 1000,
     *     "zk.max.retries": 10
     *   },
     *   "kafka.batch.writer.attributes": {
     *     "batch.size": 1,
     *     "producer.properties": {
     *       "client.id": "writer",
     *       "compression.type": "snappy",
     *       "security.protocol": "PLAINTEXT"
     *     }
     *   },
     *   "storm.attributes": {
     *     "kafka.topics": [
     *       "enrichments"
     *     ],
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
    public static String testEnrichmentStormConfig;

    /**
     * {
     *     "hdfs_tables" : [
     *     {
     *       "name" : "test_table",
     *        "path": "/nortem/tables/enrichment/test.json"
     *     }]
     * }
     **/
    @Multiline
    public static String tablesUpdate;

    /**
     *
     * {
     *   "1.2.3.4" : { "dns_name" : "secret.unknown" }
     * }
     **/
    @Multiline
    public static String simpleOneField;

    /**
     *{"ip_src_addr":"1.2.3.4","b":1,"is_alert":"true","source.type":"test","is_test_tag":"true","nortem:enrichments:dns":"secret.unknown","nortem:enriching_time":
     **/
    @Multiline
    public static String expectedEvent;

    /**
     * {"ip_src_addr" : "1.2.3.4", "b" : 1, "is_alert" : "true", "source.type" : "test"}
     **/
    @Multiline
    public static String event;

    /**
     * {
     *   "rules_version": 1,
     *   "rules": [
     *     {
     *       "rule_name": "test_rule",
     *       "rule_version": 1,
     *       "rule_author": "john",
     *       "rule_description": "Test rule",
     *       "source_type": "*",
     *       "matchers": [
     *         {
     *           "matcher_type": "REGEX_MATCH",
     *           "is_negated": false,
     *           "field": "is_alert",
     *           "data": "(?i)true"
     *         }
     *       ],
     *       "table_mapping": {
     *         "table_name": "test_table",
     *         "joining_key": "${ip_src_addr}",
     *         "tags": [
     *           {
     *             "tag_name": "is_test_tag",
     *             "tag_value": "true"
     *           }
     *         ],
     *         "enriching_fields": [
     *           {
     *             "table_field_name": "dns_name",
     *             "event_field_name": "nortem:enrichments:dns"
     *           }
     *         ]
     *       }
     *     }
     *   ]
     *   }
     **/
    @Multiline
    public static String testRules;


    @ClassRule
    public static KafkaJunitRule kafkaRule = new KafkaJunitRule(EphemeralKafkaBroker.create());

    ZookeperConnector rulesZookeperConnector;
    ZookeperConnectorFactory zookeperConnectorFactory;
    ZookeperConnector enrichingTablesZookeperConnector;
    NortemFileSystemFactory fileSystemFactory;
    NortemFileSystem fileSystem;
    StormEnrichmentAttributes enrichmentAttributes;
    StormTopology topology;

    @Before
    public void setUp() throws Exception {
        enrichmentAttributes = JSON_PARSERS_CONFIG_READER
                .readValue(testEnrichmentStormConfig);
        zookeperConnectorFactory = Mockito.mock(ZookeperConnectorFactory.class, withSettings().serializable());


        rulesZookeperConnector = Mockito.mock(ZookeperConnector.class, withSettings().serializable());
        when(zookeperConnectorFactory.createZookeperConnector(
                enrichmentAttributes.getEnrichingRulesZookeperAttributes()))
                .thenReturn(rulesZookeperConnector);

        when(rulesZookeperConnector.getData()).thenReturn(testRules);

        enrichingTablesZookeperConnector = Mockito.mock(ZookeperConnector.class, withSettings().serializable());
        when(zookeperConnectorFactory.createZookeperConnector(
                enrichmentAttributes.getEnrichingTablesAttributes()))
                .thenReturn(enrichingTablesZookeperConnector);
        when(enrichingTablesZookeperConnector.getData()).thenReturn(tablesUpdate);

        fileSystemFactory = Mockito.mock(NortemFileSystemFactory.class, withSettings().serializable());
        fileSystem = new ByteArrayFileSystem(simpleOneField);
        when(fileSystemFactory.create()).thenReturn(fileSystem);

        String bootstrapServer = String.format("127.0.0.1:%d", kafkaRule.helper().kafkaPort());
        enrichmentAttributes.getStormAttributes().setBootstrapServers(bootstrapServer);
        enrichmentAttributes.getKafkaBatchWriterAttributes().getProducerProperties()
                .put("bootstrap.servers", bootstrapServer);

        kafkaRule.waitForStartup();
        topology = StormEnrichingApplication.createTopology(enrichmentAttributes,
                zookeperConnectorFactory,
                fileSystemFactory);

        LocalCluster cluster = new LocalCluster();
        Config config = new Config();
        config.put(Config.TOPOLOGY_DEBUG, true);
        config.put(Config.TOPOLOGY_MESSAGE_TIMEOUT_SECS, 50);
        cluster.submitTopology("test", config, topology);
    }

    @Test
    public void testEnrichMessageOK() throws Exception {
        kafkaRule.helper().produceStrings("enrichments", event.trim());
        List<String> outputEvent = kafkaRule.helper().consumeStrings("output", 1)
                .get(10, TimeUnit.SECONDS);
        Assert.assertNotNull(outputEvent);
        Assert.assertEquals(1, outputEvent.size());
        Assert.assertTrue(outputEvent.get(0).contains(expectedEvent.trim()));
    }
}

