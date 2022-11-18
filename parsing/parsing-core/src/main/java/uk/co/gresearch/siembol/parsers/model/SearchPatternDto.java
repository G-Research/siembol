package uk.co.gresearch.siembol.parsers.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;
/**
 * A data transfer object for representing attributes of a search pattern
 *
 * <p>This class is used for json (de)serialisation of a search pattern used in a pattern extractor and
 * for generating json schema from this class using annotations.
 *
 * @author  Marian Novotny
 * @see com.github.reinert.jjschema.Attributes
 * @see com.fasterxml.jackson.annotation.JsonProperty
 */
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
