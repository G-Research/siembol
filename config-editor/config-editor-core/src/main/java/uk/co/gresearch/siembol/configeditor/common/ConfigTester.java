package uk.co.gresearch.siembol.configeditor.common;

import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.model.ConfigTesterDto;

import java.util.EnumSet;
/**
 * An object for testing configurations
 *
 * <p>This interface is for providing functionality for testing configurations.
 * Moreover, it validates a test specification and provides the test specification schema.
 *
 * @author  Marian Novotny
 *
 */
public interface ConfigTester {
    String DEFAULT_NAME = "default";
    String NOT_SUPPORTED_MSG = "This type of testing is not supported";

    /**
     * Gets the name of the tester
     * @return the name of teh tester
     */
    default String getName() {
        return DEFAULT_NAME;
    }

    /**
     * Gets tester flags
     * @return the set of test flags
     * @see ConfigTesterFlag
     */
    EnumSet<ConfigTesterFlag> getFlags();

    /**
     * Gets tester information
     * @return config tester info object
     * @see ConfigTesterDto
     */
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

    /**
     * Gets tester specification schema
     * @return config editor result with a test specification json schema
     */
    ConfigEditorResult getTestSpecificationSchema();

    /**
     * Validates test specification
     * @param attributes a json string with test specification
     * @return config editor result with OK status code if the specification is valid, otherwise
     *         the result with ERROR status.
     */
    ConfigEditorResult validateTestSpecification(String attributes);

    /**
     * Tests a configuration against a test specification
     * @param configuration a json string with configuration
     * @param testSpecification a json string with test specification
     * @return a config editor result with test result if the test was successful, otherwise
     *         the result with ERROR status.
     */
    default ConfigEditorResult testConfiguration(String configuration, String testSpecification) {
        return ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.BAD_REQUEST, NOT_SUPPORTED_MSG);
    }

    /**
     * Tests a configurations against a test specification
     * @param configurations a json string with configurations
     * @param testSpecification a json string with test specification
     * @return a config editor result with test result if the test was successful, otherwise
     *         the result with ERROR status.
     */
    default ConfigEditorResult testConfigurations(String configurations, String testSpecification) {
        return ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.BAD_REQUEST, NOT_SUPPORTED_MSG);
    }

    /**
     * Gets a config tester with enhanced error messages
     * @return a config tester with enhanced error messages
     */
    default ConfigTester withErrorMessage() {
        return new ConfigTesterWithErrorMessage(this);
    }
}
