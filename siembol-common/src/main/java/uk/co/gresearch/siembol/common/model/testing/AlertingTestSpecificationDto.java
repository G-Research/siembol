package uk.co.gresearch.siembol.common.model.testing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.SchemaIgnore;
import uk.co.gresearch.siembol.common.model.JsonRawStringDto;
/**
 * A data transfer object for representing an alerting test specification
 *
 * <p>This class is used for json (de)serialisation of an alerting test specification and
 * for generating json schema from this class using annotations.
 *
 * @author  Marian Novotny
 * @see com.github.reinert.jjschema.Attributes
 * @see com.fasterxml.jackson.annotation.JsonProperty
 * @see JsonRawStringDto
 */
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
