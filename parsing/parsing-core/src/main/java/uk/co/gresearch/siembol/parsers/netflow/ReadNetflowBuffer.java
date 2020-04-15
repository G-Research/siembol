package uk.co.gresearch.siembol.parsers.netflow;

@FunctionalInterface
public interface ReadNetflowBuffer {
    Object apply(BinaryBuffer buffer, int fieldLength);
};
