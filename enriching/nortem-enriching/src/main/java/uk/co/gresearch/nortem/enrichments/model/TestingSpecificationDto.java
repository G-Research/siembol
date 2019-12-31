package uk.co.gresearch.nortem.enrichments.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.SchemaIgnore;
import uk.co.gresearch.nortem.common.jsonschema.JsonRawStringDto;

import java.util.List;

@Attributes(title = "enrichments test specification",
        description = "The specification for testing enrichments")
public class TestingSpecificationDto {
    @Attributes(required = true, description = "Event for testing an enriching rule")
    private JsonRawStringDto event;
    @JsonIgnore
    @SchemaIgnore
    private String eventContent;
    @JsonProperty("testing_tables")
    @Attributes(required = true, description = "Tables for testing an enriching rule", minItems = 1)
    private List<TestingTableDto> testingTables;

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

    public List<TestingTableDto> getTestingTables() {
        return testingTables;
    }

    public void setTestingTables(List<TestingTableDto> testingTables) {
        this.testingTables = testingTables;
    }
}
