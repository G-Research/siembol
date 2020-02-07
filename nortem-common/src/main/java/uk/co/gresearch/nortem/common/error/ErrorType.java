package uk.co.gresearch.nortem.common.error;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ErrorType {
    @JsonProperty("parser_error") PARSER_ERROR,
    @JsonProperty("alerting_error") ALERTING_ERROR,
    @JsonProperty("enrichment_error") ENRICHMENT_ERROR,
    @JsonProperty("error") DEFAULT_ERROR;
}
