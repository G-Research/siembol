package uk.co.gresearch.siembol.configeditor.sync.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.common.model.StormTopologyDto;

import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorServiceContext;
import uk.co.gresearch.siembol.configeditor.sync.common.ConfigServiceHelper;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

public class GetStormTopologyAction implements SynchronisationAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String MISSING_ADMIN_CONFIG = "Missing admin configuration for the service %s";
    private static final String MISSING_TOPOLOGY_NAME_OR_IMAGE = "Missing topology name or image for the service %s";

    private final ConfigServiceHelper serviceHelper;

    public GetStormTopologyAction(ConfigServiceHelper serviceHelper) {
        this.serviceHelper = serviceHelper;
    }

    @Override
    public ConfigEditorResult execute(ConfigEditorServiceContext context) {
        if (context.getAdminConfig() == null) {
            String msg = String.format(MISSING_ADMIN_CONFIG, serviceHelper.getName());
            LOGGER.error(msg);
            return ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.ERROR, msg);
        }

        final String adminConfig = context.getAdminConfig();
        StormTopologyDto topology = new StormTopologyDto();
        Optional<String> topologyName = serviceHelper.getStormTopologyName(adminConfig);
        Optional<String> topologyImage = serviceHelper.getStormTopologyImage();
        if (!topologyName.isPresent() || !topologyImage.isPresent()) {
            String msg = String.format(MISSING_TOPOLOGY_NAME_OR_IMAGE, serviceHelper.getName());
            LOGGER.error(msg);
            return ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.ERROR, msg);
        }

        topology.setImage(topologyImage.get());
        topology.setTopologyName(topologyName.get());
        String argument = Base64.getEncoder().encodeToString(adminConfig.getBytes());
        topology.setAttributes(Arrays.asList(argument));
        topology.setServiceName(serviceHelper.getName());
        topology.setTopologyId(UUID.randomUUID().toString());

        context.setStormTopologies(Optional.of(Arrays.asList(topology)));
        return ConfigEditorResult.fromServiceContext(context);
    }
}
