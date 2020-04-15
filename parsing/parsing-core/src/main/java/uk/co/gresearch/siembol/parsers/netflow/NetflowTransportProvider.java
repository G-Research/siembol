package uk.co.gresearch.siembol.parsers.netflow;
import java.util.List;
import java.util.Optional;

public interface NetflowTransportProvider<T> {

    /**
     * Get a netflow transport message from binary data
     * @param  data byte array that contains netflow packet along with source identification
     * @return creates NetflowTransportMessage from an byte array data
     */
    NetflowTransportMessage<T> message(String metadata, byte[] data);


    /**
     * Get a template from a template store for parsing netflow dataSets
     * @param transportMessage message with related netflow packet
     * @param header netflow header of the processed packet
     * @param templateId  from netflow packet
     * @return template from the store if available otherwise the empty
     */
    Optional<List<NetflowField>> getTemplate(NetflowTransportMessage<T> transportMessage,
                                             NetflowHeader header,
                                             int templateId);

    /**
     * Update templates store by the provided template
     * @param transportMessage message with related netflow packet
     * @param header netflow header of the processed packet
     * @param templateId tenplateId from netwflow packet
     * @param template template for update
     * @return creates NetflowTransportMessage from an byte array
     */

    void updateTemplate(NetflowTransportMessage<T> transportMessage,
                        NetflowHeader header,
                        int templateId,
                        List<NetflowField> template);
}
