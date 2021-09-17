package uk.co.gresearch.siembol.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class EnrichmentTablesUpdateDto {
    @JsonProperty("enrichment_tables")
    private List<EnrichmentTableDto> enrichmentTables = new ArrayList<>();

    public List<EnrichmentTableDto> getEnrichmentTables() {
        return enrichmentTables;
    }

    public void setEnrichmentTables(List<EnrichmentTableDto> enrichmentTables) {
        this.enrichmentTables = enrichmentTables;
    }
}
