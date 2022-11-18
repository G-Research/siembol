package uk.co.gresearch.siembol.parsers.netflow;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.invoke.MethodHandles;
import java.util.Objects;
/**
 * An object for representing a netflow field
 *
 * <p>This class represents netflow field used by a netflow parser.
 *
 * @author Marian Novotny
 *
 */
public class NetflowField {
    private static final String UNKNOWN_VALUE = "unknown";
    private static final Logger LOG = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());

    private final int type;
    private final int length;

    public NetflowField(int type, int len) {
        this.type = type;
        this.length = len;
    }

    public String getName() {
        return NetflowFieldType.of(type).getFieldName();
    }

    public Object getValue(BinaryBuffer buffer) {
        NetflowDataType dataType = NetflowFieldType.of(type).getDataType();
        try {
            return dataType.read(buffer, length);
        } catch (Exception e) {
            LOG.error(String.format("Exception during parsing field %s type: %s, len: %d, exception: %s, buffer: %s",
                    ExceptionUtils.getStackTrace(e),
                    getName(),
                    type,
                    length,
                    buffer.getBase64String()));

            //NOTE: we skip the field length in buffer
            buffer.skip(length);
            return UNKNOWN_VALUE;
        }
    }

    public int getLength(){
        return length;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NetflowField)) {
            return false;
        }
        return this.length == ((NetflowField)o).length
                && this.type == ((NetflowField)o).type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, length);
    }
}
