package uk.co.gresearch.siembol.common.storm;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.storm.kafka.spout.FirstPollOffsetStrategy;


public enum FirstPoolOffsetStrategy {
    @JsonProperty("EARLIEST")
    EARLIEST("EARLIEST", FirstPollOffsetStrategy.EARLIEST),
    @JsonProperty("LATEST")
    LATEST("LATEST", FirstPollOffsetStrategy.LATEST),
    @JsonProperty("UNCOMMITTED_EARLIEST")
    UNCOMMITTED_EARLIEST("UNCOMMITTED_EARLIEST", FirstPollOffsetStrategy.UNCOMMITTED_EARLIEST),
    @JsonProperty("UNCOMMITTED_LATEST")
    UNCOMMITTED_LATEST("UNCOMMITTED_LATEST", FirstPollOffsetStrategy.UNCOMMITTED_LATEST);

    private final String name;
    private final FirstPollOffsetStrategy kafkaSpoutStrategy;

    FirstPoolOffsetStrategy(String name, FirstPollOffsetStrategy kafkaSpoutStrategy) {
        this.name = name;
        this.kafkaSpoutStrategy = kafkaSpoutStrategy;
    }

    @Override
    public String toString() {
        return name;
    }

    public FirstPollOffsetStrategy getKafkaSpoutStrategy() {
        return kafkaSpoutStrategy;
    }
}
