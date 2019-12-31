package uk.co.gresearch.nortem.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class StormTopologyAttributes {

    @JsonProperty("bootstrap_servers")
    private String bootstrapServers;

    @JsonProperty("first_pool_offset_strategy")
    private String firstPollOffsetStrategy;

    @JsonProperty("kafka_spout_properties")
    private Map<String, Object> kafkaSpoutConfig;
    @JsonProperty("storm_properties")
    private Map<String, Object> stormConfig;

}
