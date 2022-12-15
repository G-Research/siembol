package uk.co.gresearch.siembol.configeditor.testcase;

import uk.co.gresearch.siembol.configeditor.common.ServiceWithErrorMessage;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.model.ErrorMessages;
import uk.co.gresearch.siembol.configeditor.model.ErrorResolutions;
import uk.co.gresearch.siembol.configeditor.model.ErrorTitles;

import java.util.function.Supplier;
/**
 * An object for evaluating a test case with enhanced error messages
 *
 * <p>This class is implementing TestCaseEvaluator interface, and it extends ServiceWithErrorMessage class.
 * It is used for evaluating a test case using an underlying test case evaluator. It enriches error messages on error.
 *
 * @author  Marian Novotny
 * @see ServiceWithErrorMessage
 * @see TestCaseEvaluator
 * @see ErrorMessages
 * @see ErrorResolutions
 * @see ErrorTitles
 *
 */
public class TestCaseEvaluatorWithErrorMessage extends ServiceWithErrorMessage<TestCaseEvaluator>
        implements TestCaseEvaluator {
    public TestCaseEvaluatorWithErrorMessage(TestCaseEvaluator service) {
        super(service);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfigEditorResult evaluate(String jsonResult, String testCase) {
        Supplier<ConfigEditorResult> fun = () -> service.evaluate(jsonResult, testCase);
        return executeInternally(fun, ErrorTitles.TESTING_GENERIC.getTitle(),
                ErrorMessages.TESTING_GENERIC.getMessage(),
                ErrorResolutions.VALIDATION.getResolution());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfigEditorResult validate(String testCase) {
        Supplier<ConfigEditorResult> fun = () -> service.validate(testCase);
        return executeInternally(fun, ErrorTitles.VALIDATION_GENERIC.getTitle(),
                ErrorMessages.VALIDATION_GENERIC.getMessage(),
                ErrorResolutions.VALIDATION.getResolution());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfigEditorResult getSchema() {
        return service.getSchema();
    }
}
