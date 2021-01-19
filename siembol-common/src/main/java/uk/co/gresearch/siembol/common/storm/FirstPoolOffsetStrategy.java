package uk.co.gresearch.siembol.common.storm;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.storm.kafka.spout.KafkaSpoutConfig;

public enum FirstPoolOffsetStrategy {
    @JsonProperty("EARLIEST")
    EARLIEST("EARLIEST", KafkaSpoutConfig.FirstPollOffsetStrategy.EARLIEST),
    @JsonProperty("LATEST")
    LATEST("LATEST", KafkaSpoutConfig.FirstPollOffsetStrategy.LATEST),
    @JsonProperty("UNCOMMITTED_EARLIEST")
    UNCOMMITTED_EARLIEST("UNCOMMITTED_EARLIEST", KafkaSpoutConfig.FirstPollOffsetStrategy.UNCOMMITTED_EARLIEST),
    @JsonProperty("UNCOMMITTED_LATEST")
    UNCOMMITTED_LATEST("UNCOMMITTED_LATEST", KafkaSpoutConfig.FirstPollOffsetStrategy.UNCOMMITTED_LATEST);

    private final String name;
    private final KafkaSpoutConfig.FirstPollOffsetStrategy kafkaSpoutStrategy;

    FirstPoolOffsetStrategy(String name, KafkaSpoutConfig.FirstPollOffsetStrategy kafkaSpoutStrategy) {
        this.name = name;
        this.kafkaSpoutStrategy = kafkaSpoutStrategy;
    }

    @Override
    public String toString() {
        return name;
    }

    public KafkaSpoutConfig.FirstPollOffsetStrategy getKafkaSpoutStrategy() {
        return kafkaSpoutStrategy;
    }
}
