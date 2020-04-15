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
import uk.co.gresearch.siembol.common.storm.StormAttributes;
import uk.co.gresearch.siembol.common.storm.StormHelper;
import uk.co.gresearch.siembol.common.zookeper.ZookeperConnectorFactory;
import uk.co.gresearch.siembol.alerts.storm.model.AlertingEngineType;
import uk.co.gresearch.siembol.alerts.storm.model.AlertingStormAttributes;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
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

    private static KafkaSpoutConfig<String, String> createKafkaSpoutConfig(AlertingStormAttributes attributes) {
        StormAttributes stormAttributes = attributes.getStormAttributes();
        stormAttributes.setKafkaTopics(Arrays.asList(attributes.getInputTopic()));
        stormAttributes.getKafkaSpoutProperties().put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        stormAttributes.getKafkaSpoutProperties().put(VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        if (AlertingEngineType.valueOfName(attributes.getAlertingEngine()) == AlertingEngineType.SIEMBOL_ALERTS) {
            return StormHelper.createKafkaSpoutConfig(stormAttributes,
                    r -> new Values(r.value()), new Fields(TupleFieldNames.EVENT.toString()));
        } else {
            return StormHelper.createKafkaSpoutConfig(stormAttributes,
                    r -> new Values(r.key(), r.value()),
                    new Fields(TupleFieldNames.CORRELATION_KEY.toString(), TupleFieldNames.EVENT.toString()));
        }
    }

    public static StormTopology createTopology(AlertingStormAttributes attributes,
                                               ZookeperConnectorFactory zookeperConnectorFactory) {
        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout(KAFKA_SPOUT,
                new KafkaSpout<>(createKafkaSpoutConfig(attributes)),
                attributes.getKafkaSpoutNumExecutors());

        builder.setBolt(AlertingEngineType.SIEMBOL_ALERTS.getEngineName(),
                new AlertingEngineBolt(attributes, zookeperConnectorFactory), attributes.getAlertingEngineBoltNumExecutors())
                .localOrShuffleGrouping(KAFKA_SPOUT);

        builder.setBolt(KAFKA_WRITER,
                new KafkaWriterBolt(attributes), attributes.getKafkaWriterBoltNumExecutors())
                .localOrShuffleGrouping(AlertingEngineType.SIEMBOL_ALERTS.getEngineName());

        return builder.createTopology();
    }

    public static StormTopology createCorrelationAlertingTopology(AlertingStormAttributes attributes,
                                                                  ZookeperConnectorFactory zookeperConnectorFactory) {
        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout(KAFKA_SPOUT,
                new KafkaSpout<>(createKafkaSpoutConfig(attributes)),
                attributes.getKafkaSpoutNumExecutors());

        builder.setBolt(AlertingEngineType.SIEMBOL_CORRELATION_ALERTS.getEngineName(),
                new CorrelationAlertingEngineBolt(attributes, zookeperConnectorFactory),
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
        AlertingStormAttributes attributes = new ObjectMapper()
                .readerFor(AlertingStormAttributes.class)
                .readValue(input);

        AlertingEngineType engineType = AlertingEngineType.valueOfName(attributes.getAlertingEngine());

        Config config = new Config();
        config.putAll(attributes.getStormAttributes().getStormConfig());
        ZookeperConnectorFactory zookeperConnectorFactory = new ZookeperConnectorFactory() {};


        StormTopology topology = engineType == AlertingEngineType.SIEMBOL_ALERTS
                ? createTopology(attributes, zookeperConnectorFactory)
                : createCorrelationAlertingTopology(attributes, zookeperConnectorFactory);
        String topologyName = attributes.getTopologyName() != null
                ? attributes.getTopologyName()
                : engineType.toString();
        StormSubmitter.submitTopology(topologyName, config, topology);
    }
}

