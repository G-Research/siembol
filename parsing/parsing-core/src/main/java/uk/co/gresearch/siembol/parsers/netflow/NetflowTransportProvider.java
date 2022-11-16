package uk.co.gresearch.siembol.parsers.netflow;
import java.util.List;
import java.util.Optional;
/**
 * An interface for providing netflow templates
 *
 * <p>This interface is used for getting and updating netflow templates which are used by the netflow parser.
 *
 * @author Marian Novotny
 * @see NetflowTransportMessage
 * @see NetflowField
 * @see NetflowHeader
 *
 */
public interface NetflowTransportProvider<T> {

    /**
     * Gets a netflow transport message from a netflow binary payload and metadata
     * @param metadata metadata of the netflow message that should contain source identification
     * @param data byte array that contains netflow payload
     * @return NetflowTransportMessage from a byte array data
     */
    NetflowTransportMessage<T> message(String metadata, byte[] data);


    /**
     * Gets a template from a template store for parsing netflow dataSets
     * @param transportMessage message with related netflow payload
     * @param header netflow header of the processed packet
     * @param templateId from netflow packet
     * @return           template if available otherwise the Optional.empty() object
     */
    Optional<List<NetflowField>> getTemplate(NetflowTransportMessage<T> transportMessage,
                                             NetflowHeader header,
                                             int templateId);

    /**
     * Updates templates store by the provided template
     * @param transportMessage message with related netflow packet
     * @param header netflow header of the processed packet
     * @param templateId templateId from netflow packet
     * @param template template for update
     */

    void updateTemplate(NetflowTransportMessage<T> transportMessage,
                        NetflowHeader header,
                        int templateId,
                        List<NetflowField> template);
}
