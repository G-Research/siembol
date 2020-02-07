package uk.co.gresearch.nortem.enrichments.storm.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.gresearch.nortem.common.storm.KafkaBatchWriterAttributes;
import uk.co.gresearch.nortem.common.storm.StormAttributes;
import uk.co.gresearch.nortem.common.zookeper.ZookeperAttributes;

import java.io.Serializable;

public class StormEnrichmentAttributes implements Serializable {
    @JsonProperty("topology.name")
    private String topologyName;
    @JsonProperty("kafka.spout.num.executors")
    private Integer kafkaSpoutNumExecutors;
    @JsonProperty("enriching.engine.bolt.num.executors")
    private Integer enrichingEngineBoltNumExecutors;
    @JsonProperty("memory.enriching.bolt.num.executors")
    private Integer memoryEnrichingBoltNumExecutors;
    @JsonProperty("merging.bolt.num.executors")
    private Integer mergingBoltNumExecutors;
    @JsonProperty("kafka.writer.bolt.num.executors")
    private Integer kafkaWriterBoltNumExecutors;
    @JsonProperty("enriching.rules.zookeeper.attributes")
    private ZookeperAttributes enrichingRulesZookeperAttributes;
    @JsonProperty("enriching.tables.zookeeper.attributes")
    private ZookeperAttributes enrichingTablesAttributes;
    @JsonProperty("kafka.batch.writer.attributes")
    private KafkaBatchWriterAttributes kafkaBatchWriterAttributes;
    @JsonProperty("storm.attributes")
    private StormAttributes stormAttributes;
    @JsonProperty("enriching.output.topic")
    private String enrichingOutputTopic;
    @JsonProperty("enriching.error.topic")
    private String enrichingErrorTopic;
    @JsonProperty("enriching.tables.hdfs.uri")
    private String enrichingTablesHdfsUri;

    public String getTopologyName() {
        return topologyName;
    }

    public void setTopologyName(String topologyName) {
        this.topologyName = topologyName;
    }

    public Integer getKafkaSpoutNumExecutors() {
        return kafkaSpoutNumExecutors;
    }

    public void setKafkaSpoutNumExecutors(Integer kafkaSpoutNumExecutors) {
        this.kafkaSpoutNumExecutors = kafkaSpoutNumExecutors;
    }

    public Integer getEnrichingEngineBoltNumExecutors() {
        return enrichingEngineBoltNumExecutors;
    }

    public void setEnrichingEngineBoltNumExecutors(Integer enrichingEngineBoltNumExecutors) {
        this.enrichingEngineBoltNumExecutors = enrichingEngineBoltNumExecutors;
    }

    public Integer getMemoryEnrichingBoltNumExecutors() {
        return memoryEnrichingBoltNumExecutors;
    }

    public void setMemoryEnrichingBoltNumExecutors(Integer memoryEnrichingBoltNumExecutors) {
        this.memoryEnrichingBoltNumExecutors = memoryEnrichingBoltNumExecutors;
    }

    public Integer getKafkaWriterBoltNumExecutors() {
        return kafkaWriterBoltNumExecutors;
    }

    public void setKafkaWriterBoltNumExecutors(Integer kafkaWriterBoltNumExecutors) {
        this.kafkaWriterBoltNumExecutors = kafkaWriterBoltNumExecutors;
    }

    public ZookeperAttributes getEnrichingRulesZookeperAttributes() {
        return enrichingRulesZookeperAttributes;
    }

    public void setEnrichingRulesZookeperAttributes(ZookeperAttributes enrichingRulesZookeperAttributes) {
        this.enrichingRulesZookeperAttributes = enrichingRulesZookeperAttributes;
    }

    public ZookeperAttributes getEnrichingTablesAttributes() {
        return enrichingTablesAttributes;
    }

    public void setEnrichingTablesAttributes(ZookeperAttributes enrichingTablesAttributes) {
        this.enrichingTablesAttributes = enrichingTablesAttributes;
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

    public Integer getMergingBoltNumExecutors() {
        return mergingBoltNumExecutors;
    }

    public void setMergingBoltNumExecutors(Integer mergingBoltNumExecutors) {
        this.mergingBoltNumExecutors = mergingBoltNumExecutors;
    }

    public String getEnrichingOutputTopic() {
        return enrichingOutputTopic;
    }

    public void setEnrichingOutputTopic(String enrichingOutputTopic) {
        this.enrichingOutputTopic = enrichingOutputTopic;
    }

    public String getEnrichingErrorTopic() {
        return enrichingErrorTopic;
    }

    public void setEnrichingErrorTopic(String enrichingErrorTopic) {
        this.enrichingErrorTopic = enrichingErrorTopic;
    }

    public String getEnrichingTablesHdfsUri() {
        return enrichingTablesHdfsUri;
    }

    public void setEnrichingTablesHdfsUri(String enrichingTablesHdfsUri) {
        this.enrichingTablesHdfsUri = enrichingTablesHdfsUri;
    }
}
