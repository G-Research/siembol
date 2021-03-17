package uk.co.gresearch.siembol.configeditor.configinfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.configeditor.common.UserInfo;
import uk.co.gresearch.siembol.configeditor.common.ConfigInfoProvider;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorFile;
import uk.co.gresearch.siembol.configeditor.common.ConfigInfo;
import uk.co.gresearch.siembol.configeditor.common.ConfigInfoType;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonConfigInfoProvider implements ConfigInfoProvider {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String RULE_COMMIT_TEMPLATE_NEW = "Adding new %s: %s";
    private static final String RULE_COMMIT_TEMPLATE_UPDATE = "Updating %s: %s to version: %d";
    private static final String RULE_COMMIT_TEMPLATE_RELEASE = "%s released to version: %d";
    private static final ObjectReader JSON_READER = new ObjectMapper()
            .readerFor(new TypeReference<Map<String, Object>>() { });
    private static final String WRONG_RELEASE_FORMAT = "Wrong config release json file format in {}";
    private static final String MISSING_VERSION_RELEASE = "Missing version in release {}, probably init version";
    private static final String WRONG_CONFIG_FORMAT = "Wrong config json file format";
    private static final String MISSING_FILENAME_MSG = "Missing filename: {}, probably init version";
    private static final String WRONG_FILENAME_MSG = "Wrong config name: %s";
    private static final String PREFIX_NAME_FORMAT = "%s-%s";
    private static final String PREFIX_NAME_CHECK_FORMAT = "%s_%s";

    private final String configNameField;
    private String configNamePrefixField;
    private final String configAuthorField;
    private final String configVersionField;
    private final String configsVersionField;
    private final String configFilenameFormat;
    private final String releaseFilename;
    private final String jsonFileSuffix;
    private final String ruleVersionRegex;
    private final String releaseVersionRegex;
    private final String ruleAuthorRegex;
    private final String ruleVersionFormat;
    private final String ruleAuthorFormat;
    private final String releaseVersionFormat;
    private final String commitTemplateNew;
    private final String commitTemplateUpdate;
    private final String commitTemplateRelease;
    private final Pattern ruleNamePattern;
    private final ConfigInfoType configType;

    JsonConfigInfoProvider(Builder builder) {
        this.configNameField = builder.configNameField;
        this.configAuthorField = builder.configAuthorField;
        this.configVersionField = builder.configVersionField;
        this.configsVersionField = builder.configsVersionField;
        this.configFilenameFormat = builder.configFilenameFormat;
        this.releaseFilename = builder.releaseFilename;
        this.jsonFileSuffix = builder.jsonFileSuffix;
        this.ruleVersionRegex = builder.ruleVersionRegex;
        this.releaseVersionRegex = builder.releaseVersionRegex;
        this.ruleAuthorRegex = builder.ruleAuthorRegex;
        this.ruleVersionFormat = builder.ruleVersionFormat;
        this.ruleAuthorFormat = builder.ruleAuthorFormat;
        this.releaseVersionFormat = builder.releaseVersionFormat;
        this.ruleNamePattern = builder.ruleNamePattern;
        this.commitTemplateNew = builder.commitTemplateNew;
        this.commitTemplateUpdate = builder.commitTemplateUpdate;
        this.commitTemplateRelease = builder.commitTemplateRelease;
        this.configNamePrefixField = builder.configNamePrefixField;
        this.configType = builder.configType;
    }

    @Override
    public ConfigInfo getConfigInfo(UserInfo user, String config) {
        ConfigInfo configInfo = configInfoFromUser(user);

        Map<String, Object> metadata;
        try {
            metadata = JSON_READER.readValue(config);
        } catch (IOException e) {
            throw new IllegalArgumentException(WRONG_CONFIG_FORMAT);
        }
        if (metadata == null
                || !(metadata.get(configVersionField) instanceof Number)
                || !(metadata.get(configAuthorField) instanceof String)
                || !(metadata.get(configNameField) instanceof String)
                || (configNamePrefixField != null && !(metadata.get(configNamePrefixField) instanceof String))) {
            throw new IllegalArgumentException(WRONG_CONFIG_FORMAT);
        }

        String nameToCheck = String.format(PREFIX_NAME_CHECK_FORMAT,
                metadata.get(configNameField), metadata.get(configNamePrefixField));
        Matcher nameMatcher = ruleNamePattern.matcher(nameToCheck);
        if (!nameMatcher.matches()) {
            throw new IllegalArgumentException(
                    String.format(WRONG_FILENAME_MSG, nameToCheck));
        }

        String configName = configNamePrefixField == null
                ? (String)metadata.get(configNameField)
                : String.format(PREFIX_NAME_FORMAT, metadata.get(configNamePrefixField), metadata.get(configNameField));
        configInfo.setName(configName);
        String configAuthor = (String)metadata.get(configAuthorField);
        int configVersion = ((Number)metadata.get(configVersionField)).intValue();

        int newConfigVersion = configVersion + 1;
        configInfo.setOldVersion(configVersion);
        configInfo.setVersion(newConfigVersion);
        String commitMsg = configVersion == 0
                ? String.format(commitTemplateNew, configName)
                : String.format(commitTemplateUpdate, configName, newConfigVersion);
        configInfo.setCommitMessage(commitMsg);

        Map<String, String> files = new HashMap<>();
        String updatedRule = config.replaceFirst(ruleVersionRegex,
                String.format(ruleVersionFormat, newConfigVersion));

        if (!configAuthor.equals(configInfo.getCommitter())) {
            //NOTE: we consider author to be the last committer,
            // auth logic can be added here when needed
            updatedRule = updatedRule.replaceFirst(ruleAuthorRegex,
                    String.format(ruleAuthorFormat, configInfo.getCommitter()));
        }

        files.put(String.format(configFilenameFormat, configName), updatedRule);
        configInfo.setFilesContent(files);

        configInfo.setConfigInfoType(configType);
        return configInfo;
    }

    @Override
    public ConfigInfo getReleaseInfo(UserInfo user, String release) {
        ConfigInfo configInfo = configInfoFromUser(user);

        int releaseVersion = getReleaseVersion(release);

        int newRulesVersion = releaseVersion + 1;
        configInfo.setVersion(newRulesVersion);
        configInfo.setOldVersion(releaseVersion);
        configInfo.setBranchName(String.format(RELEASE_BRANCH_TEMPLATE,
                newRulesVersion,
                configInfo.getCommitter(),
                getLocalDateTime()));

        configInfo.setCommitMessage(String.format(commitTemplateRelease, newRulesVersion));

        String updatedRelease = release.replaceFirst(releaseVersionRegex,
                String.format(releaseVersionFormat, newRulesVersion));

        Map<String, String> files = new HashMap<>();
        files.put(releaseFilename, updatedRelease);
        configInfo.setFilesContent(files);
        configInfo.setConfigInfoType(configType);

        return configInfo;
    }

    @Override
    public int getReleaseVersion(String content) {
        Map<String, Object> metadata;
        try {
            metadata = JSON_READER.readValue(content);
        } catch (IOException e) {
            LOG.warn(WRONG_RELEASE_FORMAT, content);
            return INIT_RELEASE_VERSION;
        }
        if (metadata == null
                || !(metadata.get(configsVersionField) instanceof Number)) {
            LOG.warn(MISSING_VERSION_RELEASE, content);
            return INIT_RELEASE_VERSION;
        }

        return ((Number)metadata.get(configsVersionField)).intValue();
    }

    @Override
    public int getReleaseVersion(List<ConfigEditorFile> files) {
        Optional<ConfigEditorFile> release = files
                .stream()
                .filter(x -> x.getFileName().equals(releaseFilename))
                .findFirst();
        if (!release.isPresent()) {
            LOG.warn(MISSING_FILENAME_MSG, releaseFilename);
            return INIT_RELEASE_VERSION;
        }

        return getReleaseVersion(release.get().getContent());
    }

    @Override
    public ConfigEditorFile.ContentType getFileContentType() {
        return ConfigEditorFile.ContentType.RAW_JSON_STRING;
    }

    @Override
    public boolean isStoreFile(String filename) {
        return filename.endsWith(jsonFileSuffix);
    }

    @Override
    public boolean isReleaseFile(String filename) {
        return releaseFilename.equals(filename);
    }

    @Override
    public ConfigInfoType getConfigInfoType() {
        return configType;
    }

    public static class Builder {
        private static final String COMMIT_TEMPLATE_NEW = "Adding new %s: %%s";
        private static final String COMMIT_TEMPLATE_UPDATE = "Updating %s: %%s to version: %%d";
        private static final String COMMIT_TEMPLATE_RELEASE = "%s released to version: %%d";
        private static final String MISSING_ARGUMENTS = "Missing required argument for the builder";
        private static final String CONFIG_VERSION_REGEX_MSG = "\"%s\"\\s*:\\s*\\d+";
        private static final String RELEASE_VERSION_REGEX_MSG = "\"%s\"\\s*:\\s*\\d+";
        private static final String CONFIG_AUTHOR_REGEX_MSG = "\"%s\"\\s*:\\s*\"\\w+\"";
        private static final String CONFIG_VERSION_FORMAT_MSG = "\"%s\": %%d";
        private static final String CONFIG_AUTHOR_FORMAT_MSG = "\"%s\": \"%%s\"";
        private static final String RELEASE_VERSION_FORMAT = "\"%s\": %%d";
        private ConfigInfoType configType = ConfigInfoType.RULE;
        private String configNameField;
        private String configNamePrefixField;
        private String configAuthorField;
        private String configVersionField;
        private String configsVersionField;
        private String configFilenameFormat = "%s.json";
        private String releaseFilename = "rules.json";
        private String jsonFileSuffix = "json";
        private String ruleVersionRegex;
        private String releaseVersionRegex;
        private String ruleAuthorRegex;
        private String ruleVersionFormat;
        private String ruleAuthorFormat;
        private String releaseVersionFormat;
        private Pattern ruleNamePattern = Pattern.compile("^[a-zA-Z0-9_\\-]+$");
        private String commitTemplateNew = RULE_COMMIT_TEMPLATE_NEW;
        private String commitTemplateUpdate = RULE_COMMIT_TEMPLATE_UPDATE;
        private String commitTemplateRelease = RULE_COMMIT_TEMPLATE_RELEASE;

        public Builder configNameField(String configNameField) {
            this.configNameField = configNameField;
            return this;
        }

        public Builder configNamePrefixField(String configNamePrefixField) {
            this.configNamePrefixField = configNamePrefixField;
            return this;
        }

        public Builder configAuthorField(String configAuthorField) {
            this.configAuthorField = configAuthorField;
            return this;
        }

        public Builder configVersionField(String versionField) {
            this.configVersionField = versionField;
            return this;
        }

        public Builder configsVersionField(String configsVersionField) {
            this.configsVersionField = configsVersionField;
            return this;
        }

        public Builder configFilenameFormat(String configFilenameFormat) {
            this.configFilenameFormat = configFilenameFormat;
            return this;
        }

        public Builder releaseFilename(String releaseFilename) {
            this.releaseFilename = releaseFilename;
            return this;
        }

        public Builder setConfigInfoType(ConfigInfoType configType) {
           this.configType = configType;
           return this;
        }

        public JsonConfigInfoProvider build() {
            if (configNameField == null
                    || configAuthorField == null
                    || configVersionField == null
                    || configsVersionField == null
                    || configFilenameFormat == null
                    || releaseFilename == null
                    || jsonFileSuffix == null) {
                throw new IllegalArgumentException(MISSING_ARGUMENTS);
            }

            ruleVersionRegex = String.format(CONFIG_VERSION_REGEX_MSG, configVersionField);
            ruleVersionFormat = String.format(CONFIG_VERSION_FORMAT_MSG, configVersionField);
            releaseVersionRegex = String.format(RELEASE_VERSION_REGEX_MSG, configsVersionField);
            ruleAuthorRegex = String.format(CONFIG_AUTHOR_REGEX_MSG, configAuthorField);
            ruleAuthorFormat = String.format(CONFIG_AUTHOR_FORMAT_MSG, configAuthorField);
            releaseVersionFormat = String.format(RELEASE_VERSION_FORMAT, configsVersionField);

            commitTemplateNew = String.format(COMMIT_TEMPLATE_NEW, configType.getSingular());
            commitTemplateUpdate = String.format(COMMIT_TEMPLATE_UPDATE, configType.getSingular());
            commitTemplateRelease = String.format(COMMIT_TEMPLATE_RELEASE, configType.getReleaseName());

            return new JsonConfigInfoProvider(this);
        }
    }
}
