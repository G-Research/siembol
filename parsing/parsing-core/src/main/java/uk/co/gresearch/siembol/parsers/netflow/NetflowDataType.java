package uk.co.gresearch.siembol.parsers.netflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.invoke.MethodHandles;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.util.Arrays;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
/**
 * An enum of netflow v9 data types
 *
 * <p>This enum represent a netflow v9 data types.
 * A data type includes a function for reading a binary buffer.
 *
 * @author Marian Novotny
 * @see NetflowBufferReader
 *
 */
public enum NetflowDataType implements NetflowBufferReader {
    INTEGER(NetflowDataType::readInteger),
    FLOAT(NetflowDataType::readFloat),
    IPV4_ADDRESS(NetflowDataType::readIpv4Address),
    IPV6_ADDRESS(NetflowDataType::readIpv6Address),
    MAC_ADDRESS(NetflowDataType::readMACAddress),
    BOOLEAN(NetflowDataType::readBoolean),
    STRING(NetflowDataType::readString),
    OCTET_ARRAY(NetflowDataType::readString);

    public static final int SIZE_OF_MAC_ADDRESS = 6;
    public static final int SIZE_OF_BOOLEAN = 1;
    public static final int SIZE_OF_IPV4 = 4;
    public static final int SIZE_OF_IPV6 = 16;
    public static final int SIZE_OF_LONG = 8;
    public static final int SIZE_OF_DOUBLE = 8;
    public static final List<Integer> INTEGER_SIZE_LIST
            = Arrays.asList(1, 2, 4, 8);
    public static final List<Integer> FLOAT_SIZE_LIST
            = Arrays.asList(4, 8);
    public static final String UNKNOWN_IPV4 = "Unknown IPv4";
    public static final String UNKNOWN_IPV6 = "Unknown IPv6";
    private static final Logger LOG = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());

    private static Object readInteger(BinaryBuffer buffer, int fieldLength) {
        if (!INTEGER_SIZE_LIST.contains(fieldLength)) {
            throw new IllegalStateException(String.format(
                    "Wrong integer size: %d", fieldLength));
        }

        return fieldLength == SIZE_OF_LONG
                ? buffer.getBuffer().getLong()
                : buffer.readUnsigned(fieldLength);
    }

    private static Object readFloat(BinaryBuffer buffer, int fieldLength) {
        if (!(FLOAT_SIZE_LIST.contains(fieldLength))) {
            throw new IllegalStateException(String.format(
                    "Wrong float size: %d", fieldLength));
        }
        return fieldLength == SIZE_OF_DOUBLE
                ? buffer.getBuffer().getDouble()
                : buffer.getBuffer().getFloat();
    }

    private static Object readMACAddress(BinaryBuffer buffer, int fieldLength) {
        if (fieldLength != SIZE_OF_MAC_ADDRESS) {
            throw new IllegalStateException(String.format(
                    "Wrong MAC address size: %d", fieldLength));
        }

        StringBuilder sb = new StringBuilder(SIZE_OF_MAC_ADDRESS * 3 - 1);
        for (int i = 0; i < fieldLength; i++) {
            if (i > 0) {
                sb.append(':');
            }
            sb.append(String.format("%02x", buffer.getBuffer().get()));
        }
        return sb.toString();
    }

    private static Object readBoolean(BinaryBuffer buffer, int fieldLength) {
        if (fieldLength != SIZE_OF_BOOLEAN) {
            throw new IllegalStateException(String.format(
                    "Wrong boolean size: %d", fieldLength));
        }
        return buffer.readUByte() != 0;
    }

    private static Object readIpv4Address(BinaryBuffer buffer, int fieldLength) {
        if (fieldLength != SIZE_OF_IPV4) {
            throw new IllegalStateException(String.format(
                    "Wrong IPv4 address size: %d", fieldLength));
        }

        byte[] dst = buffer.readByteArray(fieldLength);
        try {
            return Inet4Address.getByAddress(dst).getHostAddress();
        } catch (Exception e) {
            LOG.error(String.format("Invalid Ipv4 address: %s",
                    new String(dst, UTF_8)));
            return UNKNOWN_IPV4;
        }
    }

    private static Object readIpv6Address(BinaryBuffer buffer, int fieldLength) {
        if (fieldLength != SIZE_OF_IPV6) {
            throw new IllegalStateException(String.format(
                    "Wrong IPv6 address size: %d", fieldLength));
        }

        byte[] dst = buffer.readByteArray(fieldLength);
        try {
            return Inet6Address.getByAddress(dst).getHostAddress();
        } catch (Exception e) {
            LOG.error(String.format("Invalid Ipv6 address: %s",
                    new String(dst, UTF_8)));
            return UNKNOWN_IPV6;
        }
    }

    private static Object readString(BinaryBuffer buffer, int fieldLength) {
        byte[] dst = buffer.readByteArray(fieldLength);
        return new String(dst, UTF_8);
    }

    private final NetflowBufferReader readBuffer;

    NetflowDataType(NetflowBufferReader readBuffer) {
        this.readBuffer = readBuffer;
    }

    @Override
    public Object read(BinaryBuffer buffer, int fieldLength) {
        return readBuffer.read(buffer, fieldLength);
    }
}
