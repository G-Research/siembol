package uk.co.gresearch.siembol.response.evaluators.timeexclusion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.gresearch.siembol.response.common.RespondingResult;
import uk.co.gresearch.siembol.response.common.ResponseAlert;
import uk.co.gresearch.siembol.response.model.TimeExclusionEvaluatorAttributesDto;

import java.io.IOException;

import static uk.co.gresearch.siembol.response.common.ResponseEvaluationResult.*;

public class TimeExclusionEvaluatorTest {
    private static final ObjectReader JSON_ATTRIBUTES_READER = new ObjectMapper()
            .readerFor(TimeExclusionEvaluatorAttributesDto.class);
    private final String attributes = """
            {
              "timestamp_field": "timestamp",
              "time_zone": "Europe/London",
              "months_of_year_pattern": ".*",
              "days_of_week_pattern": "3|6|7",
              "hours_of_day_pattern": "7|8|11"
            }
            """;

    private TimeExclusionEvaluator evaluator;
    private ResponseAlert alert = new ResponseAlert();

    private TimeExclusionEvaluatorAttributesDto attributesDto;

    @Before
    public void setUp() throws IOException {
        alert = new ResponseAlert();
        alert.put("host", "secret");
        alert.put("timestamp", 1652871240393L);

        attributesDto = JSON_ATTRIBUTES_READER.readValue(attributes);
        evaluator = new TimeExclusionEvaluator(attributesDto);
    }

    @Test
    public void filteredSimple() {
        RespondingResult result = evaluator.evaluate(alert);
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertEquals(FILTERED, result.getAttributes().getResult());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getAlert());
    }

    @Test
    public void filteredSubstitution() {
        attributesDto.setHoursOfDayPattern("${hours_pattern}");
        alert.put("hours_pattern", "7|8|11");
        evaluator = new TimeExclusionEvaluator(attributesDto);
        RespondingResult result = evaluator.evaluate(alert);
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertEquals(FILTERED, result.getAttributes().getResult());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getAlert());
    }

    @Test
    public void noMatchHoursSimple() {
        attributesDto.setHoursOfDayPattern("1|2|3");
        evaluator = new TimeExclusionEvaluator(attributesDto);
        RespondingResult result = evaluator.evaluate(alert);
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertEquals(NO_MATCH, result.getAttributes().getResult());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getAlert());
    }

    @Test
    public void noMatchHoursSubstitution() {
        attributesDto.setHoursOfDayPattern("${hours_pattern}");
        alert.put("hours_pattern", "1|2|3");
        evaluator = new TimeExclusionEvaluator(attributesDto);
        RespondingResult result = evaluator.evaluate(alert);
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertEquals(NO_MATCH, result.getAttributes().getResult());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getAlert());
    }

    @Test
    public void noMatchDaysSimple() {
        attributesDto.setDaysOfWeekPattern("1|2");
        evaluator = new TimeExclusionEvaluator(attributesDto);
        RespondingResult result = evaluator.evaluate(alert);
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertEquals(NO_MATCH, result.getAttributes().getResult());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getAlert());
    }

    @Test
    public void noMatchMonthsSimple() {
        attributesDto.setMonthsOfYearPattern("1|2");
        evaluator = new TimeExclusionEvaluator(attributesDto);
        RespondingResult result = evaluator.evaluate(alert);
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertEquals(NO_MATCH, result.getAttributes().getResult());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getAlert());
    }

    @Test
    public void missingTimestampError() {
        alert.remove("timestamp");
        RespondingResult result = evaluator.evaluate(alert);
        Assert.assertEquals(RespondingResult.StatusCode.ERROR, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }

    @Test
    public void errorMissingFields() {
        attributesDto.setMonthsOfYearPattern("");
        evaluator = new TimeExclusionEvaluator(attributesDto);
        RespondingResult result = evaluator.evaluate(alert);
        Assert.assertEquals(RespondingResult.StatusCode.ERROR, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }

    @Test
    public void errorSubstitutionMissingFields() {
        attributesDto.setMonthsOfYearPattern("${missing}");
        evaluator = new TimeExclusionEvaluator(attributesDto);
        RespondingResult result = evaluator.evaluate(alert);
        Assert.assertEquals(RespondingResult.StatusCode.ERROR, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }

    @Test
    public void errorSubstitutionInvalid() {
        attributesDto.setMonthsOfYearPattern("${invalid");
        evaluator = new TimeExclusionEvaluator(attributesDto);
        RespondingResult result = evaluator.evaluate(alert);
        Assert.assertEquals(RespondingResult.StatusCode.ERROR, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }

    @Test
    public void errorInvalidRegex() {
        attributesDto.setMonthsOfYearPattern("[");
        evaluator = new TimeExclusionEvaluator(attributesDto);
        RespondingResult result = evaluator.evaluate(alert);
        Assert.assertEquals(RespondingResult.StatusCode.ERROR, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }

    @Test
    public void errorInvalidTimezone() {
        attributesDto.setTimeZone("INVALID");
        evaluator = new TimeExclusionEvaluator(attributesDto);
        RespondingResult result = evaluator.evaluate(alert);
        Assert.assertEquals(RespondingResult.StatusCode.ERROR, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }
}
