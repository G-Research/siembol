package uk.co.gresearch.nortem.configeditor.testcase.model;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.SchemaIgnore;
import uk.co.gresearch.nortem.common.jsonschema.JsonRawStringDto;

import java.util.List;

@Attributes(title = "test case", description = "Test case for testing configurations")
public class TestCaseDto {
    @JsonProperty("test_case_name")
    @Attributes(required = true, description = "The name of the test case")
    private String testCaseName;
    @Attributes(required = true, description = "Version of the test case")
    private Integer version;
    @Attributes(required = true, description = "Author of the test case")
    private String author;

    @JsonProperty("config_name")
    @Attributes(required = true, description = "Version of the test case")
    private String configName;

    @Attributes(description = "Description of the test case")
    private String description;

    @JsonProperty("test_specification")
    @Attributes(required = true, description = "Test specification")
    private JsonRawStringDto testSpecification;

    @JsonIgnore
    @SchemaIgnore
    private String testSpecificationContent;

    @Attributes(required = true, description = "Test case assertions", minItems = 1)
    private List<TestAssertionDto> assertions;

    public String getTestCaseName() {
        return testCaseName;
    }

    public void setTestCaseName(String testCaseName) {
        this.testCaseName = testCaseName;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonSetter("test_specification")
    public void setTestSpecification(JsonNode testSpecification) {
        this.testSpecificationContent = testSpecification.toString();
    }

    @JsonGetter("test_specification")
    @JsonRawValue
    public String getTestSpecificationContent() {
        return testSpecificationContent;
    }

    public List<TestAssertionDto> getAssertions() {
        return assertions;
    }

    public void setAssertions(List<TestAssertionDto> assertions) {
        this.assertions = assertions;
    }

    public JsonRawStringDto getTestSpecification() {
        return testSpecification;
    }
}
