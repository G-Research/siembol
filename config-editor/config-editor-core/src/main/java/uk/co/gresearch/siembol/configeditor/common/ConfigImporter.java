package uk.co.gresearch.siembol.configeditor.common;

import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;

public interface ConfigImporter {
    ConfigEditorResult getImporterAttributesSchema();
    ConfigEditorResult validateImporterAttributes(String attributes);
    ConfigEditorResult importConfig(UserInfo user, String importerAttributes, String configuration);
}
