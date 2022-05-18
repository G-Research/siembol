package uk.co.gresearch.siembol.response.evaluators.timeexclusion;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.gresearch.siembol.response.common.ProvidedEvaluators;
import uk.co.gresearch.siembol.response.common.RespondingResult;

public class TimeExclusionEvaluatorFactoryTest {
    private final String attributes = """
            {
              "timestamp_field": "timestamp",
              "time_zone": "Europe/London",
              "months_of_year_pattern": ".*",
              "days_of_week_pattern": "6|7",
              "hours_of_day_pattern": "7|8|9"
            }
            """;

    private TimeExclusionEvaluatorFactory factory;

    @Before
    public void setUp() throws Exception {
        factory = new TimeExclusionEvaluatorFactory();
    }

    @Test
    public void getType() {
        RespondingResult result = factory.getType();
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertEquals(ProvidedEvaluators.TIME_EXCLUSION_EVALUATOR.toString(),
                result.getAttributes().getEvaluatorType());
    }

    @Test
    public void getAttributesJsonSchema() {
        RespondingResult result = factory.getAttributesJsonSchema();
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getAttributesSchema());
    }

    @Test
    public void createInstance() {
        RespondingResult result = factory.createInstance(attributes);
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getRespondingEvaluator());
    }

    @Test
    public void validateAttributesOk() {
        RespondingResult result = factory.validateAttributes(attributes);
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
    }

    @Test
    public void validateAttributesInvalidJson() {
        RespondingResult result = factory.validateAttributes("INVALID");
        Assert.assertEquals(RespondingResult.StatusCode.ERROR, result.getStatusCode());
    }

    @Test
    public void validateAttributesInvalid() {
        RespondingResult result = factory.validateAttributes(
                attributes.replace("\"timestamp_field\"", "\"unsupported\""));
        Assert.assertEquals(RespondingResult.StatusCode.ERROR, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }
}
