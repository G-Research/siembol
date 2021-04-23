package uk.co.gresearch.siembol.parsers.netflow;

public interface NetflowTransportMessage<T> {

    /**
     * Get an unique global Id that identifies the template in the global store
     * @param header a netflow header
     * @param templateId id of the template
     * @return Object of the type T that  will be used as a key in a templates store
     */

    public T getGlobalTemplateId(NetflowHeader header, int templateId);

    /**
     * Get a BinaryBuffer with offset pointing to start of the netflow payload
     *
     * @return BinaryBuffer with Netflow payload
     */

    public BinaryBuffer getNetflowPayload();

    /**
     * Get a global identifier of the device which sent the netflow message
     *
     * @return returns String that identifies the device on the network that produces the netflow packet
     */

    public String getGlobalSource();
    /**
     * Get original string used in parsed message.
     *
     * @return returns String that should be used for original string.
     */
    public String getOriginalString();
}
