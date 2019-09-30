package uk.co.gresearch.nortem.parsers.storm;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public class StormParsingApplicationAttributes implements Serializable {
    @JsonProperty("bootstrap.servers")
    private String bootstrapServers;
    @JsonProperty("group.id.prefix")
    private String groupIdPrefix;
    @JsonIgnore
    private String groupId;
    @JsonProperty("security.protocol")
    private String securityProtocol;
    @JsonProperty("session.timeout.ms")
    private Integer sessionTimeoutMs;
    @JsonProperty("topology.worker.childopts")
    private String topologyWorkerChildopts;
    @JsonProperty("client.id.prefix")
    private String clientIdPrefix;
    @JsonIgnore
    private String clientId;
    @JsonProperty("max.spout.pending")
    private Integer maxSpoutPending;
    @JsonProperty("num.workers")
    private Integer numWorkers;
    @JsonProperty("topology.message.timeout.secs")
    private Integer topologyMessageTimeoutSecs;
    @JsonProperty("zk.url")
    private String zkUrl;
    @JsonProperty("zk.path.parserconfig")
    private String zkPathParserConfigs;
    @JsonProperty("zk.base.sleep.ms")
    private Integer zkBaseSleepMs;
    @JsonProperty("zk.max.retries")
    private Integer zkMaxRetries;

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public String getGroupIdPrefix() {
        return groupIdPrefix;
    }

    public void setGroupIdPrefix(String groupIdPrefix) {
        this.groupIdPrefix = groupIdPrefix;
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

    public String getClientIdPrefix() {
        return clientIdPrefix;
    }

    public void setClientIdPrefix(String clientIdPrefix) {
        this.clientIdPrefix = clientIdPrefix;
    }

    public Integer getMaxSpoutPending() {
        return maxSpoutPending;
    }

    public void setMaxSpoutPending(Integer maxSpoutPending) {
        this.maxSpoutPending = maxSpoutPending;
    }

    public Integer getNumWorkers() {
        return numWorkers;
    }

    public void setNumWorkers(Integer numWorkers) {
        this.numWorkers = numWorkers;
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

    public String getZkPathParserConfigs() {
        return zkPathParserConfigs;
    }

    public void setZkPathParserConfigs(String zkPathParserConfigs) {
        this.zkPathParserConfigs = zkPathParserConfigs;
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

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
