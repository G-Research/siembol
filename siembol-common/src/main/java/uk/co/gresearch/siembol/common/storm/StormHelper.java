package uk.co.gresearch.siembol.common.storm;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.storm.kafka.spout.Func;
import org.apache.storm.kafka.spout.KafkaSpoutConfig;
import org.apache.storm.kafka.spout.KafkaSpoutRetryExponentialBackoff;
import org.apache.storm.kafka.spout.KafkaSpoutRetryService;
import org.apache.storm.tuple.Fields;
import uk.co.gresearch.siembol.common.model.StormAttributesDto;

import java.util.List;
import java.util.Properties;

public class StormHelper {
    private static final int KAFKA_SPOUT_INIT_DELAY_MICRO_SEC = 500;
    private static final int KAFKA_SPOUT_DELAY_PERIOD_MILLI_SEC = 2;
    private static final int KAFKA_SPOUT_MAX_RETRIES = Integer.MAX_VALUE;
    private static final int KAFKA_SPOUT_MAX_DELAY_SEC = 10;

    public static <K, V> KafkaSpoutConfig<K, V> createKafkaSpoutConfig(
            StormAttributesDto stormAttributes,
            Func<ConsumerRecord<K,V>, List<Object>> func,
            Fields fields) {
        KafkaSpoutRetryService kafkaSpoutRetryService = new KafkaSpoutRetryExponentialBackoff(
                KafkaSpoutRetryExponentialBackoff.TimeInterval.microSeconds(KAFKA_SPOUT_INIT_DELAY_MICRO_SEC),
                KafkaSpoutRetryExponentialBackoff.TimeInterval.milliSeconds(KAFKA_SPOUT_DELAY_PERIOD_MILLI_SEC),
                KAFKA_SPOUT_MAX_RETRIES,
                KafkaSpoutRetryExponentialBackoff.TimeInterval.seconds(KAFKA_SPOUT_MAX_DELAY_SEC));

        KafkaSpoutConfig.FirstPollOffsetStrategy pollStrategy = stormAttributes.getFirstPollOffsetStrategy()
                .getKafkaSpoutStrategy();

        Properties props = new Properties();
        props.putAll(stormAttributes.getKafkaSpoutProperties().getRawMap());

        KafkaSpoutConfig.Builder<K, V> builder =  new KafkaSpoutConfig.Builder<K, V>(
                stormAttributes.getBootstrapServers(),
                stormAttributes.getKafkaTopics())
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
}
