package uk.co.gresearch.nortem.configeditor.service.centrifuge.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.SchemaIgnore;
import uk.co.gresearch.nortem.common.jsonschema.JsonRawStringDto;

@Attributes(title = "centrifuge test specification", description = "Specification for testing centrifuge rules")
public class CentrifugeTestSpecificationDto {
    @Attributes(required = true, description = "Event for testing centrifuge.")
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

