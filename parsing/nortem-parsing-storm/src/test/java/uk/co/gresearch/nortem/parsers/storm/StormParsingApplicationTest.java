package uk.co.gresearch.nortem.parsers.storm;

import com.salesforce.kafka.test.KafkaBrokers;
import com.salesforce.kafka.test.junit4.SharedKafkaTestResource;
import org.adrianwalker.multilinestring.Multiline;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.test.TestingServer;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.generated.StormTopology;
import org.apache.zookeeper.CreateMode;
import org.junit.*;
import uk.co.gresearch.nortem.common.utils.TestKafkaProducer;
import uk.co.gresearch.nortem.parsers.application.factory.ParsingApplicationFactoryImpl;
import uk.co.gresearch.nortem.parsers.application.factory.ParsingApplicationFactoryResult;

import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.kafka.common.security.auth.SecurityProtocol.PLAINTEXT;

public class StormParsingApplicationTest {
    /**
     * {
     *   "parsing_app_name": "test",
     *   "parsing_app_version": 1,
     *   "parsing_app_author": "dummy",
     *   "parsing_app_description": "Description of parser application",
     *   "parsing_app_settings": {
     *     "input_topics": [
     *       "secret"
     *     ],
     *     "error_topic": "error",
     *     "input_parallelism": 1,
     *     "parsing_parallelism": 1,
     *     "output_parallelism": 1,
     *     "parsing_app_type": "router_parsing"
     *   },
     *   "parsing_settings": {
     *     "routing_parser": {
     *       "router_parser_name": "test_router",
     *       "routing_field": "command",
     *       "routing_message": "msg",
     *       "default_parser": {
     *         "parser_name": "default_parser",
     *         "output_topic": "output_default"
     *       },
     *       "parsers": [
     *       {
     *         "routing_field_pattern": "secret",
     *         "parser_properties": {
     *           "parser_name": "test_parser",
     *           "output_topic": "out_secret"
     *         }
     *       }
     *     ]
     *   }
     *  }
     * }
     **/
    @Multiline
    public static String simpleRoutingApplicationParser;


    /**
     * {
     *   "parsers_version": 1,
     *   "parsers_configurations": [
     *     {
     *       "parser_description": "for testing single app parser",
     *       "parser_version": 2,
     *       "parser_name": "test_router",
     *       "parser_author": "dummy",
     *       "parser_extractors": [
     *         {
     *           "attributes": {
     *             "should_overwrite_fields": true,
     *             "should_remove_field": false,
     *             "remove_quotes": true,
     *             "skip_empty_values": false,
     *             "thrown_exception_on_error": true,
     *             "nested_separator": "_"
     *           },
     *           "name": "json_object",
     *           "field": "original_string",
     *           "extractor_type": "json_extractor"
     *         }
     *       ],
     *       "parser_attributes": {
     *         "parser_type": "generic"
     *       }
     *     },
     *     {
     *       "parser_description": "for testing routing app paerser",
     *       "parser_version": 2,
     *       "parser_name": "test_parser",
     *       "parser_author": "dummy",
     *       "parser_attributes": {
     *         "parser_type": "generic"
     *       }
     *     },
     *     {
     *       "parser_description": "for testing routing app parser",
     *       "parser_version": 2,
     *       "parser_name": "default_parser",
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
     * {"command" : "secret", "msg" : "secret message"}
     **/
    @Multiline
    public static String testSecretMessage;

    /**
     * {"command" : "info", "msg" : "info message"}
     **/
    @Multiline
    public static String testMessage;

    @ClassRule
    public static final SharedKafkaTestResource sharedKafkaTestResource =
            new SharedKafkaTestResource()
                    .withBrokerProperty("auto.create.topics.enable", "true");

    @Ignore
    @Test
    public void initTestProducer() throws Exception {
        TestingServer zkServer = new TestingServer(6666, true);

        CuratorFramework zkClient = CuratorFrameworkFactory
                .newClient(zkServer.getConnectString(), new RetryNTimes(3, 1000));
        zkClient.start();

        if (zkClient.checkExists().forPath("/nortem/parserconfigs") == null) {
            zkClient.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .forPath("/nortem/parserconfigs", testParsersConfigs.getBytes(UTF_8));
        }

        zkClient.setData().forPath("/nortem/parserconfigs", testParsersConfigs.getBytes(UTF_8));

        KafkaBrokers brokers = sharedKafkaTestResource.getKafkaBrokers();

        StringBuilder sb = new StringBuilder();
        brokers.forEach(x -> {
            sb.append("127.0.0.1");
            sb.append(x.getConnectString().substring(x.getConnectString().lastIndexOf(':')));
        });


        StormParsingApplicationAttributes stormAttributes = new StormParsingApplicationAttributes();
        stormAttributes.setGroupId("parsing-test");
        stormAttributes.setBootstrapServers(sb.toString());
        stormAttributes.setSecurityProtocol(PLAINTEXT.toString());

        stormAttributes.setSessionTimeoutMs(300000);
        stormAttributes.setClientId("my_client_id");
        stormAttributes.setZkUrl(zkServer.getConnectString());
        stormAttributes.setZkPathParserConfigs("/nortem/parserconfigs");
        stormAttributes.setZkBaseSleepMs(1000);
        stormAttributes.setZkMaxRetries(10);
        stormAttributes.setWriterCompressionType("snappy");

        TestKafkaProducer testProducer = new TestKafkaProducer(sb.toString(), "secret");

        ParsingApplicationFactoryResult result = new ParsingApplicationFactoryImpl()
                .create(simpleRoutingApplicationParser);
        Assert.assertTrue(result.getStatusCode() == ParsingApplicationFactoryResult.StatusCode.OK);

        StormTopology topology =  StormParsingApplication.createTopology(stormAttributes, result.getAttributes());

        LocalCluster cluster = new LocalCluster();
        Config config = new Config();
        config.put(Config.TOPOLOGY_DEBUG, true);
        config.put(Config.TOPOLOGY_MESSAGE_TIMEOUT_SECS, 5);

        cluster.submitTopology("test", config, topology);
        TimeUnit.SECONDS.sleep(50);

        testProducer.sendMessage("INVALID");
        for (int i = 1; i < 10; i++) {
            testProducer.sendMessage("test", testMessage);
        }

        for (int i = 1; i < 10; i++) {
            testProducer.sendMessage(testSecretMessage);
        }


        zkClient.setData().forPath("/nortem/parserconfigs", "INVALID".getBytes());
        TimeUnit.MINUTES.sleep(10);
        for (int i = 1; i < 10; i++) {
            testProducer.sendMessage(testMessage);
        }

        zkClient.close();
        zkServer.close();
        System.exit(0);

    }
}

