package uk.co.gresearch.siembol.parsers.storm;
/**
 * An enum of tuple field names used in a parsing application storm topology
 *
 * @author  Marian Novotny
 *
 * @see #SOURCE
 * @see #METADATA
 * @see #LOG
 * @see #PARSING_MESSAGES
 * @see #COUNTERS
 *
 */
public enum ParsingApplicationTuples {
    SOURCE("source"),
    METADATA("metadata"),
    LOG("log"),
    PARSING_MESSAGES("messages"),
    COUNTERS("counters");

    private final String name;
    ParsingApplicationTuples(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
