package uk.co.gresearch.siembol.enrichments.storm.common;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class TablesUpdateDto {
    @JsonProperty("enrichment_tables")
    private List<EnrichmentTableDto> enrichmentTables;

    public List<EnrichmentTableDto> getEnrichmentTables() {
        return enrichmentTables;
    }

    public void setEnrichmentTables(List<EnrichmentTableDto> enrichmentTables) {
        this.enrichmentTables = enrichmentTables;
    }
}
