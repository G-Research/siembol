package uk.co.gresearch.siembol.enrichments.storm.common;

public enum EnrichmentTuples {
    EVENT("event"),
    COMMANDS("commands"),
    ENRICHMENTS("enrichments"),
    EXCEPTIONS("exceptions"),
    KAFKA_MESSAGES("messages"),
    COUNTERS("counters");

    private final String name;
    EnrichmentTuples(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
