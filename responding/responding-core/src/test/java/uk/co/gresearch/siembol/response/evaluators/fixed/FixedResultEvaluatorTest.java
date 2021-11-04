package uk.co.gresearch.siembol.response.evaluators.fixed;

import org.junit.Assert;
import org.junit.Test;
import uk.co.gresearch.siembol.response.common.RespondingResult;
import uk.co.gresearch.siembol.response.common.ResponseAlert;
import uk.co.gresearch.siembol.response.common.ResponseEvaluationResult;

import static uk.co.gresearch.siembol.response.common.ResponseEvaluationResult.MATCH;

public class FixedResultEvaluatorTest {
    private FixedResultEvaluator evaluator;
    private final ResponseAlert alert = new ResponseAlert();

    @Test
    public void testFixedEvaluatorMatch() {
        evaluator = new FixedResultEvaluator(MATCH);
        RespondingResult result = evaluator.evaluate(alert);
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertEquals(MATCH, result.getAttributes().getResult());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertEquals(alert, result.getAttributes().getAlert());
    }

    @Test
    public void testFixedEvaluatorNoMatch() {
        evaluator = new FixedResultEvaluator(ResponseEvaluationResult.NO_MATCH);
        RespondingResult result = evaluator.evaluate(alert);
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertEquals(ResponseEvaluationResult.NO_MATCH, result.getAttributes().getResult());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertEquals(alert, result.getAttributes().getAlert());
    }

    @Test
    public void testFixedEvaluatorFiltered() {
        evaluator = new FixedResultEvaluator(ResponseEvaluationResult.FILTERED);
        RespondingResult result = evaluator.evaluate(alert);
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertEquals(ResponseEvaluationResult.FILTERED, result.getAttributes().getResult());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertEquals(alert, result.getAttributes().getAlert());
    }
}
