package uk.co.gresearch.siembol.alerts.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

@Attributes(title = "tag", description = "The tag pair added to the event after matching")
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

    public static TagDto from(String tagName, String tagValue) {
        TagDto ret = new TagDto();
        ret.setTagName(tagName);
        ret.setTagValue(tagValue);
        return ret;
    }

}

