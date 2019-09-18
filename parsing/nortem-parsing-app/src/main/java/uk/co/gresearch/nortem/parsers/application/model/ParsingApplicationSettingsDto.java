package uk.co.gresearch.nortem.parsers.application.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

import java.util.List;

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

    @JsonProperty("input_parallelism")
    @Attributes(description = "The number of parallel executors for reading messages from the input kafka topics",
            required = true, minimum = 1)
    private Integer inputParallelism;

    @JsonProperty("parsing_parallelism")
    @Attributes(description = "The number of parallel executors for parsing messages",
            required = true, minimum = 1)
    private Integer parsingParallelism;

    @JsonProperty("output_parallelism")
    @Attributes(description = "The number of parallel executors for publishing to kafka",
            required = true, minimum = 1)
    private Integer outputParallelism;

    @JsonProperty("parse_metadata")
    @Attributes(description = "Parsing metadata from input key records")
    private Boolean parseMetadata = false;

    @JsonProperty("metadata_prefix")
    @Attributes(description = "Prefix used in metadata fields when parsing metadata is enabled")
    private String metadataPrefix = "metadata";

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
}
