package uk.co.gresearch.siembol.alerts.storm;
/**
 * An enum of tuple field names used in alerting storm topology
 *
 * @author  Marian Novotny
 *
 * @see #EVENT
 * @see #ALERTING_MATCHES
 * @see #CORRELATION_KEY
 * @see #ALERTING_EXCEPTIONS
 * @see #CORRELATION_KEY
 *
 */
public enum TupleFieldNames {
    EVENT("event"),
    ALERTING_MATCHES("matches"),
    ALERTING_EXCEPTIONS("exceptions"),
    CORRELATION_KEY("correlation_key");

    private final String name;
    TupleFieldNames(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
