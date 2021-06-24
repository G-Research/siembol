package uk.co.gresearch.siembol.configeditor.sync.actions;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnector;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorServiceContext;
import uk.co.gresearch.siembol.configeditor.sync.common.ConfigServiceHelper;

import java.lang.invoke.MethodHandles;
import java.util.Optional;

public class UpdateReleaseInZookeeperAction implements SynchronisationAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String MISSING_RELEASE_ATTRIBUTES = "Missing release attributes for the service %s";
    private static final String NO_UPDATE_NEEDED_MSG = "No release update needed for the service: %s";
    private static final String UPDATE_START_MSG = "updating release in zookeeper for the service: {} to version: {}";
    private static final String UPDATE_COMPLETED_MSG = "updating release in zookeeper completed";
    private static final String UPDATE_ERROR_MSG = "Error during updating release for service {} in zookeeper {}";
    private static final String MISSING_ZOOKEEPER_CONNECTOR = "Missing zookeeper connector for the service %s";
    private static final String SKIPPING_UPDATING_ZOOKEEPER =
            "Skipping updating release in zookeeper for the service {} since it has init release";

    private final ConfigServiceHelper serviceHelper;
    private final ZooKeeperConnector zooKeeperConnector;

    public UpdateReleaseInZookeeperAction(ConfigServiceHelper serviceHelper) {
        this.serviceHelper = serviceHelper;

        Optional<ZooKeeperConnector> connectorOptional = serviceHelper.getZookeeperReleaseConnector();
        if (!connectorOptional.isPresent()) {
            throw new IllegalArgumentException(String.format(MISSING_ZOOKEEPER_CONNECTOR, serviceHelper.getName()));
        }
        this.zooKeeperConnector = connectorOptional.get();
    }

    @Override
    public ConfigEditorResult execute(ConfigEditorServiceContext context) {
        if (context.getConfigRelease() == null) {
            String msg = String.format(MISSING_RELEASE_ATTRIBUTES, serviceHelper.getName());
            LOGGER.error(msg);
            return ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.ERROR, msg);
        }

        if (serviceHelper.isInitRelease(context.getConfigRelease())) {
            LOGGER.warn(SKIPPING_UPDATING_ZOOKEEPER, serviceHelper.getName());
            return ConfigEditorResult.fromServiceContext(context);
        }

        String currentRelease = zooKeeperConnector.getData();
        int currentReleaseVersion = serviceHelper.getReleaseVersion(currentRelease);
        int updatedReleaseVersion = serviceHelper.getReleaseVersion(context.getConfigRelease());

        if (currentReleaseVersion >= updatedReleaseVersion) {
            String msg = String.format(NO_UPDATE_NEEDED_MSG, serviceHelper.getName());
            LOGGER.info(msg);
            ConfigEditorResult ret = ConfigEditorResult
                    .fromMessage(ConfigEditorResult.StatusCode.OK, NO_UPDATE_NEEDED_MSG);
            ret.getAttributes().setServiceContext(context);
            return ret;
        }

        LOGGER.info(UPDATE_START_MSG, serviceHelper.getName(), updatedReleaseVersion);
        try {
            zooKeeperConnector.setData(context.getConfigRelease());
        } catch (Exception e) {
            LOGGER.error(UPDATE_ERROR_MSG, serviceHelper.getName(), ExceptionUtils.getStackTrace(e));
            return ConfigEditorResult.fromException(e);
        }
        LOGGER.info(UPDATE_COMPLETED_MSG);

        return ConfigEditorResult.fromServiceContext(context);
    }
}
