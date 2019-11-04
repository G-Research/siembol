package uk.co.gresearch.nortem.configeditor.service.parserconfig;

import uk.co.gresearch.nortem.configeditor.configstore.ConfigInfoProvider;
import uk.co.gresearch.nortem.configeditor.configstore.ConfigInfoType;
import uk.co.gresearch.nortem.configeditor.configstore.JsonConfigInfoProvider;

public class ParserConfigConfigInfoProvider {
    private static final String AUTHOR_FIELD = "parser_author";
    private static final String NAME_FIELD = "parser_name";
    private static final String VERSION_FIELD = "parser_version";
    private static final String RELEASE_VERSION_FIELD = "parsers_version";
    private static final String PARSERS_FILENAME = "parsers.json";

    public static ConfigInfoProvider create() {
        return new JsonConfigInfoProvider.Builder()
                .configAuthorField(AUTHOR_FIELD)
                .configNameField(NAME_FIELD)
                .configsVersionField(RELEASE_VERSION_FIELD)
                .configVersionField(VERSION_FIELD)
                .setConfigInfoType(ConfigInfoType.CONFIG)
                .releaseFilename(PARSERS_FILENAME)
                .build();
    }
}
