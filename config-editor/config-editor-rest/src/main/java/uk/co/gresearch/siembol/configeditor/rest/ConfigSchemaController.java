package uk.co.gresearch.siembol.configeditor.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import uk.co.gresearch.siembol.configeditor.common.ConfigSchemaService;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorAttributes;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.serviceaggregator.ServiceAggregator;

import java.util.Optional;

import static uk.co.gresearch.siembol.configeditor.rest.ConfigEditorHelper.getFileContent;
import static uk.co.gresearch.siembol.configeditor.rest.ConfigEditorHelper.getUserNameFromAuthentication;
import static uk.co.gresearch.siembol.configeditor.rest.ConfigEditorHelper.wrapEventAsTestSpecification;

@RestController
public class ConfigSchemaController {
    private static final String MISSING_ATTRIBUTES = "missing required attributes for testing";
    @Autowired
    @Qualifier("serviceAggregator")
    private ServiceAggregator serviceAggregator;

    @CrossOrigin
    @GetMapping(value = "/api/v1/{service}/configs/schema", produces = MediaType.APPLICATION_JSON_VALUE)
    public ConfigEditorResult getSchema(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable("service") String serviceName) {
        return serviceAggregator
                .getConfigSchema(getUserNameFromAuthentication(authentication), serviceName)
                .getSchema();
    }

    @CrossOrigin
    @GetMapping(value = "/api/v1/{service}/configs/testschema", produces = MediaType.APPLICATION_JSON_VALUE)
    public ConfigEditorResult getTestSchema(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable("service") String serviceName) {
        return serviceAggregator
                .getConfigSchema(getUserNameFromAuthentication(authentication), serviceName)
                .getTestSchema();
    }

    @CrossOrigin
    @PostMapping(value = "/api/v1/{service}/configs/validate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorResult> validate(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable("service") String serviceName,
            @RequestParam(required = false, defaultValue = "false") boolean singleConfig,
            @RequestBody String body) {
        ConfigSchemaService service = serviceAggregator.getConfigSchema(getUserNameFromAuthentication(authentication),
                serviceName);
        return ConfigEditorHelper.fromConfigEditorResult(singleConfig
                ? service.validateConfiguration(body)
                : service.validateConfigurations(body));
    }

    @CrossOrigin
    @GetMapping(value = "/api/v1/{service}/configs/fields", produces = MediaType.APPLICATION_JSON_VALUE)
    public ConfigEditorResult getFields(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable("service") String serviceName) {
        return serviceAggregator.getConfigSchema(getUserNameFromAuthentication(authentication), serviceName).getFields();
    }

    @CrossOrigin
    @PostMapping(value = "/api/v1/{service}/configs/test", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorResult> test(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable("service") String serviceName,
            @RequestParam(required = false, defaultValue = "false") boolean singleConfig,
            @RequestBody ConfigEditorAttributes attributes) {

        Optional<String> config = getFileContent(attributes);
        if (!config.isPresent()
                || (attributes.getTestSpecification() == null && attributes.getEvent() == null)) {
            return new ResponseEntity<>(ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.BAD_REQUEST,
                    MISSING_ATTRIBUTES),
                    HttpStatus.BAD_REQUEST);
        }

        String testSpecification = attributes.getTestSpecification() != null
                ? attributes.getTestSpecification()
                : wrapEventAsTestSpecification(serviceName, attributes.getEvent()); //TODO: remove when UI will use test specification
        ConfigSchemaService service = serviceAggregator.getConfigSchema(
                getUserNameFromAuthentication(authentication), serviceName);
        return ConfigEditorHelper.fromConfigEditorResult((singleConfig
                ? service.testConfiguration(config.get(), testSpecification)
                : service.testConfigurations(config.get(), testSpecification)));
    }
}
