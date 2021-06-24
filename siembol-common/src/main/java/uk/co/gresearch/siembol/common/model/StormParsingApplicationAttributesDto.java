package uk.co.gresearch.siembol.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.SchemaIgnore;

import java.io.Serializable;
import java.util.List;

@Attributes(title = "storm parsing application attributes", description = "Storm parsing application attributes")
public class StormParsingApplicationAttributesDto extends AdminConfigDto implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String KAFKA_PRINCIPAL_FORMAT_MSG = "%s.%s";
    private static final String TOPOLOGY_NAME_FORMAT_MSG = "%s-%s";

    @Attributes(description = "The prefix that will be used to create a topology name using application name",
            required = true)
    @JsonProperty("topology.name.prefix")
    private String topologyNamePrefix = "parsing";
    @Attributes(description = "The prefix that will be used to create a kafka producer client id using application name",
            required = true)
    @JsonProperty("client.id.prefix")
    private String clientIdPrefix = "siembol.parsing.writer";
    @JsonProperty("group.id.prefix")
    @Attributes(description = "The prefix that will be used to create a kafka group id reader using application name",
            required = true)
    private String groupIdPrefix = "siembol.parsing.reader";
    @JsonProperty("zookeeper.attributes")
    @Attributes(description = "Zookeeper attributes for updating parser configurations",
            required = true)
    private ZooKeeperAttributesDto zookeeperAttributes;
    @Attributes(description = "Global settings for kafka batch writer used if are not overridden", required = true)
    @JsonProperty("kafka.batch.writer.attributes")
    private KafkaBatchWriterAttributesDto kafkaBatchWriterAttributes;
    @Attributes(description = "Global settings for kafka batch writer used if are not overridden", required = true)
    @JsonProperty("storm.attributes")
    private StormAttributesDto stormAttributes;

    @Attributes(title = "overridden applications",
            description = "List of overridden settings for individual parsing applications")
    @JsonProperty("overridden.applications")
    private List<OverriddenApplicationAttributesDto> overriddenApplications;


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

    public ZooKeeperAttributesDto getZookeeperAttributes() {
        return zookeeperAttributes;
    }

    public void setZookeeperAttributes(ZooKeeperAttributesDto zookeeperAttributes) {
        this.zookeeperAttributes = zookeeperAttributes;
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

    public List<OverriddenApplicationAttributesDto> getOverriddenApplications() {
        return overriddenApplications;
    }

    public void setOverriddenApplications(List<OverriddenApplicationAttributesDto> overriddenApplications) {
        this.overriddenApplications = overriddenApplications;
    }

    public String getTopologyNamePrefix() {
        return topologyNamePrefix;
    }

    public void setTopologyNamePrefix(String topologyNamePrefix) {
        this.topologyNamePrefix = topologyNamePrefix;
    }

    @SchemaIgnore
    @JsonIgnore
    public String getTopologyName(String parsingAppName) {
        return String.format(TOPOLOGY_NAME_FORMAT_MSG, topologyNamePrefix, parsingAppName);
    }

    @SchemaIgnore
    @JsonIgnore
    public String getClientId(String parsingAppName) {
        return String.format(KAFKA_PRINCIPAL_FORMAT_MSG, clientIdPrefix, parsingAppName);
    }

    @SchemaIgnore
    @JsonIgnore
    public String getGroupId(String parsingAppName) {
        return String.format(KAFKA_PRINCIPAL_FORMAT_MSG, groupIdPrefix, parsingAppName);
    }

}
