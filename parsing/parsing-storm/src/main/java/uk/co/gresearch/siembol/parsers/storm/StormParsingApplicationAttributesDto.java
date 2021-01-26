package uk.co.gresearch.siembol.parsers.storm;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;
import uk.co.gresearch.siembol.common.model.AdminConfigDto;
import uk.co.gresearch.siembol.common.model.StormAttributesDto;
import uk.co.gresearch.siembol.common.model.ZookeperAttributesDto;
import uk.co.gresearch.siembol.common.model.KafkaBatchWriterAttributesDto;

import java.io.Serializable;
import java.util.List;

@Attributes(title = "storm parsing application attributes", description = "Storm parsing application attributes")
public class StormParsingApplicationAttributesDto extends AdminConfigDto implements Serializable {
    @Attributes(description = "The prefix that will be used to create a kafka producer client id using application name",
            required = true)
    @JsonProperty("client.id.prefix")
    private String clientIdPrefix = "siembol-parsing-app";
    @JsonProperty("group.id.prefix")
    @Attributes(description = "The prefix that will be used to create a kafka group id reader using application name",
            required = true)
    private String groupIdPrefix = "siembol-parsing-app";;
    @JsonProperty("zookeeper.attributes")
    @Attributes(description = "Zookeeper attributes for updating parser configurations",
            required = true)
    private ZookeperAttributesDto zookeperAttributes;
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

    public ZookeperAttributesDto getZookeperAttributes() {
        return zookeperAttributes;
    }

    public void setZookeperAttributes(ZookeperAttributesDto zookeperAttributes) {
        this.zookeperAttributes = zookeperAttributes;
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
}
