package uk.co.gresearch.siembol.configeditor.service.parserconfig;

import uk.co.gresearch.siembol.configeditor.configinfo.ConfigInfoProvider;
import uk.co.gresearch.siembol.configeditor.common.ConfigInfoType;
import uk.co.gresearch.siembol.configeditor.configinfo.JsonConfigInfoProvider;

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
