package uk.co.gresearch.nortem.parsers.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

import java.util.List;

@Attributes(title = "reg select", description = "Specification for selector based on regular expressions")
public class RegexSelectDto {
    @JsonProperty("output_field")
    @Attributes(required = true, description = "Output field for selected value")
    private String outputField;
    @JsonProperty("patterns")
    @Attributes(required = true, description = "Search patterns for selecting value", minItems = 1)
    private List<SearchPatternDto> patterns;
    @JsonProperty("default_value")
    @Attributes(description = "Default value when no pattern matches")
    private String defaultValue;

    public String getOutputField() {
        return outputField;
    }

    public void setOutputField(String outputField) {
        this.outputField = outputField;
    }

    public List<SearchPatternDto> getPatterns() {
        return patterns;
    }

    public void setPatterns(List<SearchPatternDto> patterns) {
        this.patterns = patterns;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}
