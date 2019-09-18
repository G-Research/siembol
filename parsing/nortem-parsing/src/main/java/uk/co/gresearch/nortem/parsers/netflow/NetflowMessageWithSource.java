package uk.co.gresearch.nortem.parsers.netflow;

public class NetflowMessageWithSource implements NetflowTransportMessage<String> {
    private static final char IDENTIFIER_DELIMITER = '|';
    private static final String ORIGINAL_STRING = "NULL"; //NOTE: space optimisation
    private final String sourceIdentifier;
    private final BinaryBuffer buffer;

    public NetflowMessageWithSource(byte[] data) {
        buffer = new BinaryBuffer(data);
        sourceIdentifier = getSourceIdentifier(buffer);
    }

    private String getSourceIdentifier(BinaryBuffer buffer) {
        StringBuilder sb = new StringBuilder();
        boolean found = false;
        while (buffer.hasRemaining())
        {
            byte current = buffer.getBuffer().get();
            if (current == IDENTIFIER_DELIMITER) {
                found = true;
                break;
            }

            sb.append((char)current);
        }
        if (!found || sb.length() == 0) {
            throw new IllegalArgumentException("Missing Source identifier");
        }

        return sb.toString();
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
