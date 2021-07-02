package uk.co.gresearch.siembol.alerts.storm;

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
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.co.gresearch.siembol.common.model.StormAttributesDto;
import uk.co.gresearch.siembol.common.storm.StormHelper;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnectorFactory;
import uk.co.gresearch.siembol.alerts.common.AlertingEngineType;
import uk.co.gresearch.siembol.common.model.AlertingStormAttributesDto;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnectorFactoryImpl;

import java.lang.invoke.MethodHandles;
import java.util.Base64;

import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;

public class AlertingStorm {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String KAFKA_SPOUT = "kafka-spout";
    private static final String KAFKA_WRITER = "kafka-writer";
    private static final int EXPECTED_ARG_SIZE = 1;
    private static final int ATTRIBUTES_ARG_INDEX = 0;
    private static final String WRONG_ARGUMENT_MSG = "Wrong arguments. The application expects Base64 encoded attributes";

    private static KafkaSpoutConfig<String, String> createKafkaSpoutConfig(AlertingStormAttributesDto attributes) {
        StormAttributesDto stormAttributes = attributes.getStormAttributes();
        stormAttributes.setKafkaTopics(attributes.getInputTopics());
        stormAttributes.getKafkaSpoutProperties().getRawMap()
                .put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        stormAttributes.getKafkaSpoutProperties().getRawMap()
                .put(VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        if (AlertingEngineType.valueOfName(attributes.getAlertingEngine()) == AlertingEngineType.SIEMBOL_ALERTS) {
            return StormHelper.createKafkaSpoutConfig(stormAttributes,
                    r -> new Values(r.value()), new Fields(TupleFieldNames.EVENT.toString()));
        } else {
            return StormHelper.createKafkaSpoutConfig(stormAttributes,
                    r -> new Values(r.key(), r.value()),
                    new Fields(TupleFieldNames.CORRELATION_KEY.toString(), TupleFieldNames.EVENT.toString()));
        }
    }

    public static StormTopology createTopology(AlertingStormAttributesDto attributes,
                                               ZooKeeperConnectorFactory zooKeeperConnectorFactory) {
        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout(KAFKA_SPOUT,
                new KafkaSpout<>(createKafkaSpoutConfig(attributes)),
                attributes.getKafkaSpoutNumExecutors());

        builder.setBolt(AlertingEngineType.SIEMBOL_ALERTS.getEngineName(),
                new AlertingEngineBolt(attributes, zooKeeperConnectorFactory), attributes.getAlertingEngineBoltNumExecutors())
                .localOrShuffleGrouping(KAFKA_SPOUT);

        builder.setBolt(KAFKA_WRITER,
                new KafkaWriterBolt(attributes), attributes.getKafkaWriterBoltNumExecutors())
                .localOrShuffleGrouping(AlertingEngineType.SIEMBOL_ALERTS.getEngineName());

        return builder.createTopology();
    }

    public static StormTopology createCorrelationAlertingTopology(AlertingStormAttributesDto attributes,
                                                                  ZooKeeperConnectorFactory zooKeeperConnectorFactory) {
        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout(KAFKA_SPOUT,
                new KafkaSpout<>(createKafkaSpoutConfig(attributes)),
                attributes.getKafkaSpoutNumExecutors());

        builder.setBolt(AlertingEngineType.SIEMBOL_CORRELATION_ALERTS.getEngineName(),
                new CorrelationAlertingEngineBolt(attributes, zooKeeperConnectorFactory),
                attributes.getAlertingEngineBoltNumExecutors())
                .fieldsGrouping(KAFKA_SPOUT, new Fields(TupleFieldNames.CORRELATION_KEY.toString()));

        builder.setBolt(KAFKA_WRITER,
                new KafkaWriterBolt(attributes), attributes.getKafkaWriterBoltNumExecutors())
                .localOrShuffleGrouping(AlertingEngineType.SIEMBOL_CORRELATION_ALERTS.getEngineName());

        return builder.createTopology();
    }

    public static void main(String[] args) throws Exception {
        if(args.length != EXPECTED_ARG_SIZE) {
            LOG.error(WRONG_ARGUMENT_MSG);
            throw new IllegalArgumentException(WRONG_ARGUMENT_MSG);
        }

        String input = new String(Base64.getDecoder().decode(args[ATTRIBUTES_ARG_INDEX]));
        AlertingStormAttributesDto attributes = new ObjectMapper()
                .readerFor(AlertingStormAttributesDto.class)
                .readValue(input);

        AlertingEngineType engineType = AlertingEngineType.valueOfName(attributes.getAlertingEngine());

        Config config = new Config();
        config.putAll(attributes.getStormAttributes().getStormConfig().getRawMap());
        ZooKeeperConnectorFactory zooKeeperConnectorFactory = new ZooKeeperConnectorFactoryImpl();


        StormTopology topology = engineType == AlertingEngineType.SIEMBOL_ALERTS
                ? createTopology(attributes, zooKeeperConnectorFactory)
                : createCorrelationAlertingTopology(attributes, zooKeeperConnectorFactory);
        String topologyName = attributes.getTopologyName() != null
                ? attributes.getTopologyName()
                : engineType.toString();
        StormSubmitter.submitTopology(topologyName, config, topology);
    }
}

