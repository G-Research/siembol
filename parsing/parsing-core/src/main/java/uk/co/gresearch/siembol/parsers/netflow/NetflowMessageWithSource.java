package uk.co.gresearch.siembol.parsers.netflow;
/**
 * An object for providing a netflow transport message
 *
 * <p>This class is implementing NetflowTransportMessage interface.
 * It uses a string identification of a device such as its IP address that must be unique per a device.
 *
 * @author Marian Novotny
 * @see NetflowTransportMessage
 *
 *
 */
public class NetflowMessageWithSource implements NetflowTransportMessage<String> {
    private static final String ORIGINAL_STRING = "NULL"; //NOTE: space optimisation
    private final String sourceIdentifier;
    private final BinaryBuffer buffer;

    /**
     * Creates NetflowMessageWithSource instance
     *
     * @param metadata a unique identifier of the device that generated the message
     * @param data a binary data of the netflow message
     */
    public NetflowMessageWithSource(String metadata, byte[] data) {
        if (metadata == null || metadata.isEmpty()) {
            throw new IllegalArgumentException("Missing Source identifier");
        }

        buffer = new BinaryBuffer(data);
        sourceIdentifier = metadata;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getGlobalTemplateId(NetflowHeader header, int templateId) {
        return String.format("%s|%d|%d",
                sourceIdentifier,
                header.getSourceId(),
                templateId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BinaryBuffer getNetflowPayload() {
        return buffer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getGlobalSource() {
        return sourceIdentifier;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getOriginalString() {
        return ORIGINAL_STRING;
    }
}
