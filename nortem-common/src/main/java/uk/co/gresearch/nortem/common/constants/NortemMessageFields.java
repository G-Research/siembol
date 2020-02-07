package uk.co.gresearch.nortem.common.constants;

public enum NortemMessageFields {
    SRC_ADDR("ip_src_addr"),
    SRC_PORT("ip_src_port"),
    DST_ADDR("ip_dst_addr"),
    DST_PORT("ip_dst_port"),
    PROTOCOL("protocol"),
    TIMESTAMP("timestamp"),
    ORIGINAL("original_string"),
    GUID("guid"),
    SENSOR_TYPE("source.type"),
    PARSING_TIME("nortem:parsing_time"),
    ENRICHING_TIME("nortem:enriching_time");

    private final String name;
    NortemMessageFields(String name) {
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
