package uk.co.gresearch.siembol.configeditor.common;

import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.model.ConfigTesterDto;

import java.util.EnumSet;

public interface ConfigTester {
    String DEFAULT_NAME = "default";
    String NOT_SUPPORTED_MSG = "This type of testing is not supported";

    default String getName() {
        return DEFAULT_NAME;
    }

    EnumSet<ConfigTesterFlag> getFlags();

    default ConfigTesterDto getConfigTesterInfo() {
        var ret = new ConfigTesterDto();
        ret.setName(getName());
        var flags = getFlags();
        ret.setConfigTesting(flags.contains(ConfigTesterFlag.CONFIG_TESTING));
        ret.setTestCaseTesting(flags.contains(ConfigTesterFlag.TEST_CASE_TESTING));
        ret.setReleaseTesting(flags.contains(ConfigTesterFlag.RELEASE_TESTING));
        ret.setIncompleteResult(flags.contains(ConfigTesterFlag.INCOMPLETE_RESULT));
        ret.setTestSchema(getTestSpecificationSchema().getAttributes().getTestSchema());
        return ret;
    }

    ConfigEditorResult getTestSpecificationSchema();

    ConfigEditorResult validateTestSpecification(String attributes);

    default ConfigEditorResult testConfiguration(String configuration, String testSpecification) {
        return ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.BAD_REQUEST, NOT_SUPPORTED_MSG);
    }

    default ConfigEditorResult testConfigurations(String configurations, String testSpecification) {
        return ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.BAD_REQUEST, NOT_SUPPORTED_MSG);
    }

    default ConfigTester withErrorMessage() {
        return new ConfigTesterWithErrorMessage(this);
    }
}
