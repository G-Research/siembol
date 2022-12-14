package uk.co.gresearch.siembol.configeditor.configinfo;
/**
 * An object for providing metadata about a json rule change
 *
 * <p>This class implements ConfigInfoProvider interface. It provides metadata about a json rule change.
 * It provides information such as the author of the change, type of change, and the version of the configuration.
 *
 * @author  Marian Novotny
 * @see ConfigInfoProvider
 *
 */
public class JsonRuleConfigInfoProvider {
    private static final String AUTHOR_FIELD = "rule_author";
    private static final String NAME_FIELD = "rule_name";
    private static final String VERSION_FIELD = "rule_version";
    private static final String RELEASE_VERSION_FIELD = "rules_version";

    public static ConfigInfoProvider create() {
        return new JsonConfigInfoProvider.Builder()
                .configAuthorField(AUTHOR_FIELD)
                .configNameField(NAME_FIELD)
                .configsVersionField(RELEASE_VERSION_FIELD)
                .configVersionField(VERSION_FIELD)
                .build();
    }
}
