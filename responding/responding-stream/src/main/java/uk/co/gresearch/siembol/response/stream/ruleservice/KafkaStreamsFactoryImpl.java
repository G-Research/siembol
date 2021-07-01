package uk.co.gresearch.siembol.response.stream.ruleservice;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Properties;

public class KafkaStreamsFactoryImpl implements KafkaStreamsFactory {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String UNCAUGHT_EXCEPTION = "Uncaught exception in siembol response kafka streams";

    @Override
    public KafkaStreams createKafkaStreams(Topology topology, Properties properties) {
        KafkaStreams kafkaStreams = new KafkaStreams(topology, properties);
        kafkaStreams.setUncaughtExceptionHandler(e -> {
            LOG.error(UNCAUGHT_EXCEPTION, e);
            return StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.SHUTDOWN_APPLICATION;
        });
        return kafkaStreams;
    }
}
