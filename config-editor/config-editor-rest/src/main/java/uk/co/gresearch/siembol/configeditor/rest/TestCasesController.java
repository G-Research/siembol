package uk.co.gresearch.siembol.configeditor.rest;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import uk.co.gresearch.siembol.configeditor.model.ConfigEditorAttributes;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.rest.common.ConfigEditorHelper;
import uk.co.gresearch.siembol.configeditor.testcase.TestCaseEvaluator;
import java.util.Optional;

import static uk.co.gresearch.siembol.common.authorisation.SiembolAuthorisationProperties.SWAGGER_AUTH_SCHEMA;

@RestController
@SecurityRequirement(name = SWAGGER_AUTH_SCHEMA)
public class TestCasesController {
    private static final String MISSING_ATTRIBUTES = "missing testcase in files attributes";
    @Autowired
    private TestCaseEvaluator testCaseEvaluator;

    @CrossOrigin
    @GetMapping(value = "/api/v1/testcases/schema", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorAttributes> getSchema() {
        return testCaseEvaluator.getSchema().toResponseEntity();
    }

    @CrossOrigin
    @PostMapping(value = "/api/v1/testcases/validate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorAttributes> validate(@RequestBody ConfigEditorAttributes attributes) {
        Optional<String> testCase = ConfigEditorHelper.getFileContent(attributes);
        if (!testCase.isPresent()) {
            return ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.BAD_REQUEST, MISSING_ATTRIBUTES)
                    .toResponseEntity();
        }

        return testCaseEvaluator
                .validate(testCase.get())
                .toResponseEntity();
    }

    @CrossOrigin
    @PostMapping(value = "/api/v1/testcases/evaluate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorAttributes> evaluate(@RequestBody ConfigEditorAttributes attributes) {
        Optional<String> testCase = ConfigEditorHelper.getFileContent(attributes);
        if (!testCase.isPresent()
                || attributes.getTestResultRawOutput() == null) {
            return ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.BAD_REQUEST, MISSING_ATTRIBUTES)
                    .toResponseEntity();
        }

        return testCaseEvaluator
                .evaluate(attributes.getTestResultRawOutput(), testCase.get())
                .toResponseEntity();
    }
}
