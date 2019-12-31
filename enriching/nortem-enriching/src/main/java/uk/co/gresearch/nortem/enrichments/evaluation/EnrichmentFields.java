package uk.co.gresearch.nortem.enrichments.evaluation;

public enum EnrichmentFields {
    ENRICHMENT_COMMAND("nortem:internal:enrichment_command");

    private final String name;

    EnrichmentFields(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
