package uk.co.gresearch.siembol.response.evaluators.markdowntable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.gresearch.siembol.response.common.RespondingResult;
import uk.co.gresearch.siembol.response.common.ResponseAlert;
import uk.co.gresearch.siembol.response.common.ResponseEvaluationResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TableFormatterTest {
    private static final ObjectReader RESPONSE_ALERT_READER = new ObjectMapper()
            .readerFor(ResponseAlert.class);
    private final String testAlert = """
            	{ "is_alert" : true, "siembol:metadat:topic": "metasonport",   "syslog_hostname": "mail_logs_syslog:"}
            """;

    private final String testAlertWithArray = """
            	{"tmp_array" :[ {"f1" : "v11"}, {"f3": "v23", "f1" : "v21"}, {"f2" : "v31", "f3": "v33"} ] }
            """;

    private final String resultTable = """
            #### Test table
            |      Field Name       |    Field Value    |
            |:---------------------:|:-----------------:|
            |    syslog_hostname    | mail_logs_syslog: |
            | siembol:metadat:topic |    metasonport    |
            |       is_alert        |       true        |
             """;

    private final String resultEmptyTable = """
            #### Test table
            | Field Name | Field Value |
            |:----------:|:-----------:|
            """;


    private final String arrayResultTable = """
            #### Test table
            | f1  | f2  | f3  |
            |:---:|:---:|:---:|
            | v11 |     |     |
            | v21 |     | v23 |
            |     | v31 | v33 |
            """;

    private final String arrayResultTableFiltered = """
            #### Test table
            | f1  | f2  |
            |:---:|:---:|
            | v11 |     |
            | v21 |     |
            |     | v31 |
            """;

    private List<String> includingFields;
    private List<String> excludingFields;
    private TableFormatter.Builder builder;
    private TableFormatter formatter;
    private ResponseAlert responseAlert;
    private ResponseAlert responseAlertWithArray;
    private final String tableName = "Test table";
    private final String fieldName = "test_field";

    @Before
    public void setUp() throws IOException {
        includingFields = Arrays.asList(".*");
        excludingFields = new ArrayList<>();

        responseAlert = RESPONSE_ALERT_READER.readValue(testAlert);
        responseAlertWithArray = RESPONSE_ALERT_READER.readValue(testAlertWithArray);
        builder = new TableFormatter.Builder().tableName(tableName).fieldName(fieldName);
    }

    @Test
    public void formatJsonObjectAllFieldsOk() {
        formatter = builder.columnNames("Field Name", "Field Value").build();

        RespondingResult result = formatter.evaluate(responseAlert);

        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertEquals(ResponseEvaluationResult.MATCH, result.getAttributes().getResult());
        Assert.assertNotNull(result.getAttributes().getAlert());
        Assert.assertTrue(result.getAttributes().getAlert().get(fieldName) instanceof String);
        Assert.assertEquals(deleteInvisibleChars(resultTable),
                deleteInvisibleChars(result.getAttributes().getAlert().get(fieldName).toString()));
    }

    @Test
    public void formatJsonObjectFilteredOk() {
        excludingFields.add("is_alert");
        formatter = builder.columnNames("Field Name", "Field Value")
                .patternFilter(includingFields, excludingFields)
                .build();

        RespondingResult result = formatter.evaluate(responseAlert);

        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertEquals(ResponseEvaluationResult.MATCH, result.getAttributes().getResult());
        Assert.assertNotNull(result.getAttributes().getAlert());
        Assert.assertTrue(result.getAttributes().getAlert().get(fieldName) instanceof String);
        Assert.assertFalse(result.getAttributes().getAlert().get(fieldName).toString().contains("is_alert"));
    }

    @Test
    public void formatEmptyJsonObject() {
        excludingFields.add("is_alert");
        formatter = builder.columnNames("Field Name", "Field Value")
                .patternFilter(includingFields, excludingFields)
                .build();

        RespondingResult result = formatter.evaluate(new ResponseAlert());

        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertEquals(ResponseEvaluationResult.MATCH, result.getAttributes().getResult());
        Assert.assertNotNull(result.getAttributes().getAlert());
        Assert.assertTrue(result.getAttributes().getAlert().get(fieldName) instanceof String);
        Assert.assertEquals(deleteInvisibleChars(resultEmptyTable),
                deleteInvisibleChars(result.getAttributes().getAlert().get(fieldName).toString()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildFormatterMissingTableName() {
        formatter = new TableFormatter.Builder()
                .columnNames("Field Name", "Field Value")
                .fieldName(fieldName)
                .patternFilter(includingFields, excludingFields)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildFormatterMissingColumnNames() {
        formatter = new TableFormatter.Builder()
                .tableName("test")
                .fieldName(fieldName)
                .columnNames(null, "Field Value")
                .patternFilter(includingFields, excludingFields)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildFormatterMissingFilterFields() {
        formatter = new TableFormatter.Builder()
                .tableName("test")
                .fieldName(fieldName)
                .columnNames("Field Name", "Field Value")
                .patternFilter(null, excludingFields)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildFormatterMissingFieldName() {
        formatter = new TableFormatter.Builder()
                .tableName("test")
                .columnNames("Field Name", "Field Value")
                .patternFilter(includingFields, excludingFields)
                .build();
    }

    @Test
    public void formatFilteredJsonObject() {
        includingFields = Arrays.asList("unknown");
        formatter = builder.columnNames("Field Name", "Field Value")
                .patternFilter(includingFields, excludingFields)
                .build();

        formatter = builder.columnNames("Field Name", "Field Value")
                .patternFilter(includingFields, excludingFields)
                .build();

        RespondingResult result = formatter.evaluate(responseAlert);
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertEquals(ResponseEvaluationResult.MATCH, result.getAttributes().getResult());
        Assert.assertNotNull(result.getAttributes().getAlert());
        Assert.assertTrue(result.getAttributes().getAlert().get(fieldName) instanceof String);
        Assert.assertEquals(deleteInvisibleChars(resultEmptyTable),
                deleteInvisibleChars(result.getAttributes().getAlert().get(fieldName).toString()));
    }

    @Test
    public void testArrayAllFieldsOk() {
        formatter = builder.arrayFieldName("tmp_array").build();

        RespondingResult result = formatter.evaluate(responseAlertWithArray);
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertEquals(ResponseEvaluationResult.MATCH, result.getAttributes().getResult());
        Assert.assertNotNull(result.getAttributes().getAlert());
        Assert.assertTrue(result.getAttributes().getAlert().get(fieldName) instanceof String);
        Assert.assertEquals(deleteInvisibleChars(arrayResultTable),
                deleteInvisibleChars(result.getAttributes().getAlert().get(fieldName).toString()));
    }

    @Test
    public void testArrayEmpty() {
        formatter = builder.arrayFieldName("unknown").build();

        RespondingResult result = formatter.evaluate(responseAlertWithArray);
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertEquals(ResponseEvaluationResult.MATCH, result.getAttributes().getResult());
        Assert.assertNotNull(result.getAttributes().getAlert());
        Assert.assertTrue(result.getAttributes().getAlert().get(fieldName) instanceof String);
    }

    @Test
    public void testArrayFilterAllFieldsOk() {
        includingFields = Arrays.asList("unknown");
        formatter = builder.arrayFieldName("is_alert").build();

        RespondingResult result = formatter.evaluate(responseAlert);
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertEquals(ResponseEvaluationResult.MATCH, result.getAttributes().getResult());
        Assert.assertNotNull(result.getAttributes().getAlert());
        Assert.assertTrue(result.getAttributes().getAlert().get(fieldName) instanceof String);
    }

    @Test
    public void testArrayNonArrayField() {
        includingFields = Arrays.asList("unknown");
        formatter = builder
                .arrayFieldName("tmp_array")
                .patternFilter(includingFields, excludingFields)
                .build();

        RespondingResult result = formatter.evaluate(responseAlertWithArray);
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertEquals(ResponseEvaluationResult.MATCH, result.getAttributes().getResult());
        Assert.assertNotNull(result.getAttributes().getAlert());
        Assert.assertTrue(result.getAttributes().getAlert().get(fieldName) instanceof String);
    }

    @Test
    public void testArrayFilterOneFieldOk() {
        excludingFields.add("f3");
        formatter = builder.arrayFieldName("tmp_array")
                .patternFilter(includingFields, excludingFields)
                .build();

        RespondingResult result = formatter.evaluate(responseAlertWithArray);
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertEquals(ResponseEvaluationResult.MATCH, result.getAttributes().getResult());
        Assert.assertNotNull(result.getAttributes().getAlert());
        Assert.assertTrue(result.getAttributes().getAlert().get(fieldName) instanceof String);
        Assert.assertEquals(deleteInvisibleChars(arrayResultTableFiltered),
                deleteInvisibleChars(result.getAttributes().getAlert().get(fieldName).toString()));
    }

    private String deleteInvisibleChars(String str) {
        return str.replaceAll("\\p{C}", "");
    }
}