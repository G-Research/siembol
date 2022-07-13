package uk.co.gresearch.siembol.common.model.testing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.SchemaIgnore;
import uk.co.gresearch.siembol.common.jsonschema.JsonRawStringDto;

@Attributes(title = "alerts test specification", description = "Specification for testing alerting rules")
public class AlertingTestSpecificationDto {
    @Attributes(required = true, description = "Event for alerts evaluation")
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
