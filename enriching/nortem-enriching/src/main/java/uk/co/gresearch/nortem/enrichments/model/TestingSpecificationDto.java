package uk.co.gresearch.nortem.enrichments.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.SchemaIgnore;
import uk.co.gresearch.nortem.common.jsonschema.JsonRawStringDto;

@Attributes(title = "enrichments test specification",
        description = "The specification for testing enrichments")
public class TestingSpecificationDto {
    @Attributes(required = true, description = "Event for testing an enriching rule")
    private JsonRawStringDto event;
    @JsonIgnore
    @SchemaIgnore
    private String eventContent;
    @JsonProperty("testing_table")
    @Attributes(required = true, description = "Table for testing an enriching rule")
    private TestingTableDto testingTable;

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

    public void setEvent(JsonRawStringDto event) {
        this.event = event;
    }

    public void setEventContent(String eventContent) {
        this.eventContent = eventContent;
    }

    public TestingTableDto getTestingTable() {
        return testingTable;
    }

    public void setTestingTable(TestingTableDto testingTable) {
        this.testingTable = testingTable;
    }
}
