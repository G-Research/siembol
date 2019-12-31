package uk.co.gresearch.nortem.nikita.storm;

import org.apache.kafka.common.serialization.*;
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
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.invoke.MethodHandles;
import java.util.Base64;

import static org.apache.kafka.clients.consumer.ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG;

public class NikitaStorm {
    private static final Logger LOG =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    public static final int KAFKA_SPOUT_INIT_DELAY_MICRO_SEC = 500;
    public static final int KAFKA_SPOUT_DELAY_PERIOD_MILLI_SEC = 2;
    public static final int KAFKA_SPOUT_MAX_RETRIES = Integer.MAX_VALUE;
    public static final int KAFKA_SPOUT_MAX_DELAY_SEC = 10;
    public static final String KAFKA_SPOUT = "kafka-spout";
    public static final String KAFKA_WRITER = "kafka-writer";

    public static final int EXPECTED_ARG_SIZE = 1;
    private static final String WRONG_ARGUMENT_MSG =  "Wrong arguments. The application expects Base64 encoded attributes";

    private static KafkaSpoutConfig<String, String> createKafkaSpoutConfig(NikitaStormAttributes attributes) {
        KafkaSpoutRetryService kafkaSpoutRetryService =  new KafkaSpoutRetryExponentialBackoff(
                KafkaSpoutRetryExponentialBackoff.TimeInterval.microSeconds(KAFKA_SPOUT_INIT_DELAY_MICRO_SEC),
                KafkaSpoutRetryExponentialBackoff.TimeInterval.milliSeconds(KAFKA_SPOUT_DELAY_PERIOD_MILLI_SEC),
                KAFKA_SPOUT_MAX_RETRIES,
                KafkaSpoutRetryExponentialBackoff.TimeInterval.seconds(KAFKA_SPOUT_MAX_DELAY_SEC));

        KafkaSpoutConfig.FirstPollOffsetStrategy pollStrategy = KafkaSpoutConfig.FirstPollOffsetStrategy
                .valueOf(attributes.getFirstPollOffsetStrategy());
        KafkaSpoutConfig.Builder<String, String> builder =  new KafkaSpoutConfig.Builder<>(
                attributes.getBootstrapServers(),
                StringDeserializer.class,
                StringDeserializer.class,
                attributes.getNikitaInputTopic())
                .setGroupId(attributes.getGroupId())
                .setSecurityProtocol(attributes.getSecurityProtocol())
                .setFirstPollOffsetStrategy(pollStrategy)
                .setProp(SESSION_TIMEOUT_MS_CONFIG, attributes.getSessionTimeoutMs())
                .setRetry(kafkaSpoutRetryService);

        if (NikitaEngineType.valueOfName(attributes.getNikitaEngine()) == NikitaEngineType.NIKITA) {
            builder
                    .setRecordTranslator(
                            r -> new Values(r.value()), new Fields(TupleFieldNames.EVENT.toString()));
        } else {
            builder
                    .setRecordTranslator(
                            r -> new Values(r.key(), r.value()),
                            new Fields(TupleFieldNames.CORRELATION_KEY.toString(), TupleFieldNames.EVENT.toString()));
        }

        return builder.build();
    }

    public static StormTopology createTopology(NikitaStormAttributes attributes) {
        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout(KAFKA_SPOUT,
                new KafkaSpout<>(createKafkaSpoutConfig(attributes)),
                attributes.getKafkaSpoutNumExecutors());

        builder.setBolt(NikitaEngineType.NIKITA.getEngineName(),
                new NikitaEngineBolt(attributes), attributes.getNikitaEngineBoltNumExecutors())
                .localOrShuffleGrouping(KAFKA_SPOUT);

        builder.setBolt(KAFKA_WRITER,
                new KafkaWriterBolt(attributes), attributes.getKafkaWriterBoltNumExecutors())
                .localOrShuffleGrouping(NikitaEngineType.NIKITA.getEngineName());

        return builder.createTopology();
    }

    public static StormTopology createNikitaCorrelationTopology(NikitaStormAttributes attributes) {
        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout(KAFKA_SPOUT,
                new KafkaSpout<>(createKafkaSpoutConfig(attributes)),
                attributes.getKafkaSpoutNumExecutors());

        builder.setBolt(NikitaEngineType.NIKITA_CORRELATION.getEngineName(),
                new NikitaCorrelationEngineBolt(attributes), attributes.getNikitaEngineBoltNumExecutors())
                .fieldsGrouping(KAFKA_SPOUT, new Fields(TupleFieldNames.CORRELATION_KEY.toString()));

        builder.setBolt(KAFKA_WRITER,
                new KafkaWriterBolt(attributes), attributes.getKafkaWriterBoltNumExecutors())
                .localOrShuffleGrouping(NikitaEngineType.NIKITA_CORRELATION.getEngineName());

        return builder.createTopology();
    }

    public static void main(String[] args) throws Exception {
        if(args.length != EXPECTED_ARG_SIZE) {
            LOG.error(WRONG_ARGUMENT_MSG);
            throw new IllegalArgumentException(WRONG_ARGUMENT_MSG);
        }

        String input = new String(Base64.getDecoder().decode(args[0]));
        NikitaStormAttributes attributes = new ObjectMapper()
                .readerFor(NikitaStormAttributes.class)
                .readValue(input);

        NikitaEngineType engineType = NikitaEngineType.valueOfName(attributes.getNikitaEngine());

        Config config = new Config();
        config.setNumWorkers(attributes.getNumWorkers());
        config.setMaxSpoutPending(attributes.getMaxSpoutPending());
        config.put(Config.TOPOLOGY_MESSAGE_TIMEOUT_SECS, attributes.getTopologyMessageTimeoutSecs());
        config.put(Config.TOPOLOGY_WORKER_CHILDOPTS, attributes.getTopologyWorkerChildopts());

        StormTopology topology = engineType == NikitaEngineType.NIKITA
                ? createTopology(attributes)
                : createNikitaCorrelationTopology(attributes);
        StormSubmitter.submitTopology(engineType.toString(), config, topology);
    }
}

