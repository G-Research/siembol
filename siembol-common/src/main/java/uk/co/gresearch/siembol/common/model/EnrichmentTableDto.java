package uk.co.gresearch.siembol.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
/**
 * A data transfer object for representing an enrichment table update message
 *
 * <p>This class is used for json (de)serialisation of an enrichment table update message.
 * It is used in enrichment topology and synchronisation service.
 *
 * @author  Marian Novotny
 * @see JsonProperty
 */
public class EnrichmentTableDto {
    @JsonProperty("name")
    private String name;
    @JsonProperty("path")
    private String path;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
