package uk.co.gresearch.siembol.common.error;

import com.fasterxml.jackson.annotation.JsonProperty;
/**
 * An enum for representing a Siembol error types used in an error message
 *
 * @author  Marian Novotny
 *
 * @see #PARSER_ERROR
 * @see #ALERTING_ERROR
 * @see #ENRICHMENT_ERROR
 * @see #RESPONSE_ERROR
 * @see #DEFAULT_ERROR
 */
public enum ErrorType {
    @JsonProperty("parser_error") PARSER_ERROR,
    @JsonProperty("alerting_error") ALERTING_ERROR,
    @JsonProperty("enrichment_error") ENRICHMENT_ERROR,
    @JsonProperty("response_error") RESPONSE_ERROR,
    @JsonProperty("error") DEFAULT_ERROR;
}
