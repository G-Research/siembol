package uk.co.gresearch.siembol.common.model.testing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.SchemaIgnore;
import uk.co.gresearch.siembol.common.jsonschema.JsonRawStringDto;
/**
 * A data transfer object for representing an enrichment test specification
 *
 * <p>This class is used for json (de)serialisation of an enrichment test specification and
 * for generating json schema from this class using annotations.
 *
 * @author  Marian Novotny
 * @see com.github.reinert.jjschema.Attributes
 * @see com.fasterxml.jackson.annotation.JsonProperty
 * @see JsonRawStringDto
 */
@Attributes(title = "enrichments test specification",
        description = "The specification for testing enrichments")
public class EnrichmentTestingSpecificationDto {
    @Attributes(required = true, description = "Event for testing an enriching rule")
    private JsonRawStringDto event;
    @JsonIgnore
    @SchemaIgnore
    private String eventContent;

    @JsonProperty("testing_table_name")
    @Attributes(required = true, description = "Name of a testing table")
    private String testingTableName;

    @JsonProperty("testing_table_mapping")
    @Attributes(required = true, description = "Mapping for a testing table")
    private JsonRawStringDto testingTableMapping;

    @JsonIgnore
    @SchemaIgnore
    private String testingTableMappingContent;

    @JsonSetter
    public void setTestingTableMapping(JsonNode testingTableMapping) {
        this.testingTableMappingContent = testingTableMapping.toString();
    }

    public JsonRawStringDto getTestingTableMapping() {
        return testingTableMapping;
    }

    @JsonIgnore
    public String getTestingTableMappingContent() {
        return testingTableMappingContent;
    }

    public String getTestingTableName() {
        return testingTableName;
    }

    public void setTestingTableName(String testingTableName) {
        this.testingTableName = testingTableName;
    }

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

}
