package uk.co.gresearch.nortem.nikita.storm;

public enum TupleFieldNames {
    EVENT("event"),
    NIKITA_MATCHES("nikita_matches"),
    NIKITA_EXCEPTIONS("nikita_exceptions"),
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
