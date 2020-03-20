package uk.co.gresearch.nortem.response.evaluators.fixed;

import org.junit.Assert;
import org.junit.Test;
import uk.co.gresearch.nortem.response.common.RespondingResult;
import uk.co.gresearch.nortem.response.common.ResponseAlert;
import uk.co.gresearch.nortem.response.common.ResponseEvaluationResult;

import static uk.co.gresearch.nortem.response.common.ResponseEvaluationResult.MATCH;

public class FixedEvaluatorTest {
    private FixedEvaluator evaluator;
    private ResponseAlert alert = new ResponseAlert();

    @Test
    public void testFixedEvaluatorMatch() {
        evaluator = new FixedEvaluator(MATCH);
        RespondingResult result = evaluator.evaluate(alert);
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertEquals(MATCH, result.getAttributes().getResult());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertEquals(alert, result.getAttributes().getAlert());
    }

    @Test
    public void testFixedEvaluatorNoMatch() {
        evaluator = new FixedEvaluator(ResponseEvaluationResult.NO_MATCH);
        RespondingResult result = evaluator.evaluate(alert);
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertEquals(ResponseEvaluationResult.NO_MATCH, result.getAttributes().getResult());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertEquals(alert, result.getAttributes().getAlert());
    }

    @Test
    public void testFixedEvaluatorFiltered() {
        evaluator = new FixedEvaluator(ResponseEvaluationResult.FILTERED);
        RespondingResult result = evaluator.evaluate(alert);
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertEquals(ResponseEvaluationResult.FILTERED, result.getAttributes().getResult());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertEquals(alert, result.getAttributes().getAlert());
    }
}
