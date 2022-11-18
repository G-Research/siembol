package uk.co.gresearch.siembol.parsers.netflow;

import java.util.HashMap;
import java.util.Map;

import static uk.co.gresearch.siembol.parsers.netflow.NetflowDataType.*;
/**
 * An enum of netflow v9 field types
 *
 * <p>This enum represents a netflow v9 field types.
 * It provides a name, a type code and a data type used for parsing a field.
 *
 * @author Marian Novotny
 * @see NetflowBufferReader
 *
 */
public enum NetflowFieldType {
    CUSTOM(0, OCTET_ARRAY),
    IN_BYTES(1, INTEGER),
    IN_PKTS(2, INTEGER),
    FLOWS(3, INTEGER),
    PROTOCOL(4, INTEGER),
    SRC_TOS(5, INTEGER),
    TCP_FLAGS(6, INTEGER),
    L4_SRC_PORT(7, INTEGER, "ip_src_port"),
    IPV4_SRC_ADDR(8, IPV4_ADDRESS, "ip_src_addr"),
    SRC_MASK(9, INTEGER),
    INPUT_SNMP(10, INTEGER),
    L4_DST_PORT(11, INTEGER, "ip_dst_port"),
    IPV4_DST_ADDR(12, IPV4_ADDRESS, "ip_dst_addr"),
    DST_MASK(13, INTEGER),
    OUTPUT_SNMP(14, INTEGER),
    IPV4_NEXT_HOP(15, IPV4_ADDRESS),
    SRC_AS(16, INTEGER),
    DST_AS(17, INTEGER),
    BGP_IPV4_NEXT_HOP(18, IPV4_ADDRESS),
    MUL_DST_PKTS(19, INTEGER),
    MUL_DST_BYTES(20, INTEGER),
    LAST_SWTICHED(21, INTEGER),
    FIRST_SWITCHED(22, INTEGER),
    OUT_BYTES(23, INTEGER),
    OUT_PKTS(24, INTEGER),
    MIN_PKT_LNGTH(25, INTEGER),
    MAX_PKT_LNGTH(26, INTEGER),
    IPV6_SRC_ADDR(27, IPV6_ADDRESS, "ip_src_addr"),
    IPV6_DST_ADDR(28, IPV6_ADDRESS, "ip_dst_addr"),
    IPV6_SRC_MASK(29, INTEGER),
    IPV6_DST_MASK(30, INTEGER),
    IPV6_FLOW_LABEL(31, INTEGER),
    ICMP_TYPE(32, INTEGER),
    MUL_IGMP_TYPE(33, INTEGER),
    SAMPLING_INTERVAL(34, INTEGER),
    SAMPLING_ALGORITHM(35, INTEGER),
    FLOW_ACTIVE_TIMEOUT(36, INTEGER),
    FLOW_INACTIVE_TIMEOUT(37, INTEGER),
    ENGINE_TYPE(38, INTEGER),
    ENGINE_ID(39, INTEGER),
    TOTAL_BYTES_EXP(40, INTEGER),
    TOTAL_PKTS_EXP(41, INTEGER),
    TOTAL_FLOWS_EXP(42, INTEGER),
    IPV4_SRC_PREFIX(44, IPV4_ADDRESS),
    IPV4_DST_PREFIX(45, IPV4_ADDRESS),
    MPLS_TOP_LABEL_TYPE(46, INTEGER),
    MPLS_TOP_LABEL_IP_ADDR(47, IPV4_ADDRESS),
    FLOW_SAMPLER_ID(48, INTEGER),
    FLOW_SAMPLER_MODE(49, INTEGER),
    FLOW_SAMPLER_RANDOM_INTERVAL(50, INTEGER),
    MIN_TTL(52, INTEGER),
    MAX_TTL(53, INTEGER),
    IPV4_IDENT(54, INTEGER),
    DST_TOS(55, INTEGER),
    IN_SRC_MAC(56, MAC_ADDRESS),
    OUT_DST_MAC(57, MAC_ADDRESS),
    SRC_VLAN(58, INTEGER),
    DST_VLAN(59, INTEGER),
    IP_PROTOCOL_VERSION(60, INTEGER),
    DIRECTION(61, INTEGER),
    IPV6_NEXT_HOP(62, IPV6_ADDRESS),
    BGP_IPV6_NEXT_HOP(63, IPV6_ADDRESS),
    IPV6_OPTION_HEADERS(64, INTEGER),
    MPLS_LABEL_1(70, OCTET_ARRAY),
    MPLS_LABEL_2(71, OCTET_ARRAY),
    MPLS_LABEL_3(72, OCTET_ARRAY),
    MPLS_LABEL_4(73, OCTET_ARRAY),
    MPLS_LABEL_5(74, OCTET_ARRAY),
    MPLS_LABEL_6(75, OCTET_ARRAY),
    MPLS_LABEL_7(76, OCTET_ARRAY),
    MPLS_LABEL_8(77, OCTET_ARRAY),
    MPLS_LABEL_9(78, OCTET_ARRAY),
    MPLS_LABEL_10(79, OCTET_ARRAY),
    IN_DST_MAC(80, MAC_ADDRESS),
    OUT_SRC_MAC(81, MAC_ADDRESS),
    IF_NAME(82, STRING),
    IF_DESC(83, STRING),
    SAMPLER_NAME(84, STRING),
    IN_PERMANENT_BYTES(85, INTEGER),
    IN_PERMANENT_PKTS(86, INTEGER),
    FRAGMENT_OFFSET(88, INTEGER),
    FORWARDING_STATUS(89, INTEGER),
    MPLS_PAL_RD(90, OCTET_ARRAY),
    MPLS_PREFIX_LEN(91, INTEGER),
    SRC_TRAFFIC_INDEX(92, INTEGER),
    DST_TRAFFIC_INDEX(93, INTEGER),
    APPLICATION_DESCRIPTION(94, STRING),
    APPLICATION_TAG(95, OCTET_ARRAY),
    APPLICATION_NAME(96, STRING),
    postipDiffServCodePoint(98, INTEGER, "post_ip_diff_serv_code_point"),
    replicationFactor(99, INTEGER, "replication_factor"),
    layer2packetSectionOffset(102, INTEGER, "layer2_packet_section_offset"),
    layer2packetSectionSize(103, INTEGER, "layer2_packet_section_size"),
    layer2packetSectionData(104, OCTET_ARRAY, "layer2_packet_section_data");

    private static final Map<Integer, NetflowFieldType> typeMapping = new HashMap<>();
    static {
        for (NetflowFieldType type : NetflowFieldType.values()) {
            typeMapping.put(type.typeCode, type);
        }
    }
    public static NetflowFieldType of(int typeCode) {
        return typeMapping.getOrDefault(typeCode, CUSTOM);
    }

    private final int typeCode;
    private final NetflowDataType dataType;
    private final String fieldName;

    NetflowFieldType(int typeCode, NetflowDataType dataType) {
        this(typeCode, dataType, null);
    }

    NetflowFieldType(int typeCode, NetflowDataType dataType, String fieldName) {
        this.typeCode = typeCode;
        this.dataType = dataType;
        this.fieldName = fieldName != null
                ? fieldName
                : this.name().toLowerCase();
    }

    public int getTypeCode() {
        return typeCode;
    }

    public NetflowDataType getDataType() {
        return dataType;
    }

    public String getFieldName() {
        return fieldName;
    }

}
