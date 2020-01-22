package uk.co.gresearch.nortem.nikita.storm;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.gresearch.nortem.common.storm.StormAttributes;
import uk.co.gresearch.nortem.common.utils.ZookeperAttributes;

import java.io.Serializable;
import java.util.Map;

public class NikitaStormAttributes implements Serializable {
    @JsonProperty("nikita.engine")
    private String nikitaEngine;
    @JsonProperty("nikita.topology.name")
    private String nikitaTopologyName;
    @JsonProperty("nikita.engine.clean.interval.sec")
    private Integer nikitaEngineCleanIntervalSec;
    @JsonProperty("nikita.input.topic")
    private String nikitaInputTopic;
    @JsonProperty("kafka.error.topic")
    private String kafkaErrorTopic;
    @JsonProperty("nikita.output.topic")
    private String nikitaOutputTopic;
    @JsonProperty("nikita.correlation.output.topic")
    private String nikitaCorrelationOutputTopic;
    @JsonProperty("kafka.producer.properties")
    private Map<String, Object> kafkaProducerProperties;
    @JsonProperty("zookeeper.attributes")
    ZookeperAttributes zookeperAttributes;
    @JsonProperty("storm.attributes")
    private StormAttributes stormAttributes;
    @JsonProperty("kafka.spout.num.executors")
    private Integer kafkaSpoutNumExecutors;
    @JsonProperty("nikita.engine.bolt.num.executors")
    private Integer nikitaEngineBoltNumExecutors;
    @JsonProperty("kafka.writer.bolt.num.executors")
    private Integer kafkaWriterBoltNumExecutors;

    public String getNikitaEngine() {
        return nikitaEngine;
    }

    public void setNikitaEngine(String nikitaEngine) {
        this.nikitaEngine = nikitaEngine;
    }

    public String getNikitaTopologyName() {
        return nikitaTopologyName;
    }

    public void setNikitaTopologyName(String nikitaTopologyName) {
        this.nikitaTopologyName = nikitaTopologyName;
    }

    public Integer getNikitaEngineCleanIntervalSec() {
        return nikitaEngineCleanIntervalSec;
    }

    public void setNikitaEngineCleanIntervalSec(Integer nikitaEngineCleanIntervalSec) {
        this.nikitaEngineCleanIntervalSec = nikitaEngineCleanIntervalSec;
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

    public StormAttributes getStormAttributes() {
        return stormAttributes;
    }

    public void setStormAttributes(StormAttributes stormAttributes) {
        this.stormAttributes = stormAttributes;
    }

    public String getNikitaCorrelationOutputTopic() {
        return nikitaCorrelationOutputTopic;
    }

    public void setNikitaCorrelationOutputTopic(String nikitaCorrelationOutputTopic) {
        this.nikitaCorrelationOutputTopic = nikitaCorrelationOutputTopic;
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

    public Map<String, Object> getKafkaProducerProperties() {
        return kafkaProducerProperties;
    }

    public void setKafkaProducerProperties(Map<String, Object> kafkaProducerProperties) {
        this.kafkaProducerProperties = kafkaProducerProperties;
    }

    public ZookeperAttributes getZookeperAttributes() {
        return zookeperAttributes;
    }

    public void setZookeperAttributes(ZookeperAttributes zookeperAttributes) {
        this.zookeperAttributes = zookeperAttributes;
    }
}
