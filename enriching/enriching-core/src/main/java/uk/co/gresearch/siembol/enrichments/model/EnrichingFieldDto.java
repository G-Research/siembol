package uk.co.gresearch.siembol.enrichments.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

/**
 * A data transfer object for representing enriching field used in a table mapping
 *
 * <p>This class is used for json (de)serialisation of an enriching field and
 * for generating json schema from this class using annotations.
 *
 * @author  Marian Novotny
 * @see com.github.reinert.jjschema.Attributes
 * @see com.fasterxml.jackson.annotation.JsonProperty
 *
 */
@Attributes(title = "enriching field", description = "Mapping definition for enriching field")
public class EnrichingFieldDto {
    @JsonProperty("table_field_name")
    @Attributes(required = true, description = "The name of the field in the table")
    private String tableFieldName;
    @JsonProperty("event_field_name")
    @Attributes(required = true, description = "The name of the field added into the event after enriching")
    private String eventFieldName;

    public String getTableFieldName() {
        return tableFieldName;
    }

    public void setTableFieldName(String tableFieldName) {
        this.tableFieldName = tableFieldName;
    }

    public String getEventFieldName() {
        return eventFieldName;
    }

    public void setEventFieldName(String eventFieldName) {
        this.eventFieldName = eventFieldName;
    }
}
