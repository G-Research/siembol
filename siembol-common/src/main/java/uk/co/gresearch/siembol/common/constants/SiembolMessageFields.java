package uk.co.gresearch.siembol.common.constants;

public enum SiembolMessageFields {
    SRC_ADDR("ip_src_addr"),
    SRC_PORT("ip_src_port"),
    DST_ADDR("ip_dst_addr"),
    DST_PORT("ip_dst_port"),
    PROTOCOL("protocol"),
    TIMESTAMP(SiembolConstants.TIMESTAMP),
    ORIGINAL("original_string"),
    GUID("guid"),
    SENSOR_TYPE("source_type"),
    PARSING_TIME(SiembolConstants.PARSING_TIME),
    ENRICHING_TIME(SiembolConstants.ENRICHING_TIME),
    RESPONSE_TIME(SiembolConstants.RESPONSE_TIME);

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
