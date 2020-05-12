package uk.co.gresearch.siembol.configeditor.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.serviceaggregator.ServiceAggregator;

import static uk.co.gresearch.siembol.configeditor.rest.ConfigEditorHelper.fromConfigEditorResult;
import static uk.co.gresearch.siembol.configeditor.rest.ConfigEditorHelper.getUserNameFromAuthentication;

@RestController
public class ConfigStoreController {
    @Autowired
    @Qualifier("serviceAggregator")
    private ServiceAggregator serviceAggregator;

    @CrossOrigin
    @GetMapping(value = "/api/v1/{service}/configstore/configs",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ConfigEditorResult getConfigs(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable("service") String service) {
        return serviceAggregator.getConfigStore(getUserNameFromAuthentication(authentication), service).getConfigs();
    }

    @CrossOrigin
    @PostMapping(value = "/api/v1/{service}/configstore/configs",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorResult> addConfig(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable("service") String service,
            @RequestBody String rule) {

        return fromConfigEditorResult(
                serviceAggregator.getConfigStore(getUserNameFromAuthentication(authentication), service)
                        .addConfig(authentication.getName(), rule));
    }

    @CrossOrigin
    @PutMapping(value = "/api/v1/{service}/configstore/configs",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorResult> updateConfig(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable("service") String service,
            @RequestBody String rule) {
        return fromConfigEditorResult(
                serviceAggregator.getConfigStore(getUserNameFromAuthentication(authentication), service)
                        .updateConfig(authentication.getName(), rule));
    }

    @CrossOrigin
    @GetMapping(value = "/api/v1/{service}/configstore/testcases",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ConfigEditorResult getTestCases(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable("service") String service) {
        return serviceAggregator.getConfigStore(getUserNameFromAuthentication(authentication), service).getTestCases();
    }

    @CrossOrigin
    @PostMapping(value = "/api/v1/{service}/configstore/testcases",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorResult> addTestCase(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable("service") String service,
            @RequestBody String testCase) {

        return fromConfigEditorResult(
                serviceAggregator.getConfigStore(getUserNameFromAuthentication(authentication), service)
                        .addTestCase(authentication.getName(), testCase));
    }

    @CrossOrigin
    @PutMapping(value = "/api/v1/{service}/configstore/testcases",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorResult> updateTestCase(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable("service") String service,
            @RequestBody String testCase) {
        return fromConfigEditorResult(
                serviceAggregator.getConfigStore(getUserNameFromAuthentication(authentication), service)
                        .updateTestCase(authentication.getName(), testCase));
    }

    @CrossOrigin
    @GetMapping(value = "/api/v1/{service}/configstore/release",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ConfigEditorResult getRelease(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable("service") String service) {
        return serviceAggregator.getConfigStore(getUserNameFromAuthentication(authentication), service)
                .getConfigsRelease();
    }

    @CrossOrigin
    @GetMapping(value = "/api/v1/{service}/configstore/release/status",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ConfigEditorResult getReleaseStatus(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable("service") String service) {
        return serviceAggregator.getConfigStore(getUserNameFromAuthentication(authentication), service)
                .getConfigsReleaseStatus();
    }

    @CrossOrigin
    @PostMapping(value = "/api/v1/{service}/configstore/release",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorResult> submitRelease(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable("service") String service,
            @RequestBody String rule) {
        return fromConfigEditorResult(
                serviceAggregator.getConfigStore(getUserNameFromAuthentication(authentication), service)
                        .submitConfigsRelease(authentication.getName(), rule));
    }

    @CrossOrigin
    @GetMapping(value = "/api/v1/{service}/configstore/repositories",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ConfigEditorResult getRepositories(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable("service") String service) {
        return serviceAggregator
                .getConfigStore(getUserNameFromAuthentication(authentication), service).getRepositories();
    }
}
