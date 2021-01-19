package uk.co.gresearch.siembol.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;
import uk.co.gresearch.siembol.common.jsonschema.JsonRawStringDto;
import uk.co.gresearch.siembol.common.storm.FirstPoolOffsetStrategy;

import java.util.List;

@Attributes(title = "storm attributes", description = "Attributes for storm configuration")
public class StormAttributesDto {
    @JsonProperty("bootstrap.servers")
    @Attributes(required = true, description = "Kafka brokers servers url. Multiple servers are separated by coma")
    private String bootstrapServers;
    @Attributes(required = true, description = "List of input kafka topics for storm kafka spout", minItems = 1)
    @JsonProperty(value = "kafka.topics")
    private List<String> kafkaTopics;
    @Attributes(required = true, description = "Defines how the kafka spout seeks the offset to be used in the first poll to kafka")
    @JsonProperty("first.pool.offset.strategy")
    private FirstPoolOffsetStrategy firstPollOffsetStrategy = FirstPoolOffsetStrategy.UNCOMMITTED_LATEST;
    @Attributes(required = true, description = "Defines kafka consumer attributes for kafka spout such as group.id, " +
            "protocol, https://kafka.apache.org/0102/documentation.html#consumerconfigs")
    @JsonProperty("kafka.spout.properties")
    private JsonRawStringDto kafkaSpoutProperties;
    @Attributes(description = "Kafka consumer parameter poll.timeout.ms used in kafka spout")
    @JsonProperty("poll.timeout.ms")
    private Long pollTimeoutMs;
    @Attributes(description = "Specifies the period of time (in milliseconds) after which the spout commits to Kafka, " +
            "https://docs.cloudera.com/HDPDocuments/HDP3/HDP-3.1.5/storm-moving-data/content/tuning_kafkaspout_performance.html")
    @JsonProperty("offset.commit.period.ms")
    private Long offsetCommitPeriodMs;
    @Attributes(description = "defines the maximum number of polled offsets (records) that can be pending commit before another poll can take place" +
            "https://docs.cloudera.com/HDPDocuments/HDP3/HDP-3.1.5/storm-moving-data/content/tuning_kafkaspout_performance.html")
    @JsonProperty("max.uncommitted.offsets")
    private Integer maxUncommittedOffsets;
    @Attributes(required = true, description = "Defines storm attributes for a topology, " +
            "https://storm.apache.org/releases/current/Configuration.html")
    @JsonProperty("storm.config")
    private JsonRawStringDto stormConfig;

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public List<String> getKafkaTopics() {
        return kafkaTopics;
    }

    public void setKafkaTopics(List<String> kafkaTopics) {
        this.kafkaTopics = kafkaTopics;
    }

    public Long getPollTimeoutMs() {
        return pollTimeoutMs;
    }

    public void setPollTimeoutMs(Long pollTimeoutMs) {
        this.pollTimeoutMs = pollTimeoutMs;
    }

    public Long getOffsetCommitPeriodMs() {
        return offsetCommitPeriodMs;
    }

    public void setOffsetCommitPeriodMs(Long offsetCommitPeriodMs) {
        this.offsetCommitPeriodMs = offsetCommitPeriodMs;
    }

    public Integer getMaxUncommittedOffsets() {
        return maxUncommittedOffsets;
    }

    public void setMaxUncommittedOffsets(Integer maxUncommittedOffsets) {
        this.maxUncommittedOffsets = maxUncommittedOffsets;
    }

    public JsonRawStringDto getStormConfig() {
        return stormConfig;
    }

    public void setStormConfig(JsonRawStringDto stormConfig) {
        this.stormConfig = stormConfig;
    }

    public FirstPoolOffsetStrategy getFirstPollOffsetStrategy() {
        return firstPollOffsetStrategy;
    }

    public void setFirstPollOffsetStrategy(FirstPoolOffsetStrategy firstPollOffsetStrategy) {
        this.firstPollOffsetStrategy = firstPollOffsetStrategy;
    }

    public JsonRawStringDto getKafkaSpoutProperties() {
        return kafkaSpoutProperties;
    }

    public void setKafkaSpoutProperties(JsonRawStringDto kafkaSpoutProperties) {
        this.kafkaSpoutProperties = kafkaSpoutProperties;
    }
}
