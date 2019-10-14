package uk.co.gresearch.nortem.configeditor.service.parsingapp;

import uk.co.gresearch.nortem.configeditor.configstore.ConfigInfoProvider;
import uk.co.gresearch.nortem.configeditor.configstore.JsonConfigInfoProvider;

public class ParsingAppConfigInfoProvider {
    private static final String AUTHOR_FIELD = "parsing_app_author";
    private static final String NAME_FIELD = "parsing_app_name";
    private static final String VERSION_FIELD = "parsing_app_version";
    private static final String RELEASE_VERSION_FIELD = "parsing_applications_version";
    private static final String PARSERS_FILENAME = "parsing_applications.json";

    public static ConfigInfoProvider create() {
        return new JsonConfigInfoProvider.Builder()
                .configAuthorField(AUTHOR_FIELD)
                .configNameField(NAME_FIELD)
                .configsVersionField(RELEASE_VERSION_FIELD)
                .configVersionField(VERSION_FIELD)
                .useConfigWordingInGitMessages()
                .releaseFilename(PARSERS_FILENAME)
                .build();
    }
}
