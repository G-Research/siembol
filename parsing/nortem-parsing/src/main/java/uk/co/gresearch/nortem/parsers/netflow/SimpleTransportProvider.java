package uk.co.gresearch.nortem.parsers.netflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.*;

public class SimpleTransportProvider implements NetflowTransportProvider<String> {
    private static final Logger LOG = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());

    private final Map<String, List<NetflowField>> templates
            = new HashMap<>();

    @Override
    public NetflowTransportMessage<String> message(byte[] data) {
        return new NetflowMessageWithSource(data);
    }

    @Override
    public Optional<List<NetflowField>> getTemplate(NetflowTransportMessage<String> transportMessage,
                                                          NetflowHeader header,
                                                          int templateId) {

        String key = transportMessage.getGlobalTemplateId(header, templateId);
        return Optional.ofNullable(templates.get(key));
    }

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
