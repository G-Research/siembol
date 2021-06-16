package uk.co.gresearch.siembol.configeditor.rest;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
            @AuthenticationPrincipal Object principal,
            @PathVariable("service") String service) {
        UserInfo user = userInfoProvider.getUserInfo(principal);
        return serviceAggregator
                .getConfigStore(user, service)
                .getConfigs()
                .toResponseEntity();
    }

    @CrossOrigin
    @PostMapping(value = "/api/v1/{service}/configstore/configs",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorAttributes> addConfig(
            @AuthenticationPrincipal Object principal,
            @PathVariable("service") String service,
            @RequestBody String rule) {
        UserInfo user = userInfoProvider.getUserInfo(principal);
        return serviceAggregator
                .getConfigStore(user, service)
                .addConfig(user, rule)
                .toResponseEntity();
    }

    @CrossOrigin
    @PostMapping(value = "/api/v1/{service}/configstore/configs/delete",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorAttributes> deleteConfig(
            @AuthenticationPrincipal Object principal,
            @PathVariable("service") String service,
            @RequestParam() String configName) {
        UserInfo user = userInfoProvider.getUserInfo(principal);
        return serviceAggregator
                .getConfigStore(user, service)
                .deleteConfig(user, configName)
                .toResponseEntity();
    }

    @CrossOrigin
    @PutMapping(value = "/api/v1/{service}/configstore/configs",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorAttributes> updateConfig(
            @AuthenticationPrincipal Object principal,
            @PathVariable("service") String service,
            @RequestBody String rule) {
        UserInfo user = userInfoProvider.getUserInfo(principal);
        return serviceAggregator
                .getConfigStore(user, service)
                .updateConfig(user, rule)
                .toResponseEntity();
    }

    @CrossOrigin
    @GetMapping(value = "/api/v1/{service}/configstore/testcases",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorAttributes> getTestCases(
            @AuthenticationPrincipal Object principal,
            @PathVariable("service") String service) {
        UserInfo user = userInfoProvider.getUserInfo(principal);
        return serviceAggregator
                .getConfigStore(user, service)
                .getTestCases()
                .toResponseEntity();
    }

    @CrossOrigin
    @PostMapping(value = "/api/v1/{service}/configstore/testcases",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorAttributes> addTestCase(
            @AuthenticationPrincipal Object principal,
            @PathVariable("service") String service,
            @RequestBody String testCase) {
        UserInfo user = userInfoProvider.getUserInfo(principal);
        return serviceAggregator
                .getConfigStore(user, service)
                .addTestCase(user, testCase)
                .toResponseEntity();
    }

    @CrossOrigin
    @PutMapping(value = "/api/v1/{service}/configstore/testcases",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorAttributes> updateTestCase(
            @AuthenticationPrincipal Object principal,
            @PathVariable("service") String service,
            @RequestBody String testCase) {
        UserInfo user = userInfoProvider.getUserInfo(principal);
        return serviceAggregator
                .getConfigStore(user, service)
                .updateTestCase(user, testCase)
                .toResponseEntity();
    }

    @CrossOrigin
    @PostMapping(value = "/api/v1/{service}/configstore/testcases/delete",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorAttributes> deleteTestCase(
            @AuthenticationPrincipal Object principal,
            @PathVariable("service") String service,
            @RequestParam() String configName,
            @RequestParam() String testCaseName) {
        UserInfo user = userInfoProvider.getUserInfo(principal);
        return serviceAggregator
                .getConfigStore(user, service)
                .deleteTestCase(user, configName, testCaseName)
                .toResponseEntity();
    }

    @CrossOrigin
    @GetMapping(value = "/api/v1/{service}/configstore/release",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorAttributes> getRelease(
            @AuthenticationPrincipal Object principal,
            @PathVariable("service") String service) {
        UserInfo user = userInfoProvider.getUserInfo(principal);
        return serviceAggregator
                .getConfigStore(user, service)
                .getConfigsRelease()
                .toResponseEntity();
    }

    @CrossOrigin
    @GetMapping(value = "/api/v1/{service}/configstore/release/status",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorAttributes> getReleaseStatus(
            @AuthenticationPrincipal Object principal,
            @PathVariable("service") String service) {
        UserInfo user = userInfoProvider.getUserInfo(principal);
        return serviceAggregator
                .getConfigStore(user, service)
                .getConfigsReleaseStatus()
                .toResponseEntity();
    }

    @CrossOrigin
    @PostMapping(value = "/api/v1/{service}/configstore/release",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorAttributes> submitRelease(
            @AuthenticationPrincipal Object principal,
            @PathVariable("service") String service,
            @RequestBody String rule) {
        UserInfo user = userInfoProvider.getUserInfo(principal);
        return serviceAggregator
                .getConfigStore(user, service)
                .submitConfigsRelease(user, rule)
                .toResponseEntity();
    }

    @CrossOrigin
    @GetMapping(value = "/api/v1/{service}/configstore/repositories",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorAttributes> getRepositories(
            @AuthenticationPrincipal Object principal,
            @PathVariable("service") String service) {
        UserInfo user = userInfoProvider.getUserInfo(principal);
        return serviceAggregator
                .getConfigStore(user, service)
                .getRepositories()
                .toResponseEntity();
    }

    @CrossOrigin
    @GetMapping(value = "/api/v1/{service}/configstore/adminconfig",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorAttributes> getAdminConfig(
            @AuthenticationPrincipal Object principal,
            @PathVariable("service") String service) {
        UserInfo user = userInfoProvider.getUserInfo(principal);
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
            @AuthenticationPrincipal Object principal,
            @PathVariable("service") String service) {
        UserInfo user = userInfoProvider.getUserInfo(principal);
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
            @AuthenticationPrincipal Object principal,
            @PathVariable("service") String service,
            @RequestBody String config) {
        UserInfo user = userInfoProvider.getUserInfo(principal);
        user.setServiceUserRole(ServiceUserRole.SERVICE_ADMIN);
        return serviceAggregator
                .getConfigStore(user, service)
                .submitAdminConfig(user, config)
                .toResponseEntity();
    }

}