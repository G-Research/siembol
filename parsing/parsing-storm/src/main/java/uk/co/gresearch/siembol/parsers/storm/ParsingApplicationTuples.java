package uk.co.gresearch.siembol.parsers.storm;

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
