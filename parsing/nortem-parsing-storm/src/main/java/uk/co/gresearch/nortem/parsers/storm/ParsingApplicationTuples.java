package uk.co.gresearch.nortem.parsers.storm;

public enum ParsingApplicationTuples {
    METADATA("metadata"),
    LOG("log"),
    PARSING_RESULTS("parsing_results");

    private final String name;
    ParsingApplicationTuples(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
