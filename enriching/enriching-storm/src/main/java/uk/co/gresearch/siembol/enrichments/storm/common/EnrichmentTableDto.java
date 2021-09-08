package uk.co.gresearch.siembol.enrichments.storm.common;

import com.fasterxml.jackson.annotation.JsonProperty;

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
