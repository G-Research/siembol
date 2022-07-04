package uk.co.gresearch.siembol.configeditor.service.alerts;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import uk.co.gresearch.siembol.alerts.common.AlertingAttributes;
import uk.co.gresearch.siembol.alerts.common.AlertingResult;
import uk.co.gresearch.siembol.alerts.compiler.AlertingCompiler;
import uk.co.gresearch.siembol.common.jsonschema.SiembolJsonSchemaValidator;
import uk.co.gresearch.siembol.common.model.testing.AlertingTestSpecificationDto;
import uk.co.gresearch.siembol.configeditor.common.ConfigTesterBase;
import uk.co.gresearch.siembol.configeditor.common.ConfigTesterFlag;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorAttributes;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.model.ErrorMessages;

import java.io.IOException;
import java.util.EnumSet;

import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.BAD_REQUEST;
import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.OK;

public class AlertingConfigTester extends ConfigTesterBase<AlertingCompiler> {
    private static final ObjectWriter ALERTING_ATTRIBUTES_WRITER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .writerFor(AlertingAttributes.class)
            .with(SerializationFeature.INDENT_OUTPUT);

    private static final ObjectReader TEST_SPECIFICATION_READER = new ObjectMapper()
            .readerFor(AlertingTestSpecificationDto.class);

    public AlertingConfigTester(SiembolJsonSchemaValidator testValidator,
                                String testSchema,
                                AlertingCompiler compiler) {
        super(testValidator, testSchema, compiler);
    }

    @Override
    public EnumSet<ConfigTesterFlag> getFlags() {
        return EnumSet.of(ConfigTesterFlag.CONFIG_TESTING,
                ConfigTesterFlag.RELEASE_TESTING,
                ConfigTesterFlag.TEST_CASE_TESTING);
    }

    @Override
    public ConfigEditorResult testConfiguration(String configuration, String testSpecification) {
        AlertingTestSpecificationDto specificationDto;
        try {
            specificationDto = TEST_SPECIFICATION_READER.readValue(testSpecification);
        } catch (IOException e) {
            return ConfigEditorResult.fromException(BAD_REQUEST, e);
        }
        return fromAlertingTestResult(testProvider.testRule(configuration, specificationDto.getEventContent()));
    }

    @Override
    public ConfigEditorResult testConfigurations(String configurations, String testSpecification) {
        AlertingTestSpecificationDto specificationDto;
        try {
            specificationDto = TEST_SPECIFICATION_READER.readValue(testSpecification);
        } catch (IOException e) {
            return ConfigEditorResult.fromException(BAD_REQUEST, e);
        }
        return fromAlertingTestResult(testProvider.testRules(configurations, specificationDto.getEventContent()));
    }

    private ConfigEditorResult fromAlertingTestResult(AlertingResult alertingResult) {
        ConfigEditorAttributes attr = new ConfigEditorAttributes();
        if (alertingResult.getStatusCode() != AlertingResult.StatusCode.OK) {
            attr.setMessage(alertingResult.getAttributes().getMessage());
            attr.setException(alertingResult.getAttributes().getException());
            return new ConfigEditorResult(BAD_REQUEST, attr);
        }

        if (alertingResult.getAttributes().getMessage() == null) {
            return ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.ERROR,
                    ErrorMessages.UNEXPECTED_TEST_RESULT.getMessage());
        }

        attr.setTestResultOutput(alertingResult.getAttributes().getMessage());
        AlertingAttributes alertingAttributes = new AlertingAttributes();
        alertingAttributes.setOutputEvents(alertingResult.getAttributes().getOutputEvents());
        alertingAttributes.setExceptionEvents(alertingResult.getAttributes().getExceptionEvents());
        try {
            String rawTestOutput = ALERTING_ATTRIBUTES_WRITER.writeValueAsString(alertingAttributes);
            attr.setTestResultRawOutput(rawTestOutput);
        } catch (JsonProcessingException e) {
            return ConfigEditorResult.fromException(e);
        }

        return new ConfigEditorResult(OK, attr);
    }
}
