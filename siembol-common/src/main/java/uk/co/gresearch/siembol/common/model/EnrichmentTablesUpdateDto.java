package uk.co.gresearch.siembol.common.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
/**
 * A data transfer object for representing an enrichment tables update message
 *
 * <p>This class is used for json (de)serialisation of an enrichment table update message.
 * It is used in enrichment topology and synchronisation service.
 *
 * @author  Marian Novotny
 * @see JsonProperty
 * @see EnrichmentTableDto
 */
public class EnrichmentTablesUpdateDto {
    @JsonAlias("hdfs_tables")
    @JsonProperty("enrichment_tables")
    private List<EnrichmentTableDto> enrichmentTables = new ArrayList<>();

    public List<EnrichmentTableDto> getEnrichmentTables() {
        return enrichmentTables;
    }

    public void setEnrichmentTables(List<EnrichmentTableDto> enrichmentTables) {
        this.enrichmentTables = enrichmentTables;
    }
}
