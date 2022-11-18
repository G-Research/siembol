package uk.co.gresearch.siembol.parsers.application.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

import java.util.List;
/**
 * A data transfer object for representing parsing application settings
 *
 * <p>This class is used for json (de)serialisation of parsing application settings and
 * for generating json schema from this class using annotations.
 *
 * @author  Marian Novotny
 *
 * @see com.github.reinert.jjschema.Attributes
 * @see com.fasterxml.jackson.annotation.JsonProperty
 *
 */
@Attributes(title = "parsing application settings", description = "Parser application settings")
public class ParsingApplicationSettingsDto {
    @JsonProperty("parsing_app_type")
    @Attributes(description = "The type of the parsing application")
    private ParsingApplicationTypeDto applicationType;

    @JsonProperty("input_topics")
    @Attributes(description = "The kafka topics for reading messages for parsing", required = true, minItems = 1)
    private List<String> inputTopics;

    @JsonProperty("error_topic")
    @Attributes(description = "The kafka topic for publishing error messages", required = true)
    private String errorTopic;

    @JsonProperty("num_workers")
    @Attributes(description = "The number of workers for the parsing application", minimum = 1, required = true)
    private Integer numWorkers = 1;
    @JsonProperty("input_parallelism")
    @Attributes(description = "The number of parallel executors per worker for reading messages from the input topics",
            required = true, minimum = 1)
    private Integer inputParallelism;

    @JsonProperty("parsing_parallelism")
    @Attributes(description = "The number of parallel executors per worker for parsing messages",
            required = true, minimum = 1)
    private Integer parsingParallelism;

    @JsonProperty("output_parallelism")
    @Attributes(description = "The number of parallel executors per worker for publishing to kafka",
            required = true, minimum = 1)
    private Integer outputParallelism;

    @JsonProperty("parse_metadata")
    @Attributes(description = "Parsing json metadata from input key records")
    private Boolean parseMetadata = false;

    @JsonProperty("max_num_fields")
    @Attributes(description = "Maximum number of fields after parsing the message")
    private Integer maxNumFields = 300;
    @JsonProperty("max_field_size")
    @Attributes(description = "Maximum field size after parsing the message in bytes")
    private Integer maxFieldSize = 40000;

    @JsonProperty("original_string_topic")
    @Attributes(description = "Kafka topic for messages with truncated original_string field. " +
            "The raw log for a message with truncated original_string will be sent to this topic")
    private String originalStringTopic;

    @JsonProperty("metadata_prefix")
    @Attributes(description = "Prefix used in metadata fields when parsing metadata is enabled")
    private String metadataPrefix = "metadata_";

    public ParsingApplicationTypeDto getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(ParsingApplicationTypeDto applicationType) {
        this.applicationType = applicationType;
    }

    public List<String> getInputTopics() {
        return inputTopics;
    }

    public void setInputTopics(List<String> inputTopics) {
        this.inputTopics = inputTopics;
    }

    public String getErrorTopic() {
        return errorTopic;
    }

    public void setErrorTopic(String errorTopic) {
        this.errorTopic = errorTopic;
    }

    public Integer getInputParallelism() {
        return inputParallelism;
    }

    public void setInputParallelism(Integer inputParallelism) {
        this.inputParallelism = inputParallelism;
    }

    public Integer getParsingParallelism() {
        return parsingParallelism;
    }

    public void setParsingParallelism(Integer parsingParallelism) {
        this.parsingParallelism = parsingParallelism;
    }

    public Integer getOutputParallelism() {
        return outputParallelism;
    }

    public void setOutputParallelism(Integer outputParallelism) {
        this.outputParallelism = outputParallelism;
    }

    public String getMetadataPrefix() {
        return metadataPrefix;
    }

    public void setMetadataPrefix(String metadataPrefix) {
        this.metadataPrefix = metadataPrefix;
    }

    public Boolean getParseMetadata() {
        return parseMetadata;
    }

    public void setParseMetadata(Boolean parseMetadata) {
        this.parseMetadata = parseMetadata;
    }

    public Integer getMaxNumFields() {
        return maxNumFields;
    }

    public void setMaxNumFields(Integer maxNumFields) {
        this.maxNumFields = maxNumFields;
    }

    public Integer getMaxFieldSize() {
        return maxFieldSize;
    }

    public void setMaxFieldSize(Integer maxFieldSize) {
        this.maxFieldSize = maxFieldSize;
    }

    public String getOriginalStringTopic() {
        return originalStringTopic;
    }

    public void setOriginalStringTopic(String originalStringTopic) {
        this.originalStringTopic = originalStringTopic;
    }

    public Integer getNumWorkers() {
        return numWorkers;
    }

    public void setNumWorkers(Integer numWorkers) {
        this.numWorkers = numWorkers;
    }
}
