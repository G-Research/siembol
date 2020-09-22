package uk.co.gresearch.siembol.configeditor.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import uk.co.gresearch.siembol.configeditor.common.UserInfo;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.rest.common.UserInfoProvider;
import uk.co.gresearch.siembol.configeditor.serviceaggregator.ServiceAggregator;

import static uk.co.gresearch.siembol.configeditor.rest.common.ConfigEditorHelper.fromConfigEditorResult;

@RestController
public class ConfigStoreController {
    @Autowired
    private ServiceAggregator serviceAggregator;
    @Autowired
    private UserInfoProvider userInfoProvider;

    @CrossOrigin
    @GetMapping(value = "/api/v1/{service}/configstore/configs",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ConfigEditorResult getConfigs(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable("service") String service) {
        UserInfo user = userInfoProvider.getUserInfo(authentication);
        return serviceAggregator.getConfigStore(user, service).getConfigs();
    }

    @CrossOrigin
    @PostMapping(value = "/api/v1/{service}/configstore/configs",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorResult> addConfig(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable("service") String service,
            @RequestBody String rule) {
        UserInfo user = userInfoProvider.getUserInfo(authentication);
        return fromConfigEditorResult(
                serviceAggregator.getConfigStore(user, service).addConfig(user, rule));
    }

    @CrossOrigin
    @PutMapping(value = "/api/v1/{service}/configstore/configs",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorResult> updateConfig(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable("service") String service,
            @RequestBody String rule) {
        UserInfo user = userInfoProvider.getUserInfo(authentication);
        return fromConfigEditorResult(
                serviceAggregator.getConfigStore(user, service).updateConfig(user, rule));
    }

    @CrossOrigin
    @GetMapping(value = "/api/v1/{service}/configstore/testcases",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ConfigEditorResult getTestCases(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable("service") String service) {
        UserInfo user = userInfoProvider.getUserInfo(authentication);
        return serviceAggregator.getConfigStore(user, service).getTestCases();
    }

    @CrossOrigin
    @PostMapping(value = "/api/v1/{service}/configstore/testcases",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorResult> addTestCase(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable("service") String service,
            @RequestBody String testCase) {
        UserInfo user = userInfoProvider.getUserInfo(authentication);
        return fromConfigEditorResult(
                serviceAggregator.getConfigStore(user, service).addTestCase(user, testCase));
    }

    @CrossOrigin
    @PutMapping(value = "/api/v1/{service}/configstore/testcases",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorResult> updateTestCase(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable("service") String service,
            @RequestBody String testCase) {
        UserInfo user = userInfoProvider.getUserInfo(authentication);
        return fromConfigEditorResult(
                serviceAggregator.getConfigStore(user, service).updateTestCase(user, testCase));
    }

    @CrossOrigin
    @GetMapping(value = "/api/v1/{service}/configstore/release",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ConfigEditorResult getRelease(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable("service") String service) {
        UserInfo user = userInfoProvider.getUserInfo(authentication);
        return serviceAggregator.getConfigStore(user, service).getConfigsRelease();
    }

    @CrossOrigin
    @GetMapping(value = "/api/v1/{service}/configstore/release/status",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ConfigEditorResult getReleaseStatus(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable("service") String service) {
        UserInfo user = userInfoProvider.getUserInfo(authentication);
        return serviceAggregator.getConfigStore(user, service).getConfigsReleaseStatus();
    }

    @CrossOrigin
    @PostMapping(value = "/api/v1/{service}/configstore/release",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorResult> submitRelease(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable("service") String service,
            @RequestBody String rule) {
        UserInfo user = userInfoProvider.getUserInfo(authentication);
        return fromConfigEditorResult(
                serviceAggregator.getConfigStore(user, service).submitConfigsRelease(user, rule));
    }

    @CrossOrigin
    @GetMapping(value = "/api/v1/{service}/configstore/repositories",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ConfigEditorResult getRepositories(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable("service") String service) {
        UserInfo user = userInfoProvider.getUserInfo(authentication);
        return serviceAggregator.getConfigStore(user, service).getRepositories();
    }
}