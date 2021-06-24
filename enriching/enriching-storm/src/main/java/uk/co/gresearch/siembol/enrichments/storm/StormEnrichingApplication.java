package uk.co.gresearch.siembol.enrichments.storm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.kafka.spout.KafkaSpout;
import org.apache.storm.kafka.spout.KafkaSpoutConfig;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.common.filesystem.HdfsFileSystemFactory;
import uk.co.gresearch.siembol.common.filesystem.SiembolFileSystemFactory;
import uk.co.gresearch.siembol.common.storm.KafkaBatchWriterBolt;
import uk.co.gresearch.siembol.common.model.StormAttributesDto;
import uk.co.gresearch.siembol.common.storm.StormHelper;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnectorFactory;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnectorFactoryImpl;
import uk.co.gresearch.siembol.enrichments.storm.common.EnrichmentTuples;
import uk.co.gresearch.siembol.common.model.StormEnrichmentAttributesDto;

import java.lang.invoke.MethodHandles;
import java.util.Base64;

import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;

public class StormEnrichingApplication {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String KAFKA_SPOUT = "kafka-spout";
    private static final String ENRICHING_ENGINE_BOLT_NAME = "enriching-engine";
    private static final String MEMORY_ENRICHING_BOLT_NAME = "memory-enriching";
    private static final String MERGING_BOLT_NAME = "merging";
    private static final String KAFKA_WRITER_BOLT_NAME = "kafka-writer";
    private static final int EXPECTED_ARG_SIZE = 1;
    private static final int STORM_ATTR_INDEX = 0;
    private static final String WRONG_ARGUMENT_MSG =  "Wrong arguments. The application expects " +
            "Base64 encoded enriching storm application attributes";
    private static final String SUBMIT_INFO_MSG = "Submitted enriching storm topology: {} " +
            "with enriching storm attributes: {}";

    private static KafkaSpoutConfig<String, String> createKafkaSpoutConfig(StormEnrichmentAttributesDto attributes) {
        StormAttributesDto stormAttributes = attributes.getStormAttributes();
        stormAttributes.getKafkaSpoutProperties().getRawMap()
                .put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        stormAttributes.getKafkaSpoutProperties().getRawMap()
                .put(VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        stormAttributes.setKafkaTopics(attributes.getEnrichingInputTopics());

        return StormHelper.createKafkaSpoutConfig(stormAttributes,
                r -> new Values(r.value()), new Fields(EnrichmentTuples.EVENT.toString()));
    }

    public static StormTopology createTopology(StormEnrichmentAttributesDto attributes,
                                               ZooKeeperConnectorFactory zooKeeperConnectorFactory,
                                               SiembolFileSystemFactory siembolFileSystemFactory) {
        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout(KAFKA_SPOUT,
                new KafkaSpout<>(createKafkaSpoutConfig(attributes)),
                attributes.getKafkaSpoutNumExecutors());

        builder.setBolt(ENRICHING_ENGINE_BOLT_NAME,
                new EnrichmentEvaluatorBolt(attributes, zooKeeperConnectorFactory),
                attributes.getEnrichingEngineBoltNumExecutors())
                .localOrShuffleGrouping(KAFKA_SPOUT);

        builder.setBolt(MEMORY_ENRICHING_BOLT_NAME,
                new MemoryTableEnrichmentBolt(attributes, zooKeeperConnectorFactory, siembolFileSystemFactory),
                attributes.getMemoryEnrichingBoltNumExecutors())
                .localOrShuffleGrouping(ENRICHING_ENGINE_BOLT_NAME);

        builder.setBolt(MERGING_BOLT_NAME,
                new EnrichmentMergerBolt(attributes),
                attributes.getMergingBoltNumExecutors())
                .localOrShuffleGrouping(MEMORY_ENRICHING_BOLT_NAME);

        builder.setBolt(KAFKA_WRITER_BOLT_NAME,
                new KafkaBatchWriterBolt(attributes.getKafkaBatchWriterAttributes(),
                        EnrichmentTuples.KAFKA_MESSAGES.toString()),
                attributes.getKafkaWriterBoltNumExecutors())
                .localOrShuffleGrouping(MERGING_BOLT_NAME);
        return builder.createTopology();
    }

    public static void main(String[] args) throws Exception {
        if(args.length != EXPECTED_ARG_SIZE) {
            LOG.error(WRONG_ARGUMENT_MSG);
            throw new IllegalArgumentException(WRONG_ARGUMENT_MSG);
        }

        String attributesStr = new String(Base64.getDecoder().decode(args[STORM_ATTR_INDEX]));
        StormEnrichmentAttributesDto attributes = new ObjectMapper()
                .readerFor(StormEnrichmentAttributesDto.class)
                .readValue(attributesStr);

        Config config = new Config();
        config.putAll(attributes.getStormAttributes().getStormConfig().getRawMap());
        StormTopology topology = createTopology(attributes,
                new ZooKeeperConnectorFactoryImpl(),
                new HdfsFileSystemFactory(attributes.getEnrichingTablesHdfsUri()));

        LOG.info(SUBMIT_INFO_MSG, attributesStr);
        StormSubmitter.submitTopology(attributes.getTopologyName(), config, topology);
    }
}
