package uk.co.gresearch.siembol.parsers.netflow;
/**
 * An interface for reading from a binary buffer
 *
 * <p>This functional interface is used for reading form a binary buffer.
 *
 * @author Marian Novotny
 *
 */
@FunctionalInterface
public interface NetflowBufferReader {
    Object read(BinaryBuffer buffer, int fieldLength);
}
