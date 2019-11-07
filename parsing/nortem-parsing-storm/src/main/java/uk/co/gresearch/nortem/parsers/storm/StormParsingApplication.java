package uk.co.gresearch.nortem.parsers.storm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.kafka.spout.KafkaSpout;
import org.apache.storm.kafka.spout.KafkaSpoutConfig;
import org.apache.storm.kafka.spout.KafkaSpoutRetryExponentialBackoff;
import org.apache.storm.kafka.spout.KafkaSpoutRetryService;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.nortem.parsers.application.factory.ParsingApplicationFactoryAttributes;
import uk.co.gresearch.nortem.parsers.application.factory.ParsingApplicationFactoryImpl;
import uk.co.gresearch.nortem.parsers.application.factory.ParsingApplicationFactoryResult;

import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.apache.kafka.clients.consumer.ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG;
import static org.apache.storm.kafka.spout.KafkaSpoutConfig.FirstPollOffsetStrategy.LATEST;

public class StormParsingApplication {
    private static final Logger LOG =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final int KAFKA_SPOUT_INIT_DELAY_MICRO_SEC = 500;
    private static final int KAFKA_SPOUT_DELAY_PERIOD_MILLI_SEC = 2;
    private static final int KAFKA_SPOUT_MAX_RETRIES = Integer.MAX_VALUE;
    private static final int KAFKA_SPOUT_MAX_DELAY_SEC = 10;
    private static final String KAFKA_SPOUT = "kafka-spout";
    private static final String KAFKA_WRITER = "kafka-writer";

    private static final int EXPECTED_ARG_SIZE = 2;
    private static final int STORM_ATTR_INDEX = 0;
    private static final int PARSING_ATTR_INDEX = 1;
    private static final String WRONG_ARGUMENT_MSG =  "Wrong arguments. The application expects " +
            "Base64 encoded storm attributes and parsing app attributes";
    private static final String KAFKA_PRINCIPAL_FORMAT_MSG = "%s.%s";
    private static final String TOPOLOGY_NAME_FORMAT_MSG = "parsing-%s";
    private static final String SUBMIT_INFO_LOG = "Submitted parsing application storm topology: {} " +
            "with storm attributes: {}\nparsing application attributes: {}";

    private static KafkaSpoutConfig<String, byte[]> createKafkaSpoutConfig(
            StormParsingApplicationAttributes stormAttributes,
            ParsingApplicationFactoryAttributes parsingAttributes) {
        KafkaSpoutRetryService kafkaSpoutRetryService = new KafkaSpoutRetryExponentialBackoff(
                KafkaSpoutRetryExponentialBackoff.TimeInterval.microSeconds(KAFKA_SPOUT_INIT_DELAY_MICRO_SEC),
                KafkaSpoutRetryExponentialBackoff.TimeInterval.milliSeconds(KAFKA_SPOUT_DELAY_PERIOD_MILLI_SEC),
                KAFKA_SPOUT_MAX_RETRIES,
                KafkaSpoutRetryExponentialBackoff.TimeInterval.seconds(KAFKA_SPOUT_MAX_DELAY_SEC));

        return new KafkaSpoutConfig.Builder<>(
                stormAttributes.getBootstrapServers(),
                StringDeserializer.class,
                ByteArrayDeserializer.class,
                parsingAttributes.getInputTopics())
                .setGroupId(stormAttributes.getGroupId())
                .setSecurityProtocol(stormAttributes.getSecurityProtocol())
                .setFirstPollOffsetStrategy(LATEST)
                .setProp(SESSION_TIMEOUT_MS_CONFIG, stormAttributes.getSessionTimeoutMs())
                .setRetry(kafkaSpoutRetryService)
                .setRecordTranslator(
                        r -> new Values(r.key(), r.value()), new Fields(
                                ParsingApplicationTuples.METADATA.toString(),
                                ParsingApplicationTuples.LOG.toString()))
                .build();
    }

    public static StormTopology createTopology(StormParsingApplicationAttributes stormAttributes,
                                               ParsingApplicationFactoryAttributes parsingAttributes) throws Exception {
        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout(KAFKA_SPOUT,
                new KafkaSpout<>(createKafkaSpoutConfig(stormAttributes, parsingAttributes)),
                parsingAttributes.getInputParallelism());

        builder.setBolt(parsingAttributes.getName(),
                new ParsingApplicationBolt(stormAttributes, parsingAttributes),
                parsingAttributes.getParsingParallelism())
                .localOrShuffleGrouping(KAFKA_SPOUT);

        builder.setBolt(KAFKA_WRITER,
                new ParsingApplicationWriterBolt(stormAttributes),
                parsingAttributes.getOutputParallelism())
                .localOrShuffleGrouping(parsingAttributes.getName());

        return builder.createTopology();
    }

    public static void main(String[] args) throws Exception {
        if(args.length != EXPECTED_ARG_SIZE) {
            LOG.error(WRONG_ARGUMENT_MSG);
            throw new IllegalArgumentException(WRONG_ARGUMENT_MSG);
        }

        String stormAttributesStr = new String(Base64.getDecoder().decode(args[STORM_ATTR_INDEX]));
        String parsingAttributesStr = new String(Base64.getDecoder().decode(args[PARSING_ATTR_INDEX]));

        StormParsingApplicationAttributes stormAttributes = new ObjectMapper()
                .readerFor(StormParsingApplicationAttributes.class)
                .readValue(stormAttributesStr);


        ParsingApplicationFactoryResult result = new ParsingApplicationFactoryImpl().create(parsingAttributesStr);
        if (result.getStatusCode() != ParsingApplicationFactoryResult.StatusCode.OK) {
            throw new IllegalArgumentException(result.getAttributes().getMessage());
        }

        ParsingApplicationFactoryAttributes parsingAttributes = result.getAttributes();
        stormAttributes.setClientId(String.format(KAFKA_PRINCIPAL_FORMAT_MSG,
                stormAttributes.getClientIdPrefix(), parsingAttributes.getName()));
        stormAttributes.setGroupId(String.format(KAFKA_PRINCIPAL_FORMAT_MSG,
                stormAttributes.getGroupIdPrefix(), parsingAttributes.getName()));

        Config config = new Config();
        config.setNumWorkers(stormAttributes.getNumWorkers());
        config.setMaxSpoutPending(stormAttributes.getMaxSpoutPending());
        config.put(Config.TOPOLOGY_MESSAGE_TIMEOUT_SECS, stormAttributes.getTopologyMessageTimeoutSecs());
        config.put(Config.TOPOLOGY_WORKER_CHILDOPTS, stormAttributes.getTopologyWorkerChildopts());

        StormTopology topology = createTopology(stormAttributes, parsingAttributes);
        String topologyName = String.format(TOPOLOGY_NAME_FORMAT_MSG, parsingAttributes.getName());
        LOG.info(SUBMIT_INFO_LOG, topologyName, stormAttributesStr, parsingAttributesStr);
        StormSubmitter.submitTopology(topologyName, config, topology);
    }
}

