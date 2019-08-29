package uk.co.gresearch.nortem.parsers.netflow;

@FunctionalInterface
public interface ReadNetflowBuffer {
    Object apply(BinaryBuffer buffer, int fieldLength);
};
