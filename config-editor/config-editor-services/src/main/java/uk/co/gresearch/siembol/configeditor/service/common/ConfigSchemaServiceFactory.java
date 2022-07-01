package uk.co.gresearch.siembol.configeditor.service.common;

import uk.co.gresearch.siembol.configeditor.common.ConfigSchemaService;
import uk.co.gresearch.siembol.configeditor.model.AdditionalConfigTesters;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorUiLayout;

import java.util.Map;
import java.util.Optional;

public interface ConfigSchemaServiceFactory {
    ConfigSchemaService createConfigSchemaService(
            ConfigEditorUiLayout uiLayout,
            Optional<Map<String, String>> attributes,
            Optional<AdditionalConfigTesters> additionalTesters) throws Exception;
}
