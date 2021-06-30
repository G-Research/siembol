package uk.co.gresearch.siembol.response.stream.ruleservice;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;

import java.io.Closeable;
import java.util.Properties;

public class TestingDriverKafkaStreamsFactory implements KafkaStreamsFactory, Closeable {
    private final KafkaStreams streams;
    private TopologyTestDriver testDriver;

    public TestingDriverKafkaStreamsFactory(KafkaStreams streams) {
        this.streams = streams;
    }

    @Override
    public KafkaStreams createKafkaStreams(Topology topology, Properties properties) {
        if (testDriver != null) {
            throw new IllegalStateException("Create kafka streams can be called only once");
        }

        testDriver = new TopologyTestDriver(topology, properties);
        return streams;
    }

    @Override
    public void close() {
        if (testDriver != null) {
            testDriver.close();
        }
    }

    public TopologyTestDriver getTestDriver() {
        return testDriver;
    }
}
