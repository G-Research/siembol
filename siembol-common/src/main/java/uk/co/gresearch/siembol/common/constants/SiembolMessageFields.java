package uk.co.gresearch.siembol.common.constants;

public enum SiembolMessageFields {
    SRC_ADDR("ip_src_addr"),
    SRC_PORT("ip_src_port"),
    DST_ADDR("ip_dst_addr"),
    DST_PORT("ip_dst_port"),
    PROTOCOL("protocol"),
    TIMESTAMP("timestamp"),
    ORIGINAL("original_string"),
    GUID("guid"),
    SENSOR_TYPE("source_type"),
    PARSING_TIME("siembol_parsing_ts"),
    ENRICHING_TIME("siembol_enriching_ts");

    private final String name;
    SiembolMessageFields(String name) {
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
