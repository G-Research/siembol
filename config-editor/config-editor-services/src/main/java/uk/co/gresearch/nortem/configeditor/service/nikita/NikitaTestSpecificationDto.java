package uk.co.gresearch.nortem.configeditor.service.nikita;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.SchemaIgnore;
import uk.co.gresearch.nortem.common.jsonschema.JsonRawStringDto;

@Attributes(title = "nikita test specification", description = "Specification for testing parser configurations")
public class NikitaTestSpecificationDto {
    @Attributes(required = true, description = "Event for nikita evaluation")
    JsonRawStringDto event;

    @JsonIgnore
    @SchemaIgnore
    private String eventContent;

    public JsonRawStringDto getEvent() {
        return event;
    }

    @JsonSetter
    public void setEvent(JsonNode event) {
        this.eventContent = event.toString();
    }

    @JsonIgnore
    public String getEventContent() {
        return eventContent;
    }
}
