package uk.co.gresearch.siembol.configeditor.rest;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import uk.co.gresearch.siembol.configeditor.common.ServiceUserRole;
import uk.co.gresearch.siembol.configeditor.common.UserInfo;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorAttributes;
import uk.co.gresearch.siembol.configeditor.rest.common.UserInfoProvider;
import uk.co.gresearch.siembol.configeditor.serviceaggregator.ServiceAggregator;

import static uk.co.gresearch.siembol.common.authorisation.SiembolAuthorisationProperties.SWAGGER_AUTH_SCHEMA;

@RestController
@SecurityRequirement(name = SWAGGER_AUTH_SCHEMA)
public class ConfigStoreController {
    @Autowired
    private ServiceAggregator serviceAggregator;
    @Autowired
    private UserInfoProvider userInfoProvider;

    @CrossOrigin
    @GetMapping(value = "/api/v1/{service}/configstore/configs",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorAttributes> getConfigs(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable("service") String service) {
        UserInfo user = userInfoProvider.getUserInfo(authentication);
        return serviceAggregator
                .getConfigStore(user, service)
                .getConfigs()
                .toResponseEntity();
    }

    @CrossOrigin
    @PostMapping(value = "/api/v1/{service}/configstore/configs",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorAttributes> addConfig(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable("service") String service,
            @RequestBody String rule) {
        UserInfo user = userInfoProvider.getUserInfo(authentication);
        return serviceAggregator
                .getConfigStore(user, service)
                .addConfig(user, rule)
                .toResponseEntity();
    }

    @CrossOrigin
    @PutMapping(value = "/api/v1/{service}/configstore/configs",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorAttributes> updateConfig(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable("service") String service,
            @RequestBody String rule) {
        UserInfo user = userInfoProvider.getUserInfo(authentication);
        return serviceAggregator
                .getConfigStore(user, service)
                .updateConfig(user, rule)
                .toResponseEntity();
    }

    @CrossOrigin
    @GetMapping(value = "/api/v1/{service}/configstore/testcases",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorAttributes> getTestCases(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable("service") String service) {
        UserInfo user = userInfoProvider.getUserInfo(authentication);
        return serviceAggregator
                .getConfigStore(user, service)
                .getTestCases()
                .toResponseEntity();
    }

    @CrossOrigin
    @PostMapping(value = "/api/v1/{service}/configstore/testcases",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorAttributes> addTestCase(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable("service") String service,
            @RequestBody String testCase) {
        UserInfo user = userInfoProvider.getUserInfo(authentication);
        return serviceAggregator
                .getConfigStore(user, service)
                .addTestCase(user, testCase)
                .toResponseEntity();
    }

    @CrossOrigin
    @PutMapping(value = "/api/v1/{service}/configstore/testcases",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorAttributes> updateTestCase(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable("service") String service,
            @RequestBody String testCase) {
        UserInfo user = userInfoProvider.getUserInfo(authentication);
        return serviceAggregator
                .getConfigStore(user, service)
                .updateTestCase(user, testCase)
                .toResponseEntity();
    }

    @CrossOrigin
    @GetMapping(value = "/api/v1/{service}/configstore/release",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorAttributes> getRelease(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable("service") String service) {
        UserInfo user = userInfoProvider.getUserInfo(authentication);
        return serviceAggregator
                .getConfigStore(user, service)
                .getConfigsRelease()
                .toResponseEntity();
    }

    @CrossOrigin
    @GetMapping(value = "/api/v1/{service}/configstore/release/status",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorAttributes> getReleaseStatus(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable("service") String service) {
        UserInfo user = userInfoProvider.getUserInfo(authentication);
        return serviceAggregator
                .getConfigStore(user, service)
                .getConfigsReleaseStatus()
                .toResponseEntity();
    }

    @CrossOrigin
    @PostMapping(value = "/api/v1/{service}/configstore/release",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorAttributes> submitRelease(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable("service") String service,
            @RequestBody String rule) {
        UserInfo user = userInfoProvider.getUserInfo(authentication);
        return serviceAggregator
                .getConfigStore(user, service)
                .submitConfigsRelease(user, rule)
                .toResponseEntity();
    }

    @CrossOrigin
    @GetMapping(value = "/api/v1/{service}/configstore/repositories",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorAttributes> getRepositories(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable("service") String service) {
        UserInfo user = userInfoProvider.getUserInfo(authentication);
        return serviceAggregator
                .getConfigStore(user, service)
                .getRepositories()
                .toResponseEntity();
    }

    @CrossOrigin
    @GetMapping(value = "/api/v1/{service}/configstore/adminconfig",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorAttributes> getAdminConfig(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable("service") String service) {
        UserInfo user = userInfoProvider.getUserInfo(authentication);
        user.setServiceUserRole(ServiceUserRole.SERVICE_ADMIN);
        return serviceAggregator
                .getConfigStore(user, service)
                .getAdminConfig()
                .toResponseEntity();
    }

    @CrossOrigin
    @GetMapping(value = "/api/v1/{service}/configstore/adminconfig/status",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorAttributes> getAdminConfigStatus(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable("service") String service) {
        UserInfo user = userInfoProvider.getUserInfo(authentication);
        user.setServiceUserRole(ServiceUserRole.SERVICE_ADMIN);
        return serviceAggregator
                .getConfigStore(user, service)
                .getAdminConfigStatus()
                .toResponseEntity();
    }

    @CrossOrigin
    @PostMapping(value = "/api/v1/{service}/configstore/adminconfig",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorAttributes> submitAdminConfig(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable("service") String service,
            @RequestBody String config) {
        UserInfo user = userInfoProvider.getUserInfo(authentication);
        user.setServiceUserRole(ServiceUserRole.SERVICE_ADMIN);
        return serviceAggregator
                .getConfigStore(user, service)
                .submitAdminConfig(user, config)
                .toResponseEntity();
    }

}