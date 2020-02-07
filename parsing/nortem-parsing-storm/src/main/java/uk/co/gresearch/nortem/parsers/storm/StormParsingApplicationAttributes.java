package uk.co.gresearch.nortem.parsers.storm;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.gresearch.nortem.common.storm.StormAttributes;
import uk.co.gresearch.nortem.common.zookeper.ZookeperAttributes;
import uk.co.gresearch.nortem.common.storm.KafkaBatchWriterAttributes;

import java.io.Serializable;

public class StormParsingApplicationAttributes implements Serializable {
    @JsonProperty("client.id.prefix")
    private String clientIdPrefix;
    @JsonProperty("group.id.prefix")
    private String groupIdPrefix;
    @JsonProperty("zookeeper.attributes")
    private ZookeperAttributes zookeperAttributes;
    @JsonProperty("kafka.batch.writer.attributes")
    private KafkaBatchWriterAttributes kafkaBatchWriterAttributes;
    @JsonProperty("storm.attributes")
    private StormAttributes stormAttributes;

    public String getClientIdPrefix() {
        return clientIdPrefix;
    }

    public void setClientIdPrefix(String clientIdPrefix) {
        this.clientIdPrefix = clientIdPrefix;
    }

    public String getGroupIdPrefix() {
        return groupIdPrefix;
    }

    public void setGroupIdPrefix(String groupIdPrefix) {
        this.groupIdPrefix = groupIdPrefix;
    }

    public ZookeperAttributes getZookeperAttributes() {
        return zookeperAttributes;
    }

    public void setZookeperAttributes(ZookeperAttributes zookeperAttributes) {
        this.zookeperAttributes = zookeperAttributes;
    }

    public KafkaBatchWriterAttributes getKafkaBatchWriterAttributes() {
        return kafkaBatchWriterAttributes;
    }

    public void setKafkaBatchWriterAttributes(KafkaBatchWriterAttributes kafkaBatchWriterAttributes) {
        this.kafkaBatchWriterAttributes = kafkaBatchWriterAttributes;
    }

    public StormAttributes getStormAttributes() {
        return stormAttributes;
    }

    public void setStormAttributes(StormAttributes stormAttributes) {
        this.stormAttributes = stormAttributes;
    }
}
