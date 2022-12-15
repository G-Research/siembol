package uk.co.gresearch.siembol.configeditor.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
/**
 * A data transfer object that represents config tester attributes
 *
 * <p>This class represents config tester attributes such as the name, test specification schema and
 * flags about the tester.
 *
 * @author  Marian Novotny
 * @see JsonProperty
 * @see JsonRawValue
 */
public class ConfigTesterDto {
    private String name;
    @JsonProperty("test_schema")
    @JsonRawValue
    private String testSchema;
    @JsonProperty("config_testing")
    private boolean configTesting;
    @JsonProperty("test_case_testing")
    private boolean testCaseTesting;
    @JsonProperty("release_testing")
    private boolean releaseTesting;

    @JsonProperty("incomplete_result")
    private boolean incompleteResult;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTestSchema() {
        return testSchema;
    }

    public void setTestSchema(String testSchema) {
        this.testSchema = testSchema;
    }

    public boolean isConfigTesting() {
        return configTesting;
    }

    public void setConfigTesting(boolean configTesting) {
        this.configTesting = configTesting;
    }

    public boolean isTestCaseTesting() {
        return testCaseTesting;
    }

    public void setTestCaseTesting(boolean testCaseTesting) {
        this.testCaseTesting = testCaseTesting;
    }

    public boolean isReleaseTesting() {
        return releaseTesting;
    }

    public void setReleaseTesting(boolean releaseTesting) {
        this.releaseTesting = releaseTesting;
    }

    public boolean isIncompleteResult() {
        return incompleteResult;
    }

    public void setIncompleteResult(boolean incompleteResult) {
        this.incompleteResult = incompleteResult;
    }
}
