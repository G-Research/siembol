package uk.co.gresearch.siembol.response.application.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.co.gresearch.siembol.response.common.RespondingResult;
import uk.co.gresearch.siembol.response.common.RespondingResultAttributes;
import uk.co.gresearch.siembol.response.compiler.RespondingCompiler;

@RestController
public class RespondingRulesController {
    @Autowired
    private final RespondingCompiler respondingCompiler;

    public RespondingRulesController(RespondingCompiler respondingCompiler) {
        this.respondingCompiler = respondingCompiler;
    }

    @GetMapping(value = "/api/v1/rules/testschema", produces = MediaType.APPLICATION_JSON_VALUE)
    public RespondingResult getRulesTestSchema() {
        return respondingCompiler.getTestSpecificationSchema();
    }

    @GetMapping(value = "/api/v1/rules/schema", produces = MediaType.APPLICATION_JSON_VALUE)
    public RespondingResult getRulesSchema() {
        return respondingCompiler.getSchema();
    }

    @PostMapping(value = "/api/v1/rules/validate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RespondingResult> validateRules(@RequestBody RespondingResultAttributes attributes) {
        return RespondingRulesController.fromRespondingResult(
                respondingCompiler.validateConfigurations(attributes.getJsonRules()));
    }

    @PostMapping(value = "/api/v1/rules/test", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RespondingResult> testRules(@RequestBody RespondingResultAttributes attributes) {
        return RespondingRulesController.fromRespondingResult(
                respondingCompiler.testConfigurations(attributes.getJsonRules(), attributes.getTestSpecification()));
    }

    private static ResponseEntity<RespondingResult> fromRespondingResult(RespondingResult result) {
        switch (result.getStatusCode()) {
            case OK:
                return new ResponseEntity<>(result, HttpStatus.CREATED);
            case ERROR:
                return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
