package uk.co.gresearch.siembol.configeditor.service.alerts.spark;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import uk.co.gresearch.siembol.common.jsonschema.SiembolJsonSchemaValidator;
import uk.co.gresearch.siembol.configeditor.common.ConfigTesterFlag;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.model.SparkHdfsTesterProperties;

import java.util.HashMap;

public class AlertingSparkConfigTesterTest {
    private final String testSpecification = """
            {
              "source_type": "siembol-logs",
              "max_result": 1000,
              "from_date": "2022-06-1",
              "to_date": "2022-06-5"
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
    private String result = "result";
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

        Mockito.when(testingProvider.submitJob(argumentCaptor.capture())).thenReturn(result);

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
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertNotNull(ret.getAttributes().getTestSchema());
        Assert.assertEquals(testSchema, ret.getAttributes().getTestSchema());
    }

    @Test
    public void getName() {
        var name = configTester.getName();
        Assert.assertEquals(AlertingSparkConfigTester.CONFIG_TESTER_NAME, name);
    }
}
