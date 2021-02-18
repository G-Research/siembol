package uk.co.gresearch.siembol.configeditor.sync.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorServiceContext;
import uk.co.gresearch.siembol.configeditor.sync.common.ConfigServiceHelper;

import java.lang.invoke.MethodHandles;
import java.util.Optional;

import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.ERROR;


public class GetReleaseAction implements SynchronisationAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String GET_RELEASE_ERROR = "Problem obtaining release for the service %s";
    private static final String INVALID_RELEASE_ERROR = "The release is invalid for the service %s";

    private final ConfigServiceHelper serviceHelper;

    public GetReleaseAction(ConfigServiceHelper serviceHelper) {
        this.serviceHelper = serviceHelper;
    }

    @Override
    public ConfigEditorResult execute(ConfigEditorServiceContext context) {
        Optional<String> release = serviceHelper.getConfigsRelease();
        if (!release.isPresent()) {
            String msg = String.format(GET_RELEASE_ERROR, serviceHelper.getName());
            LOGGER.error(msg);
            return ConfigEditorResult.fromMessage(ERROR, msg);
        }

        if (!serviceHelper.validateConfigurations(release.get())) {
            String msg = String.format(INVALID_RELEASE_ERROR, serviceHelper.getName());
            LOGGER.error(msg);
            return ConfigEditorResult.fromMessage(ERROR, msg);
        }

        context.setConfigRelease(release.get());
        return ConfigEditorResult.fromServiceContext(context);
    }
}
