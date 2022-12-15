package uk.co.gresearch.siembol.configeditor.common;

import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
/**
 * An object for importing configurations
 *
 * <p>This interface is for providing functionality for importing open standard configuration into Siembol.
 * Moreover, it validates attributes and provides importer attributes schema.
 *
 * @author  Marian Novotny
 *
 */
public interface ConfigImporter {
    /**
     * Gets a json schema for importer attributes
     * @return config editor result with json schema
     */
    ConfigEditorResult getImporterAttributesSchema();

    /**
     * Validates importer attributes
     * @param attributes a json string with importer attributes
     * @return config editor result with OK status code if the attributes are valid, otherwise
     *         the result with ERROR status.
     */
    ConfigEditorResult validateImporterAttributes(String attributes);

    /**
     * Imports open standard configuration into Siembol syntax
     * @param user a user info object
     * @param importerAttributes a json string with importer attributes
     * @param configuration configuration for importing into Siembol
     * @return config editor result with OK status code and the imported config if the import was successful, otherwise
     *         the result with ERROR status.
     */
    ConfigEditorResult importConfig(UserInfo user, String importerAttributes, String configuration);
}
