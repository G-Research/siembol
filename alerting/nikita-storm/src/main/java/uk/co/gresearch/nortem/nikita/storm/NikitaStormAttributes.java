package uk.co.gresearch.nortem.nikita.storm;

import repackaged.com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class NikitaStormAttributes implements Serializable {
    @JsonProperty("nikita.engine")
    private String nikitaEngine;
    @JsonProperty("nikita.engine.clean.interval.sec")
    private Integer nikitaEngineCleanIntervalSec;
    @JsonProperty("bootstrap.servers")
    private String bootstrapServers;
    @JsonProperty("group.id")
    private String groupId;
    @JsonProperty("security.protocol")
    private String securityProtocol;
    @JsonProperty("session.timeout.ms")
    private Integer sessionTimeoutMs;
    @JsonProperty("topology.worker.childopts")
    private String topologyWorkerChildopts;
    @JsonProperty("client.id")
    private String clientId;
    @JsonProperty("max.spout.pending")
    private Integer maxSpoutPending;
    @JsonProperty("nikita.input.topic")
    private String nikitaInputTopic;
    @JsonProperty("kafka.error.topic")
    private String kafkaErrorTopic;
    @JsonProperty("nikita.output.topic")
    private String nikitaOutputTopic;
    @JsonProperty("nikita.correlation.output.topic")
    private String nikitaCorrelationOutputTopic;
    @JsonProperty("num.workers")
    private Integer numWorkers;
    @JsonProperty("kafka.spout.num.executors")
    private Integer kafkaSpoutNumExecutors;
    @JsonProperty("nikita.engine.bolt.num.executors")
    private Integer nikitaEngineBoltNumExecutors;
    @JsonProperty("kafka.writer.bolt.num.executors")
    private Integer kafkaWriterBoltNumExecutors;
    @JsonProperty("topology.message.timeout.secs")
    private Integer topologyMessageTimeoutSecs;
    @JsonProperty("zk.url")
    private String zkUrl;
    @JsonProperty("zk.path.nikita.rules")
    private String zkPathNikitaRules;
    @JsonProperty("zk.base.sleep.ms")
    private Integer zkBaseSleepMs;
    @JsonProperty("zk.max.retries")
    private Integer zkMaxRetries;

    public String getNikitaEngine() {
        return nikitaEngine;
    }

    public void setNikitaEngine(String nikitaEngine) {
        this.nikitaEngine = nikitaEngine;
    }

    public Integer getNikitaEngineCleanIntervalSec() {
        return nikitaEngineCleanIntervalSec;
    }

    public void setNikitaEngineCleanIntervalSec(Integer nikitaEngineCleanIntervalSec) {
        this.nikitaEngineCleanIntervalSec = nikitaEngineCleanIntervalSec;
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getSecurityProtocol() {
        return securityProtocol;
    }

    public void setSecurityProtocol(String securityProtocol) {
        this.securityProtocol = securityProtocol;
    }

    public Integer getSessionTimeoutMs() {
        return sessionTimeoutMs;
    }

    public void setSessionTimeoutMs(Integer sessionTimeoutMs) {
        this.sessionTimeoutMs = sessionTimeoutMs;
    }

    public String getTopologyWorkerChildopts() {
        return topologyWorkerChildopts;
    }

    public void setTopologyWorkerChildopts(String topologyWorkerChildopts) {
        this.topologyWorkerChildopts = topologyWorkerChildopts;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Integer getMaxSpoutPending() {
        return maxSpoutPending;
    }

    public void setMaxSpoutPending(Integer maxSpoutPending) {
        this.maxSpoutPending = maxSpoutPending;
    }

    public String getNikitaInputTopic() {
        return nikitaInputTopic;
    }

    public void setNikitaInputTopic(String nikitaInputTopic) {
        this.nikitaInputTopic = nikitaInputTopic;
    }

    public String getKafkaErrorTopic() {
        return kafkaErrorTopic;
    }

    public void setKafkaErrorTopic(String kafkaErrorTopic) {
        this.kafkaErrorTopic = kafkaErrorTopic;
    }

    public String getNikitaOutputTopic() {
        return nikitaOutputTopic;
    }

    public void setNikitaOutputTopic(String nikitaOutputTopic) {
        this.nikitaOutputTopic = nikitaOutputTopic;
    }

    public String getNikitaCorrelationOutputTopic() {
        return nikitaCorrelationOutputTopic;
    }

    public void setNikitaCorrelationOutputTopic(String nikitaCorrelationOutputTopic) {
        this.nikitaCorrelationOutputTopic = nikitaCorrelationOutputTopic;
    }

    public Integer getNumWorkers() {
        return numWorkers;
    }

    public void setNumWorkers(Integer numWorkers) {
        this.numWorkers = numWorkers;
    }

    public Integer getKafkaSpoutNumExecutors() {
        return kafkaSpoutNumExecutors;
    }

    public void setKafkaSpoutNumExecutors(Integer kafkaSpoutNumExecutors) {
        this.kafkaSpoutNumExecutors = kafkaSpoutNumExecutors;
    }

    public Integer getNikitaEngineBoltNumExecutors() {
        return nikitaEngineBoltNumExecutors;
    }

    public void setNikitaEngineBoltNumExecutors(Integer nikitaEngineBoltNumExecutors) {
        this.nikitaEngineBoltNumExecutors = nikitaEngineBoltNumExecutors;
    }

    public Integer getKafkaWriterBoltNumExecutors() {
        return kafkaWriterBoltNumExecutors;
    }

    public void setKafkaWriterBoltNumExecutors(Integer kafkaWriterBoltNumExecutors) {
        this.kafkaWriterBoltNumExecutors = kafkaWriterBoltNumExecutors;
    }

    public Integer getTopologyMessageTimeoutSecs() {
        return topologyMessageTimeoutSecs;
    }

    public void setTopologyMessageTimeoutSecs(Integer topologyMessageTimeoutSecs) {
        this.topologyMessageTimeoutSecs = topologyMessageTimeoutSecs;
    }

    public String getZkUrl() {
        return zkUrl;
    }

    public void setZkUrl(String zkUrl) {
        this.zkUrl = zkUrl;
    }

    public String getZkPathNikitaRules() {
        return zkPathNikitaRules;
    }

    public void setZkPathNikitaRules(String zkPathNikitaRules) {
        this.zkPathNikitaRules = zkPathNikitaRules;
    }

    public Integer getZkBaseSleepMs() {
        return zkBaseSleepMs;
    }

    public void setZkBaseSleepMs(Integer zkBaseSleepMs) {
        this.zkBaseSleepMs = zkBaseSleepMs;
    }

    public Integer getZkMaxRetries() {
        return zkMaxRetries;
    }

    public void setZkMaxRetries(Integer zkMaxRetries) {
        this.zkMaxRetries = zkMaxRetries;
    }
}
