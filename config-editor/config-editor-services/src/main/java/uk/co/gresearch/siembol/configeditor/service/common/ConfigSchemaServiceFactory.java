package uk.co.gresearch.siembol.configeditor.service.common;

import uk.co.gresearch.siembol.configeditor.common.ConfigSchemaService;

import java.util.Map;
import java.util.Optional;

public interface ConfigSchemaServiceFactory {
    ConfigSchemaService createConfigSchemaService(Optional<String> uiLayoutConfig,
                                                  Optional<String> uiTestLayoutConfig,
                                                  Optional<Map<String, String>> attributes) throws Exception;
}
