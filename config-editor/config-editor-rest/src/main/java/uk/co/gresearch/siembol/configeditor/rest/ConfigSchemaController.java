package uk.co.gresearch.siembol.configeditor.rest;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import uk.co.gresearch.siembol.configeditor.common.ConfigSchemaService;
import uk.co.gresearch.siembol.configeditor.common.UserInfo;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorAttributes;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.rest.common.UserInfoProvider;
import uk.co.gresearch.siembol.configeditor.serviceaggregator.ServiceAggregator;

import java.util.Optional;

import static uk.co.gresearch.siembol.common.authorisation.SiembolAuthorisationProperties.SWAGGER_AUTH_SCHEMA;
import static uk.co.gresearch.siembol.configeditor.rest.common.ConfigEditorHelper.getFileContent;

@RestController
@SecurityRequirement(name = SWAGGER_AUTH_SCHEMA)
public class ConfigSchemaController {
    private static final String MISSING_ATTRIBUTES = "missing required attributes for testing";
    @Autowired
    private ServiceAggregator serviceAggregator;
    @Autowired
    private UserInfoProvider userInfoProvider;

    @CrossOrigin
    @GetMapping(value = "/api/v1/{service}/configs/schema", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorAttributes> getSchema(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable("service") String serviceName) {
        UserInfo user = userInfoProvider.getUserInfo(authentication);
        return serviceAggregator
                .getConfigSchema(user, serviceName)
                .getSchema()
                .toResponseEntity();
    }

    @CrossOrigin
    @GetMapping(value = "/api/v1/{service}/configs/testschema", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorAttributes> getTestSchema(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable("service") String serviceName) {
        UserInfo user = userInfoProvider.getUserInfo(authentication);
        return serviceAggregator
                .getConfigSchema(user, serviceName)
                .getTestSchema()
                .toResponseEntity();
    }

    @CrossOrigin
    @PostMapping(value = "/api/v1/{service}/configs/validate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorAttributes> validate(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable("service") String serviceName,
            @RequestParam(required = false, defaultValue = "false") boolean singleConfig,
            @RequestBody String body) {
        UserInfo user = userInfoProvider.getUserInfo(authentication);
        ConfigSchemaService service = serviceAggregator.getConfigSchema(user, serviceName);
        return singleConfig
                ? service.validateConfiguration(body).toResponseEntity()
                : service.validateConfigurations(body).toResponseEntity();
    }


    @CrossOrigin
    @PostMapping(value = "/api/v1/{service}/configs/test", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorAttributes> test(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable("service") String serviceName,
            @RequestParam(required = false, defaultValue = "false") boolean singleConfig,
            @RequestBody ConfigEditorAttributes attributes) {

        Optional<String> config = getFileContent(attributes);
        if (!config.isPresent() || attributes.getTestSpecification() == null) {
            return ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.BAD_REQUEST, MISSING_ATTRIBUTES)
                    .toResponseEntity();
        }
        UserInfo user = userInfoProvider.getUserInfo(authentication);
        ConfigSchemaService service = serviceAggregator.getConfigSchema(user, serviceName);
        return singleConfig
                ? service.testConfiguration(config.get(), attributes.getTestSpecification()).toResponseEntity()
                : service.testConfigurations(config.get(), attributes.getTestSpecification()).toResponseEntity();
    }
}
