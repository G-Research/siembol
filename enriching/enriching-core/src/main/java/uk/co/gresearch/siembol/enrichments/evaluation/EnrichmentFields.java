package uk.co.gresearch.siembol.enrichments.evaluation;
/**
 * An enum for internal enrichment fields used in enriching rule evaluation
 *
 * <p>This enum is used for internal enrichment fields used in enriching rule evaluation
 *
 * @author  Marian Novotny
 * @see #ENRICHMENT_COMMAND
 *
 */
public enum EnrichmentFields {
    ENRICHMENT_COMMAND("siembol:internal:enrichment_command");

    private final String name;

    EnrichmentFields(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
