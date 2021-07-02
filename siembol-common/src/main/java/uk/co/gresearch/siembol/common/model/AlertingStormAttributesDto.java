package uk.co.gresearch.siembol.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;
import uk.co.gresearch.siembol.common.jsonschema.JsonRawStringDto;

import java.io.Serializable;
import java.util.List;

@Attributes(title = "storm alerting attributes", description = "Attributes for siembol alerting")
public class AlertingStormAttributesDto extends AdminConfigDto implements Serializable {
    private static final long serialVersionUID = 1L;
    @Attributes(required = true, description = "The type of siembol alerting engine")
    @JsonProperty("alerts.engine")
    private String alertingEngine;
    @Attributes(required = true, description = "The name of storm topology")
    @JsonProperty("alerts.topology.name")
    private String topologyName;
    @Attributes(description = "The number of seconds for cleaning correlation context", minimum = 1)
    @JsonProperty("alerts.engine.clean.interval.sec")
    private Integer alertingEngineCleanIntervalSec = 1;
    @Attributes(required = true, description = "The kafka input topics for reading messages", minItems = 1)
    @JsonProperty("alerts.input.topics")
    private List<String> inputTopics;
    @Attributes(required = true, description = "The kafka error topic for error messages")
    @JsonProperty("kafka.error.topic")
    private String kafkaErrorTopic;
    @Attributes(required = true, description = "The kafka output topic for producing alerts")
    @JsonProperty("alerts.output.topic")
    private String outputTopic;
    @Attributes(required = true, description = "The kafka topic for alerts used for correlation by siembol correlation engine")
    @JsonProperty("alerts.correlation.output.topic")
    private String correlationOutputTopic;
    @JsonProperty("kafka.producer.properties")
    @Attributes(required = true, description = "Defines kafka producer properties")
    private JsonRawStringDto kafkaProducerProperties;
    @JsonProperty("zookeeper.attributes")
    @Attributes(required = true, description = "The zookeeper attributes for alerting rules")
    private ZooKeeperAttributesDto zookeperAttributes;
    @Attributes(required = true, description = "Storm attributes for the topology")
    @JsonProperty("storm.attributes")
    private StormAttributesDto stormAttributes;
    @Attributes(required = true, description = "The number of executors for reading from kafka input topic", minimum = 1)
    @JsonProperty("kafka.spout.num.executors")
    private Integer kafkaSpoutNumExecutors = 1;
    @Attributes(required = true, description = "The number of executors for evaluating alerting rules", minimum = 1)
    @JsonProperty("alerts.engine.bolt.num.executors")
    private Integer AlertingEngineBoltNumExecutors = 1;
    @Attributes(required = true, description = "The number of executors for producing alerts to output topic", minimum = 1)
    @JsonProperty("kafka.writer.bolt.num.executors")
    private Integer kafkaWriterBoltNumExecutors = 1;

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

    public List<String> getInputTopics() {
        return inputTopics;
    }

    public void setInputTopics(List<String> inputTopics) {
        this.inputTopics = inputTopics;
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

    public StormAttributesDto getStormAttributes() {
        return stormAttributes;
    }

    public void setStormAttributes(StormAttributesDto stormAttributes) {
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

    public ZooKeeperAttributesDto getZookeperAttributes() {
        return zookeperAttributes;
    }

    public void setZookeperAttributes(ZooKeeperAttributesDto zookeperAttributes) {
        this.zookeperAttributes = zookeperAttributes;
    }

    public JsonRawStringDto getKafkaProducerProperties() {
        return kafkaProducerProperties;
    }

    public void setKafkaProducerProperties(JsonRawStringDto kafkaProducerProperties) {
        this.kafkaProducerProperties = kafkaProducerProperties;
    }
}
