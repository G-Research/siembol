package uk.co.gresearch.siembol.alerts.storm.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.gresearch.siembol.common.storm.StormAttributes;
import uk.co.gresearch.siembol.common.zookeper.ZookeperAttributes;

import java.io.Serializable;
import java.util.Map;

public class AlertingStormAttributes implements Serializable {
    @JsonProperty("alerts.engine")
    private String alertingEngine;
    @JsonProperty("alerts.topology.name")
    private String topologyName;
    @JsonProperty("alerts.engine.clean.interval.sec")
    private Integer alertingEngineCleanIntervalSec;
    @JsonProperty("alerts.input.topic")
    private String inputTopic;
    @JsonProperty("kafka.error.topic")
    private String kafkaErrorTopic;
    @JsonProperty("alerts.output.topic")
    private String outputTopic;
    @JsonProperty("alerts.correlation.output.topic")
    private String correlationOutputTopic;
    @JsonProperty("kafka.producer.properties")
    private Map<String, Object> kafkaProducerProperties;
    @JsonProperty("zookeeper.attributes")
    ZookeperAttributes zookeperAttributes;
    @JsonProperty("storm.attributes")
    private StormAttributes stormAttributes;
    @JsonProperty("kafka.spout.num.executors")
    private Integer kafkaSpoutNumExecutors;
    @JsonProperty("alerts.engine.bolt.num.executors")
    private Integer AlertingEngineBoltNumExecutors;
    @JsonProperty("kafka.writer.bolt.num.executors")
    private Integer kafkaWriterBoltNumExecutors;

    public String getAlertingEngine() {
        return alertingEngine;
    }

    public void setAlertingEngine(String AlertingEngine) {
        this.alertingEngine = AlertingEngine;
    }

    public String getTopologyName() {
        return topologyName;
    }

    public void setTopologyName(String topologyName) {
        this.topologyName = topologyName;
    }

    public Integer getAlertingEngineCleanIntervalSec() {
        return alertingEngineCleanIntervalSec;
    }

    public void setAlertingEngineCleanIntervalSec(Integer AlertingEngineCleanIntervalSec) {
        this.alertingEngineCleanIntervalSec = AlertingEngineCleanIntervalSec;
    }

    public String getInputTopic() {
        return inputTopic;
    }

    public void setInputTopic(String inputTopic) {
        this.inputTopic = inputTopic;
    }

    public String getKafkaErrorTopic() {
        return kafkaErrorTopic;
    }

    public void setKafkaErrorTopic(String kafkaErrorTopic) {
        this.kafkaErrorTopic = kafkaErrorTopic;
    }

    public String getOutputTopic() {
        return outputTopic;
    }

    public void setOutputTopic(String outputTopic) {
        this.outputTopic = outputTopic;
    }

    public StormAttributes getStormAttributes() {
        return stormAttributes;
    }

    public void setStormAttributes(StormAttributes stormAttributes) {
        this.stormAttributes = stormAttributes;
    }

    public String getCorrelationOutputTopic() {
        return correlationOutputTopic;
    }

    public void setCorrelationOutputTopic(String correlationOutputTopic) {
        this.correlationOutputTopic = correlationOutputTopic;
    }

    public Integer getKafkaSpoutNumExecutors() {
        return kafkaSpoutNumExecutors;
    }

    public void setKafkaSpoutNumExecutors(Integer kafkaSpoutNumExecutors) {
        this.kafkaSpoutNumExecutors = kafkaSpoutNumExecutors;
    }

    public Integer getAlertingEngineBoltNumExecutors() {
        return AlertingEngineBoltNumExecutors;
    }

    public void setAlertingEngineBoltNumExecutors(Integer AlertingEngineBoltNumExecutors) {
        this.AlertingEngineBoltNumExecutors = AlertingEngineBoltNumExecutors;
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
