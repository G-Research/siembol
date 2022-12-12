package uk.co.gresearch.siembol.enrichments.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;
/**
 * A data transfer object for representing a tag used in an enrichment rule
 *
 * <p>This class is used for json (de)serialisation of a tag used in an enrichment rule and
 * for generating json schema from this class using annotations.
 *
 * @author  Marian Novotny
 * @see com.github.reinert.jjschema.Attributes
 * @see com.fasterxml.jackson.annotation.JsonProperty
 *
 */
@Attributes(title = "tag", description = "The tags added to the event after joining the enrichment table")
public class TagDto {
    @JsonProperty("tag_name")
    @Attributes(required = true, description = "The name of the tag")
    private String tagName;
    @JsonProperty("tag_value")
    @Attributes(required = true, description = "The value of the tag")
    private String tagValue;

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getTagValue() {
        return tagValue;
    }

    public void setTagValue(String tagValue) {
        this.tagValue = tagValue;
    }
}