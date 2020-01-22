package uk.co.gresearch.nortem.common.storm;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class StormAttributes {
    @JsonProperty("bootstrap.servers")
    private String bootstrapServers;
    @JsonProperty(value = "kafka.topics")
    private List<String> kafkaTopics;
    @JsonProperty("first.pool.offset.strategy")
    private String firstPollOffsetStrategy;
    @JsonProperty("kafka.spout.properties")
    private Map<String, Object> kafkaSpoutProperties;
    @JsonProperty("poll.timeout.ms")
    private Long pollTimeoutMs;
    @JsonProperty("offset.commit.period.ms")
    private Long offsetCommitPeriodMs;
    @JsonProperty("max.uncommitted.offsets")
    private Integer maxUncommittedOffsets;
    @JsonProperty("storm.config")
    private Map<String, Object> stormConfig;

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

    public String getFirstPollOffsetStrategy() {
        return firstPollOffsetStrategy;
    }

    public void setFirstPollOffsetStrategy(String firstPollOffsetStrategy) {
        this.firstPollOffsetStrategy = firstPollOffsetStrategy;
    }

    public Map<String, Object> getKafkaSpoutProperties() {
        return kafkaSpoutProperties;
    }

    public void setKafkaSpoutProperties(Map<String, Object> kafkaSpoutProperties) {
        this.kafkaSpoutProperties = kafkaSpoutProperties;
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

    public Map<String, Object> getStormConfig() {
        return stormConfig;
    }

    public void setStormConfig(Map<String, Object> stormConfig) {
        this.stormConfig = stormConfig;
    }
}
