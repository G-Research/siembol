package uk.co.gresearch.siembol.enrichments.storm.common;
/**
 * An enum of tuple field names used in an enrichment storm topology
 *
 * @author  Marian Novotny
 *
 * @see #EVENT
 * @see #COMMANDS
 * @see #ENRICHMENTS
 * @see #EXCEPTIONS
 * @see #KAFKA_MESSAGES
 * @see #COUNTERS
 *
 */
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
