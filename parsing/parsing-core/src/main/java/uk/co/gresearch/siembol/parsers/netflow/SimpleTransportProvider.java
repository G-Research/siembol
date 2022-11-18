package uk.co.gresearch.siembol.parsers.netflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.*;
/**
 * An object for providing netflow templates
 *
 * <p>This class is implementing NetflowTransportProvider interface.
 * It uses an in-memory map for storing and obtaining templates.
 * It uses NetflowMessageWithSource implementation.
 * This map is not synchronised since it is not shared between threads in the storm integration.
 *
 * @author Marian Novotny
 * @see NetflowTransportProvider
 * @see NetflowMessageWithSource
 *
 */
public class SimpleTransportProvider implements NetflowTransportProvider<String> {
    private static final Logger LOG = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());

    private final Map<String, List<NetflowField>> templates
            = new HashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public NetflowTransportMessage<String> message(String metadata, byte[] data) {
        return new NetflowMessageWithSource(metadata, data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<List<NetflowField>> getTemplate(NetflowTransportMessage<String> transportMessage,
                                                          NetflowHeader header,
                                                          int templateId) {

        String key = transportMessage.getGlobalTemplateId(header, templateId);
        return Optional.ofNullable(templates.get(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateTemplate(NetflowTransportMessage<String> transportMessage,
                               NetflowHeader header,
                               int templateId,
                               List<NetflowField> template) {
        String key = transportMessage.getGlobalTemplateId(header, templateId);
        if (!templates.containsKey(key)) {
            LOG.debug(String.format("New template, source identifier: %s, source_id: %d, template_id: %d",
                    transportMessage.getGlobalSource(),
                    header.getSourceId(),
                    templateId));
            templates.put(key, Collections.unmodifiableList(template));
            return;
         }

         List<NetflowField> existing = templates.get(key);
         if (!existing.equals(template)){
            LOG.error(String.format(
                    "Template differs, source identifier: %s, source_id: %d, template_id: %d",
                    transportMessage.getGlobalSource(),
                    header.getSourceId(),
                    templateId));
            templates.put(key, Collections.unmodifiableList(template));
         }
    }
}
