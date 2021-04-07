package uk.co.gresearch.siembol.deployment.storm.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum TopologyStateDto {
    @JsonProperty("synchronised")
    SYNCHRONISED,
    @JsonProperty("different")
    DIFFERENT,
    @JsonProperty("desired_state_only")
    DESIRED_STATE_ONLY,
    @JsonProperty("saved_state_only")
    SAVED_STATE_ONLY
}
