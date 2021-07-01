package uk.co.gresearch.siembol.configeditor.rest;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import uk.co.gresearch.siembol.configeditor.common.AuthorisationException;
import uk.co.gresearch.siembol.configeditor.common.AuthorisationProvider;
import uk.co.gresearch.siembol.configeditor.common.ServiceUserRole;
import uk.co.gresearch.siembol.configeditor.common.UserInfo;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorAttributes;
import uk.co.gresearch.siembol.configeditor.rest.common.ConfigEditorConfigurationProperties;
import uk.co.gresearch.siembol.configeditor.rest.common.UserInfoProvider;
import uk.co.gresearch.siembol.configeditor.sync.service.StormApplicationProvider;
import uk.co.gresearch.siembol.configeditor.sync.common.SynchronisationType;
import uk.co.gresearch.siembol.configeditor.sync.service.SynchronisationService;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static uk.co.gresearch.siembol.common.authorisation.SiembolAuthorisationProperties.SWAGGER_AUTH_SCHEMA;

@ConditionalOnProperty(prefix = "config-editor", value = "synchronisation")
@RestController
public class SynchronisationServiceController {
    private static final String WRONG_GITHUB_SIGNATURE = "Wrong github signature";
    private static final String AUTHORISATION_MSG = "The user is not authorised";
    private static final String ALL_SERVICES = "all";
    private static final String SIGNATURE_FORMAT_MSG = "sha1=%s";

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    @Autowired
    private AuthorisationProvider authorisationProvider;
    @Autowired
    private UserInfoProvider userInfoProvider;
    @Autowired
    private SynchronisationService synchronisationService;
    @Autowired
    private StormApplicationProvider stormApplicationProvider;
    @Autowired
    private ConfigEditorConfigurationProperties properties;

    @PostMapping(value = "/api/v1/sync/webhook", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorAttributes> synchronise(
            @AuthenticationPrincipal Object principal,
            @RequestParam List<String> serviceNames,
            @RequestParam String syncType,
            @RequestBody String body,
            @RequestHeader(required = false, value = "X-Hub-Signature") String signature) throws Exception {
        if (properties.getGitWebhookSecret() != null) {
            checkSignature(properties.getGitWebhookSecret(), body, signature);
        }

        boolean allServices = serviceNames.contains(ALL_SERVICES);
        SynchronisationType type = SynchronisationType.fromString(syncType).orElse(SynchronisationType.ALL);
        Callable<ResponseEntity<ConfigEditorAttributes>> task = () -> allServices
                ? synchronisationService.synchroniseAllServices(type).toResponseEntity()
                : synchronisationService.synchroniseServices(serviceNames, type).toResponseEntity();

        return executorService.submit(task).get();
    }

    @SecurityRequirement(name = SWAGGER_AUTH_SCHEMA)
    @GetMapping(value = "/api/v1/{service}/topologies", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorAttributes> getTopologies(
            @AuthenticationPrincipal Object principal,
            @PathVariable("service") String service) {
        checkAdminAuthorisation(principal, service);

        return stormApplicationProvider
                .getStormTopologies(service)
                .toResponseEntity();
    }

    @SecurityRequirement(name = SWAGGER_AUTH_SCHEMA)
    @PostMapping(value = "/api/v1/{service}/topologies/{topology}/restart", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfigEditorAttributes> restartTopology(
            @AuthenticationPrincipal Object principal,
            @PathVariable("service") String service,
            @PathVariable("topology") String topology) throws ExecutionException, InterruptedException {
        checkAdminAuthorisation(principal, service);
        Callable<ResponseEntity<ConfigEditorAttributes>> task = () -> stormApplicationProvider
                .restartStormTopology(service, topology)
                .toResponseEntity();

        return executorService.submit(task).get();
    }

    private void checkAdminAuthorisation(Object principal, String service) {
        UserInfo user = userInfoProvider.getUserInfo(principal);
        user.setServiceUserRole(ServiceUserRole.SERVICE_ADMIN);
        if (AuthorisationProvider.AuthorisationResult.ALLOWED
                != authorisationProvider.getUserAuthorisation(user, service)) {
            throw new AuthorisationException(AUTHORISATION_MSG);
        }
    }

    private void checkSignature(String secret, String body, String signature) {
        String digest = new HmacUtils(HmacAlgorithms.HMAC_SHA_1, secret.getBytes()).hmacHex(body.getBytes());
        String computed = String.format(SIGNATURE_FORMAT_MSG, digest);
        if (!computed.equals(signature)) {
            throw new AuthorisationException(WRONG_GITHUB_SIGNATURE);
        }
    }
}
