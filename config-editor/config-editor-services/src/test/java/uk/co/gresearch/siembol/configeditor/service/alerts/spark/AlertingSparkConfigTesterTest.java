package uk.co.gresearch.siembol.configeditor.service.alerts.spark;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import uk.co.gresearch.siembol.common.jsonschema.SiembolJsonSchemaValidator;
import uk.co.gresearch.siembol.common.model.testing.AlertingSparkArgumentDto;
import uk.co.gresearch.siembol.configeditor.common.ConfigTesterFlag;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.model.SparkHdfsTesterProperties;

import java.util.Base64;
import java.util.HashMap;

import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.*;

public class AlertingSparkConfigTesterTest {
    private static final ObjectReader ARGUMENT_READER = new ObjectMapper()
            .readerFor(AlertingSparkArgumentDto.class);
    private final String testSpecification = """
            {
              "source_type": "siembol-logs",
              "max_result": 1000,
              "from_date": "2022-06-01",
              "to_date": "2022-06-05"
            }
            """;
    private final String testSpecificationWrongDateFormat = """
            {
              "source_type": "siembol-logs",
              "max_result": 1000,
              "from_date": "2022-06-55",
              "to_date": "2022-06-05"
            }
            """;

    private final String testSpecificationWrongStartDate = """
            {
              "source_type": "siembol-logs",
              "max_result": 1000,
              "from_date": "2022-06-05",
              "to_date": "2022-06-01"
            }
            """;

    private final String rule = """
            {
              "rule_name": "siembol_alert_generic",
              "rule_version": 1,
              "rule_author": "dummy",
              "rule_description": "Test rule - is_alert is equal to true",
              "source_type": "*",
              "matchers": [
                {
                  "is_enabled": true,
                  "matcher_type": "REGEX_MATCH",
                  "is_negated": false,
                  "field": "source_type",
                  "data": "(?<sensor>.*)"
                },
                {
                  "is_enabled": true,
                  "matcher_type": "REGEX_MATCH",
                  "is_negated": false,
                  "field": "is_alert",
                  "data": "(?i)true"
                }
              ]
            }
            """;
    private AlertingSparkConfigTester configTester;
    private final String testSchema = "dummy schema";
    private SiembolJsonSchemaValidator testValidator;
    private AlertingSparkTestingProvider testingProvider;
    private SparkHdfsTesterProperties sparkHdfsTesterProperties;
    private String testResultOutput = "{}";
    ArgumentCaptor<String> argumentCaptor;

    @Before
    public void setUp() throws Exception {
        sparkHdfsTesterProperties = new SparkHdfsTesterProperties();
        sparkHdfsTesterProperties.setFolderPath("/folder");
        sparkHdfsTesterProperties.setFileExtension("snappy");
        var attributes = new HashMap<String, Object>();
        attributes.put("dummy_str", "dummy");
        sparkHdfsTesterProperties.setAttributes(attributes);
        testingProvider = Mockito.mock(AlertingSparkTestingProvider.class);
        testValidator = Mockito.mock(SiembolJsonSchemaValidator.class);
        argumentCaptor = ArgumentCaptor.forClass(String.class);

        Mockito.when(testingProvider.submitJob(argumentCaptor.capture())).thenReturn(testResultOutput);

        configTester = new AlertingSparkConfigTester(testValidator, testSchema,
                testingProvider, sparkHdfsTesterProperties);
    }

    @Test
    public void getFlags() {
        var flags = configTester.getFlags();
        Assert.assertTrue(flags.contains(ConfigTesterFlag.CONFIG_TESTING));
        Assert.assertTrue(flags.contains(ConfigTesterFlag.INCOMPLETE_RESULT));
    }

    @Test
    public void getTestSpecificationSchemaOK() {
        ConfigEditorResult ret = configTester.getTestSpecificationSchema();
        Assert.assertEquals(OK, ret.getStatusCode());
        Assert.assertNotNull(ret.getAttributes().getTestSchema());
        Assert.assertEquals(testSchema, ret.getAttributes().getTestSchema());
    }

    @Test
    public void getName() {
        var name = configTester.getName();
        Assert.assertEquals(AlertingSparkConfigTester.CONFIG_TESTER_NAME, name);
    }

    @Test
    public void testConfigurationOk() throws JsonProcessingException {
        var result = configTester.testConfiguration(rule, testSpecification);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertEquals(testResultOutput,
                result.getAttributes().getTestResultRawOutput());
        var argValue = new String(
                Base64.getDecoder().decode(argumentCaptor.getValue().getBytes()));
        AlertingSparkArgumentDto argument = ARGUMENT_READER.readValue(argValue);
        Assert.assertEquals(1000, argument.getMaxResultSize().intValue());
        Assert.assertEquals(5, argument.getFilesPaths().size());
        Assert.assertEquals("/folder/2022-06-01/*.snappy", argument.getFilesPaths().get(0));
        Assert.assertEquals("/folder/2022-06-02/*.snappy", argument.getFilesPaths().get(1));
        Assert.assertEquals("/folder/2022-06-03/*.snappy", argument.getFilesPaths().get(2));
        Assert.assertEquals("/folder/2022-06-04/*.snappy", argument.getFilesPaths().get(3));
        Assert.assertEquals("/folder/2022-06-05/*.snappy", argument.getFilesPaths().get(4));
        Assert.assertNotNull(argument.getRules());
    }

    @Test
    public void testConfigurationWrongSpecification() {
        var result = configTester.testConfiguration(rule, "INVALID");
        Assert.assertEquals(BAD_REQUEST, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getException());
    }

    @Test
    public void testConfigurationProviderError() throws Exception {
        Mockito.when(testingProvider.submitJob(argumentCaptor.capture()))
                .thenThrow(new IllegalStateException());

        var result = configTester.testConfiguration(rule, testSpecification);
        Assert.assertEquals(ERROR, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getException());
    }

    @Test
    public void testConfigurationWrongStartDate() {
        var result = configTester.testConfiguration(rule, testSpecificationWrongStartDate);
        Assert.assertEquals(BAD_REQUEST, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getException());
    }

    @Test
    public void testConfigurationWrongDateFormat() {
        var result = configTester.testConfiguration(rule, testSpecificationWrongDateFormat);
        Assert.assertEquals(BAD_REQUEST, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getException());
    }
}
