package uk.co.gresearch.siembol.configeditor.sync.actions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.common.model.OverriddenApplicationAttributesDto;
import uk.co.gresearch.siembol.common.model.StormParsingApplicationAttributesDto;
import uk.co.gresearch.siembol.common.model.StormTopologyDto;

import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorServiceContext;
import uk.co.gresearch.siembol.configeditor.sync.common.ConfigServiceHelper;
import uk.co.gresearch.siembol.parsers.application.model.ParsingApplicationDto;
import uk.co.gresearch.siembol.parsers.application.model.ParsingApplicationsDto;

import java.lang.invoke.MethodHandles;
import java.util.*;

public class GetParsingAppStormTopologyAction implements SynchronisationAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final ObjectReader ADMIN_CONFIG_READER = new ObjectMapper()
            .readerFor(StormParsingApplicationAttributesDto.class);
    private static final ObjectWriter ADMIN_CONFIG_WRITER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .writerFor(StormParsingApplicationAttributesDto.class);
    private static final ObjectReader PARSING_APP_RELEASE_READER = new ObjectMapper()
            .readerFor(ParsingApplicationsDto.class);
    private static final ObjectWriter PARSING_APP_WRITER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .writerFor(ParsingApplicationDto.class);
    private static final String MISSING_ATTRIBUTES_MSG = "Missing admin config and release for the service: %s";
    private static final String WRONG_CONFIG_MSG = "Exception during json processing in the service {}, exception {}";
    private static final String SKIPPING_RELEASING_TOPOLOGY =
            "Skipping releasing topology for the service {} since it has init admin config or release";
    private final ConfigServiceHelper serviceHelper;

    public GetParsingAppStormTopologyAction(ConfigServiceHelper serviceHelper) {
        this.serviceHelper = serviceHelper;
    }

    @Override
    public ConfigEditorResult execute(ConfigEditorServiceContext context) {
        if (context.getAdminConfig() == null || context.getConfigRelease() == null) {
            String msg = String.format(MISSING_ATTRIBUTES_MSG, serviceHelper.getName());
            LOGGER.error(msg);
            return ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.ERROR, msg);
        }

        if (serviceHelper.isInitAdminConfig(context.getAdminConfig())
                || serviceHelper.isInitRelease(context.getConfigRelease())) {
            LOGGER.warn(SKIPPING_RELEASING_TOPOLOGY, serviceHelper.getName());
            return ConfigEditorResult.fromServiceContext(context);
        }

        final String adminConfigStr = context.getAdminConfig();
        final String release = context.getConfigRelease();
        List<StormTopologyDto> topologies = new ArrayList<>();
        try {
            StormParsingApplicationAttributesDto adminConfig = ADMIN_CONFIG_READER.readValue(adminConfigStr);
            ParsingApplicationsDto applications = PARSING_APP_RELEASE_READER.readValue(release);

            for (ParsingApplicationDto application : applications.getParsingApplications()) {
                StormTopologyDto topology = new StormTopologyDto();
                topology.setTopologyName(adminConfig.getTopologyName(application.getParsingApplicationName()));

                StormParsingApplicationAttributesDto currentAdminConfig = ADMIN_CONFIG_READER.readValue(adminConfigStr);
                //NOTE: we replace the main admin config attributes for an overridden application
                //THe reason is not to change attributes for storm topologies that are not related to the config change
                currentAdminConfig.setOverriddenApplications(null);
                currentAdminConfig.setConfigVersion(null);
                if (adminConfig.getOverriddenApplications() != null) {
                    for (OverriddenApplicationAttributesDto overriddenApp : adminConfig.getOverriddenApplications()) {
                        if (overriddenApp.getApplicationName().equals(application.getParsingApplicationName())) {
                            currentAdminConfig.setStormAttributes(overriddenApp.getStormAttributes());
                            currentAdminConfig.setKafkaBatchWriterAttributes(
                                    overriddenApp.getKafkaBatchWriterAttributes());
                        }
                    }
                }

                List<String> attr = new ArrayList<>();
                String currentAdminConfigStr = ADMIN_CONFIG_WRITER.writeValueAsString(currentAdminConfig);
                attr.add(Base64.getEncoder().encodeToString(currentAdminConfigStr.getBytes()));
                String applicationStr = PARSING_APP_WRITER.writeValueAsString(application);
                attr.add(Base64.getEncoder().encodeToString(applicationStr.getBytes()));
                topology.setAttributes(attr);

                topology.setServiceName(serviceHelper.getName());
                topology.setImage(serviceHelper.getStormTopologyImage().get());
                topology.setTopologyId(UUID.randomUUID().toString());
                topologies.add(topology);
            }
            context.setStormTopologies(Optional.of(topologies));
        } catch (Exception e) {
            LOGGER.error(WRONG_CONFIG_MSG, serviceHelper.getName(), ExceptionUtils.getStackTrace(e));
            return ConfigEditorResult.fromException(e);
        }

        return ConfigEditorResult.fromServiceContext(context);
    }
}
