package uk.co.gresearch.siembol.common.constants;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
/**
 * An enum for representing Siembol message fields added or used by Siembol components
 *
 * @author  Marian Novotny
 *
 * @see #ORIGINAL
 * @see #GUID
 * @see #SENSOR_TYPE
 * @see #TIMESTAMP
 * @see #PARSING_TIME
 * @see #ENRICHING_TIME
 * @see #RESPONSE_TIME
 */
public enum SiembolMessageFields {
    SRC_ADDR(SiembolConstants.SRC_ADDR),
    SRC_PORT(SiembolConstants.SRC_PORT),
    DST_ADDR(SiembolConstants.DST_ADDR),
    DST_PORT(SiembolConstants.DST_PORT),
    PROTOCOL(SiembolConstants.PROTOCOL),
    ORIGINAL(SiembolConstants.ORIGINAL),
    GUID(SiembolConstants.GUID),
    SENSOR_TYPE(SiembolConstants.SENSOR_TYPE),
    TIMESTAMP(SiembolConstants.TIMESTAMP),
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

    public static Set<String> getMessageFieldsSet() {
        return Arrays.stream(SiembolMessageFields.values()).map(x -> x.getName()).collect(Collectors.toSet());
    }
}
