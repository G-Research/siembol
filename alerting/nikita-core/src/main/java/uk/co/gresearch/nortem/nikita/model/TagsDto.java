package uk.co.gresearch.nortem.nikita.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

@Attributes(title = "tags", description = "The tags added to the event after matching")
public class TagsDto {
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

