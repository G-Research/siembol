package uk.co.gresearch.siembol.common.storm;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.storm.kafka.spout.*;
import org.apache.storm.tuple.Fields;
import uk.co.gresearch.siembol.common.model.FirstPoolOffsetStrategyDto;
import uk.co.gresearch.siembol.common.model.StormAttributesDto;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;

public class StormHelper {
    private static final int KAFKA_SPOUT_INIT_DELAY_MICRO_SEC = 500;
    private static final int KAFKA_SPOUT_DELAY_PERIOD_MILLI_SEC = 2;
    private static final int KAFKA_SPOUT_MAX_RETRIES = Integer.MAX_VALUE;
    private static final int KAFKA_SPOUT_MAX_DELAY_SEC = 10;
    private static final String UNSUPPORTED_SPOUT_STRATEGY = "Unsupported spout strategy";

    public static <K, V> KafkaSpoutConfig<K, V> createKafkaSpoutConfig(
            StormAttributesDto stormAttributes,
            Func<ConsumerRecord<K,V>, List<Object>> func,
            Fields fields) {
        KafkaSpoutRetryService kafkaSpoutRetryService = new KafkaSpoutRetryExponentialBackoff(
                KafkaSpoutRetryExponentialBackoff.TimeInterval.microSeconds(KAFKA_SPOUT_INIT_DELAY_MICRO_SEC),
                KafkaSpoutRetryExponentialBackoff.TimeInterval.milliSeconds(KAFKA_SPOUT_DELAY_PERIOD_MILLI_SEC),
                KAFKA_SPOUT_MAX_RETRIES,
                KafkaSpoutRetryExponentialBackoff.TimeInterval.seconds(KAFKA_SPOUT_MAX_DELAY_SEC));

        FirstPollOffsetStrategy pollStrategy = getKafkaSpoutStrategy(stormAttributes.getFirstPollOffsetStrategy());
        Properties props = new Properties();
        props.putAll(stormAttributes.getKafkaSpoutProperties().getRawMap());

        KafkaSpoutConfig.Builder<K, V> builder =  new KafkaSpoutConfig.Builder<K, V>(
                stormAttributes.getBootstrapServers(),
                new HashSet<>(stormAttributes.getKafkaTopics()))
                .setFirstPollOffsetStrategy(pollStrategy)
                .setProp(props)
                .setRetry(kafkaSpoutRetryService)
                .setRecordTranslator(func, fields);

        if (stormAttributes.getPollTimeoutMs() != null) {
            builder.setPollTimeoutMs(stormAttributes.getPollTimeoutMs());
        }

        if (stormAttributes.getOffsetCommitPeriodMs() != null) {
            builder.setOffsetCommitPeriodMs(stormAttributes.getOffsetCommitPeriodMs());
        }

        if (stormAttributes.getMaxUncommittedOffsets() != null) {
            builder.setMaxUncommittedOffsets(stormAttributes.getMaxUncommittedOffsets());
        }

        return builder.build();
    }

    private static FirstPollOffsetStrategy getKafkaSpoutStrategy(FirstPoolOffsetStrategyDto strategy) {
        switch (strategy) {
            case EARLIEST:
                return FirstPollOffsetStrategy.EARLIEST;
            case LATEST:
                return FirstPollOffsetStrategy.LATEST;
            case UNCOMMITTED_LATEST:
                return FirstPollOffsetStrategy.UNCOMMITTED_LATEST;
            case UNCOMMITTED_EARLIEST:
                return FirstPollOffsetStrategy.UNCOMMITTED_EARLIEST;
            default:
                throw new IllegalArgumentException(UNSUPPORTED_SPOUT_STRATEGY);
        }
    }
}
