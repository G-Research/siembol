package uk.co.gresearch.siembol.configeditor.service.alerts.spark;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import com.fasterxml.jackson.databind.ObjectWriter;
import uk.co.gresearch.siembol.alerts.compiler.AlertingCompiler;
import uk.co.gresearch.siembol.alerts.compiler.AlertingRulesCompiler;
import uk.co.gresearch.siembol.common.jsonschema.SiembolJsonSchemaValidator;
import uk.co.gresearch.siembol.common.model.testing.AlertingSparkArgumentDto;
import uk.co.gresearch.siembol.common.model.testing.AlertingSparkTestingSpecificationDto;
import uk.co.gresearch.siembol.configeditor.common.ConfigTesterBase;
import uk.co.gresearch.siembol.configeditor.common.ConfigTesterFlag;
import uk.co.gresearch.siembol.configeditor.model.SparkHdfsTesterProperties;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorAttributes;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.EnumSet;
import java.util.List;

import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.*;

public class AlertingSparkConfigTester  extends ConfigTesterBase<AlertingSparkTestingProvider> {
    public static final String CONFIG_TESTER_NAME = "spark_hdfs";

    private static final ObjectReader TEST_SPECIFICATION_READER = new ObjectMapper()
            .readerFor(AlertingSparkTestingSpecificationDto.class);
    private static final ObjectWriter ARGUMENT_WRITER = new ObjectMapper()
            .writerFor(AlertingSparkArgumentDto.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final String WRONG_DATE_MSG = "Wrong date format in test specification";
    private static final String PATH_FORMAT_MSG = "%s/%s/*.%s";
    private final SparkHdfsTesterProperties sparkHdfsTesterProperties;
    private final AlertingCompiler alertingCompiler;


    public  AlertingSparkConfigTester(SiembolJsonSchemaValidator testValidator,
                              String testSchema,
                              AlertingSparkTestingProvider compiler,
                              SparkHdfsTesterProperties sparkHdfsTesterProperties) throws Exception {
        super(testValidator, testSchema, compiler);
        this.sparkHdfsTesterProperties = sparkHdfsTesterProperties;
        this.alertingCompiler = AlertingRulesCompiler.createAlertingRulesCompiler();
    }

    @Override
    public ConfigEditorResult testConfiguration(String configuration, String testSpecificationStr) {
        String argBase64;
        var sparkAppArgument = new AlertingSparkArgumentDto();
        try {
            AlertingSparkTestingSpecificationDto testSpecification = TEST_SPECIFICATION_READER
                    .readValue(testSpecificationStr);
            sparkAppArgument.setRules(alertingCompiler.wrapRuleToRules(configuration));
            sparkAppArgument.setMaxResultSize(testSpecification.getMaxResult());
            var paths = getPaths(sparkHdfsTesterProperties.getFolderPath(),
                    sparkHdfsTesterProperties.getFileExtension(),
                    testSpecification.getFromDate(),
                    testSpecification.getToDate());
            sparkAppArgument.setFilesPaths(paths);
            String argString = ARGUMENT_WRITER.writeValueAsString(sparkAppArgument);
            argBase64 = Base64.getEncoder().encodeToString(argString.getBytes());
        } catch (Exception e) {
            return ConfigEditorResult.fromException(BAD_REQUEST, e);
        }

        try {
            String result = testProvider.submitJob(List.of(argBase64));
            var retAttributes = new ConfigEditorAttributes();
            retAttributes.setTestResultRawOutput(result);
            return new ConfigEditorResult(OK, retAttributes);
        } catch (Exception e) {
            return ConfigEditorResult.fromException(ERROR, e);
        }
    }

    private List<String> getPaths(String logPrefix, String suffix, String fromDate, String toDate) {
        List<String> paths = new ArrayList<>();
        LocalDate start = LocalDate.from(DATE_FORMATTER.parse(fromDate));
        LocalDate end = LocalDate.from(DATE_FORMATTER.parse(toDate));
        if (start.isAfter(end)) {
            throw new IllegalArgumentException(WRONG_DATE_MSG);
        }

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            paths.add(String.format(PATH_FORMAT_MSG, logPrefix, DATE_FORMATTER.format(date), suffix));
        }
        return paths;
    }

    @Override
    public EnumSet<ConfigTesterFlag> getFlags() {
        return EnumSet.of(
                ConfigTesterFlag.CONFIG_TESTING,
                ConfigTesterFlag.INCOMPLETE_RESULT);
    }

    @Override
    public String getName() {
        return CONFIG_TESTER_NAME;
    }
}
