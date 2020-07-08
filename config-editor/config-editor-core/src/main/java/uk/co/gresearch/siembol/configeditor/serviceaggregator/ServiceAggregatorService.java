package uk.co.gresearch.siembol.configeditor.serviceaggregator;

import uk.co.gresearch.siembol.configeditor.common.ConfigSchemaService;
import uk.co.gresearch.siembol.configeditor.configstore.ConfigStore;

public class ServiceAggregatorService {
    private final String type;
    private final ConfigStore configStore;
    private final ConfigSchemaService configSchemaService;

    public ServiceAggregatorService(String type, ConfigStore configStore, ConfigSchemaService configSchemaService) {
        this.type = type;
        this.configStore = configStore;
        this.configSchemaService = configSchemaService;
    }

    public String getType() {
        return type;
    }

    public ConfigStore getConfigStore() {
        return configStore;
    }

    public ConfigSchemaService getConfigSchemaService() {
        return configSchemaService;
    }
}
