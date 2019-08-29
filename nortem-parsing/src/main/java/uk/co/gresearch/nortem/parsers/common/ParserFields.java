package uk.co.gresearch.nortem.parsers.common;

public enum ParserFields {
    SRC_ADDR("ip_src_addr"),
    SRC_PORT("ip_src_port"),
    DST_ADDR("ip_dst_addr"),
    DST_PORT("ip_dst_port"),
    PROTOCOL("protocol"),
    TIMESTAMP("timestamp"),
    ORIGINAL("original_string");

    private final String name;
    ParserFields(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }
}
