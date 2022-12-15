package uk.co.gresearch.siembol.configeditor.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.gresearch.siembol.common.model.StormTopologyDto;

import java.util.List;
import java.util.Optional;
/**
 * An object that represents config editor service context
 *
 * <p>This class represents config editor service context.
 *
 * @author  Marian Novotny
 * @see JsonProperty
 */
public class ConfigEditorServiceContext {
    private String serviceName;
    private String configRelease;
    private String adminConfig;
    private Optional<List<StormTopologyDto>> stormTopologies = Optional.empty();

    public String getConfigRelease() {
        return configRelease;
    }

    public void setConfigRelease(String configRelease) {
        this.configRelease = configRelease;
    }

    public String getAdminConfig() {
        return adminConfig;
    }

    public void setAdminConfig(String adminConfig) {
        this.adminConfig = adminConfig;
    }

    public Optional<List<StormTopologyDto>> getStormTopologies() {
        return stormTopologies;
    }

    public void setStormTopologies(Optional<List<StormTopologyDto>> stormTopologies) {
        this.stormTopologies = stormTopologies;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
