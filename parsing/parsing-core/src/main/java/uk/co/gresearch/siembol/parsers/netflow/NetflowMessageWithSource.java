package uk.co.gresearch.siembol.parsers.netflow;

public class NetflowMessageWithSource implements NetflowTransportMessage<String> {
    private static final String ORIGINAL_STRING = "NULL"; //NOTE: space optimisation
    private final String sourceIdentifier;
    private final BinaryBuffer buffer;

    public NetflowMessageWithSource(String metadata, byte[] data) {
        if (metadata == null || metadata.isEmpty()) {
            throw new IllegalArgumentException("Missing Source identifier");
        }

        buffer = new BinaryBuffer(data);
        sourceIdentifier = metadata;
    }

    @Override
    public String getGlobalTemplateId(NetflowHeader header, int templateId) {
        return String.format("%s|%d|%d",
                sourceIdentifier,
                header.getSourceId(),
                templateId);
    }

    @Override
    public BinaryBuffer getNetflowPayload() {
        return buffer;
    }

    @Override
    public String getGlobalSource() {
        return sourceIdentifier;
    }

    @Override
    public String getOriginalString() {
        return ORIGINAL_STRING;
    }
}
