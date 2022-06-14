package uk.co.gresearch.siembol.common.utils;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;

import java.util.Properties;

public interface KafkaStreamsFactory {
    KafkaStreams createKafkaStreams(Topology topology, Properties properties);
}

