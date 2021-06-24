package uk.co.gresearch.siembol.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

import java.io.Serializable;
import java.util.List;

@Attributes(title = "storm enrichment attributes", description = "Attributes for storm enrichment configuration")
public class StormEnrichmentAttributesDto extends AdminConfigDto implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("topology.name")
    @Attributes(required = true, description = "The name of storm topology")
    private String topologyName = "siembol-enrichment";
    @JsonProperty("kafka.spout.num.executors")
    @Attributes(required = true, description = "The number of executors for kafka spout", minimum = 1)
    private Integer kafkaSpoutNumExecutors = 1;
    @Attributes(required = true, description = "The number of executors for enriching rule engine", minimum = 1)
    @JsonProperty("enriching.engine.bolt.num.executors")
    private Integer enrichingEngineBoltNumExecutors = 1;
    @Attributes(required = true, description = "The number of executors for memory enrichments from tables", minimum = 1)
    @JsonProperty("memory.enriching.bolt.num.executors")
    private Integer memoryEnrichingBoltNumExecutors = 1;
    @JsonProperty("merging.bolt.num.executors")
    @Attributes(required = true, description = "The number of executors for merging enriched fields", minimum = 1)
    private Integer mergingBoltNumExecutors = 1;
    @JsonProperty("kafka.writer.bolt.num.executors")
    @Attributes(required = true, description = "The number of executors for producing output messages", minimum = 1)
    private Integer kafkaWriterBoltNumExecutors = 1;

    @JsonProperty("enriching.rules.zookeeper.attributes")
    @Attributes(title = "rules zookeeper attributes", required = true,
            description = "The zookeeper attributes for configuration enriching rules")
    private ZooKeeperAttributesDto enrichingRulesZookeperAttributes;
    @JsonProperty("enriching.tables.zookeeper.attributes")
    @Attributes(title = "tables zookeeper attributes", required = true,
            description = "The zookeeper attributes for enriching tables")
    private ZooKeeperAttributesDto enrichingTablesAttributes;

    @JsonProperty("kafka.batch.writer.attributes")
    @Attributes(required = true, description = "Kafka batch writer attributes for producing output messages")
    private KafkaBatchWriterAttributesDto kafkaBatchWriterAttributes;
    @Attributes(required = true, description = "Storm attributes for the enrichment topology")
    @JsonProperty("storm.attributes")
    private StormAttributesDto stormAttributes;
    @Attributes(required = true, description = "The kafka input topics for reading messages", minItems = 1)
    @JsonProperty("enriching.input.topics")
    private List<String> enrichingInputTopics;
    @Attributes(required = true, description = "Output kafka topic name for correctly processed messages")
    @JsonProperty("enriching.output.topic")
    private String enrichingOutputTopic;
    @Attributes(required = true, description = "Output kafka topic name for error messages")
    @JsonProperty("enriching.error.topic")
    private String enrichingErrorTopic;
    @Attributes(required = true, description = "the url for hdfs cluster where enriching tables are stored")
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

    public ZooKeeperAttributesDto getEnrichingRulesZookeperAttributes() {
        return enrichingRulesZookeperAttributes;
    }

    public void setEnrichingRulesZookeperAttributes(ZooKeeperAttributesDto enrichingRulesZookeperAttributes) {
        this.enrichingRulesZookeperAttributes = enrichingRulesZookeperAttributes;
    }

    public ZooKeeperAttributesDto getEnrichingTablesAttributes() {
        return enrichingTablesAttributes;
    }

    public void setEnrichingTablesAttributes(ZooKeeperAttributesDto enrichingTablesAttributes) {
        this.enrichingTablesAttributes = enrichingTablesAttributes;
    }

    public KafkaBatchWriterAttributesDto getKafkaBatchWriterAttributes() {
        return kafkaBatchWriterAttributes;
    }

    public void setKafkaBatchWriterAttributes(KafkaBatchWriterAttributesDto kafkaBatchWriterAttributes) {
        this.kafkaBatchWriterAttributes = kafkaBatchWriterAttributes;
    }

    public StormAttributesDto getStormAttributes() {
        return stormAttributes;
    }

    public void setStormAttributes(StormAttributesDto stormAttributes) {
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

    public List<String> getEnrichingInputTopics() {
        return enrichingInputTopics;
    }

    public void setEnrichingInputTopics(List<String> enrichingInputTopics) {
        this.enrichingInputTopics = enrichingInputTopics;
    }
}
