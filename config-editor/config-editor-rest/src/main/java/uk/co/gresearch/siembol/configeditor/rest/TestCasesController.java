package uk.co.gresearch.siembol.configeditor.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import uk.co.gresearch.siembol.configeditor.model.ConfigEditorAttributes;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.rest.common.ConfigEditorHelper;
import uk.co.gresearch.siembol.configeditor.testcase.TestCaseEvaluator;
import java.util.Optional;

@RestController
public class TestCasesController {
    private static final String MISSING_ATTRIBUTES = "missing testcase in files attributes";
    @Autowired
    private TestCaseEvaluator testCaseEvaluator;

    @CrossOrigin
    @GetMapping(value = "/api/v1/testcases/schema", produces = MediaType.APPLICATION_JSON_VALUE)
    public ConfigEditorResult getSchema() {
        return testCaseEvaluator.getSchema();
    }

    @CrossOrigin
    @PostMapping(value = "/api/v1/testcases/validate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorResult> validate(@RequestBody ConfigEditorAttributes attributes) {
        Optional<String> testCase = ConfigEditorHelper.getFileContent(attributes);
        if (!testCase.isPresent()) {
            return new ResponseEntity<>(ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.BAD_REQUEST,
                    MISSING_ATTRIBUTES),
                    HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(testCaseEvaluator.validate(testCase.get()), HttpStatus.OK);
    }

    @CrossOrigin
    @PostMapping(value = "/api/v1/testcases/evaluate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorResult> evaluate(@RequestBody ConfigEditorAttributes attributes) {
        Optional<String> testCase = ConfigEditorHelper.getFileContent(attributes);
        if (!testCase.isPresent()
                || attributes.getTestResultRawOutput() == null) {
            return new ResponseEntity<>(ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.BAD_REQUEST,
                    MISSING_ATTRIBUTES),
                    HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(testCaseEvaluator.evaluate(
                attributes.getTestResultRawOutput(), testCase.get()),
                HttpStatus.OK);
    }
}
