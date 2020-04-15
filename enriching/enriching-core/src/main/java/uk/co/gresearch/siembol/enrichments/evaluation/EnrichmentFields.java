package uk.co.gresearch.siembol.enrichments.evaluation;

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
