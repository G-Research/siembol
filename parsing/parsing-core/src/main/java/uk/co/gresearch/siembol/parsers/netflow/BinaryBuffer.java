package uk.co.gresearch.siembol.parsers.netflow;

import java.nio.ByteBuffer;
import java.util.Base64;
/**
 * An object for representing a binary buffer
 *
 * <p>This class represents binary buffer for parsing netflow messages.
 * It is wrapping ByteBuffer to provide support for parsing unsigned fields of structures.
 *
 * @author Marian Novotny
 *
 */
public class BinaryBuffer {
    private static final int SIZE_OF_UBYTE = 1;
    private static final int SIZE_OF_USHORT = 2;
    private static final int SIZE_OF_UINT = 4;

    private final ByteBuffer buffer;

    public BinaryBuffer(byte[] bytes) {
        buffer = ByteBuffer.wrap(bytes);
    }

    public long readUInt() {
        return Integer.toUnsignedLong(buffer.getInt());
    }

    public int readInt() {
        return buffer.getInt();
    }

    public int readUShort() {
        return Short.toUnsignedInt(buffer.getShort());
    }

    public short readUByte() {
        return (short)Byte.toUnsignedInt(buffer.get());
    }

    public void skip(int numberOfByes) {
        buffer.position(buffer.position() + numberOfByes);
    }

    public void setPosition(int newPosition) {
        buffer.position(newPosition);
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public boolean hasRemaining(int size) {
        return buffer.remaining() >= size;
    }

    public long readUnsigned(int size) {
        switch (size) {
            case SIZE_OF_UBYTE:
                return readUByte();
            case SIZE_OF_USHORT:
                return readUShort();
            case SIZE_OF_UINT:
                return readUInt();
        }
        return -1;
    }

    public byte[] readByteArray(int size) {
        byte[] ret = new byte[size];
        buffer.get(ret);
        return ret;
    }

    public boolean hasRemaining() {
        return buffer.hasRemaining();
    }

    public String getBase64String() {
        return Base64.getEncoder().encodeToString(buffer.array());
    }
}
