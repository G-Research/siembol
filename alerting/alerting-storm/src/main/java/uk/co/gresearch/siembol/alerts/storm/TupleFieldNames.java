package uk.co.gresearch.siembol.alerts.storm;

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
