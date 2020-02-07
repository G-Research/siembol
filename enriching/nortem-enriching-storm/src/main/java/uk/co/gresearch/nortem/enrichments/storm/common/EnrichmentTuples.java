package uk.co.gresearch.nortem.enrichments.storm.common;

public enum EnrichmentTuples {
    EVENT("event"),
    COMMANDS("commands"),
    ENRICHMENTS("enrichments"),
    EXCEPTIONS("exceptions"),
    KAFKA_MESSAGES("messages");

    private final String name;
    EnrichmentTuples(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
