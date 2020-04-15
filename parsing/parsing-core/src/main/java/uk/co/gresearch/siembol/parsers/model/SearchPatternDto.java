package uk.co.gresearch.siembol.parsers.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

@Attributes(title = "search pattern", description = "Regular expression select search pattern")
public class SearchPatternDto {
    @JsonProperty("pattern")
    @Attributes(required = true, description = "Regular expression pattern")
    private String pattern;
    @JsonProperty("name")
    @Attributes(description = "The value that will be added to the output_field after pattern matching")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
}
