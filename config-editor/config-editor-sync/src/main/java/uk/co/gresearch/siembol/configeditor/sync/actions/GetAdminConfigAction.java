package uk.co.gresearch.siembol.configeditor.sync.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorServiceContext;
import uk.co.gresearch.siembol.configeditor.sync.common.ConfigServiceHelper;

import java.lang.invoke.MethodHandles;
import java.util.Optional;

import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.ERROR;

public class GetAdminConfigAction implements SynchronisationAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String GET_ADMIN_CONFIG_ERROR = "Problem obtaining admin config for the service %s";
    private static final String INVALID_ADMIN_CONFIG_ERROR = "The admin configuration is invalid for the service %s";

    private final ConfigServiceHelper serviceHelper;

    public GetAdminConfigAction(ConfigServiceHelper serviceHelper) {
        this.serviceHelper = serviceHelper;
    }

    @Override
    public ConfigEditorResult execute(ConfigEditorServiceContext context) {
        Optional<String> adminConfig = serviceHelper.getAdminConfig();
        if (!adminConfig.isPresent()) {
            String msg = String.format(GET_ADMIN_CONFIG_ERROR, serviceHelper.getName());
            LOGGER.error(msg);
            return ConfigEditorResult.fromMessage(ERROR, msg);
        }

        if (!serviceHelper.validateAdminConfiguration(adminConfig.get())) {
            String msg = String.format(INVALID_ADMIN_CONFIG_ERROR, serviceHelper.getName());
            LOGGER.error(msg);
            return ConfigEditorResult.fromMessage(ERROR, msg);
        }

        context.setAdminConfig(adminConfig.get());
        return ConfigEditorResult.fromServiceContext(context);
    }
}
