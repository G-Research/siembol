package uk.co.gresearch.siembol.common.authorisation;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
/**
 * A class with static helper methods for implementing OAUTH2
 *
 * <p>This class exposes static methods for implementing OAUTH2 in Siembol Spring boot projects.
 * These helper functions are used in all Siembol components.
 *
 * @author  Marian Novotny
 */
public class Oauth2Helper {
    private static final String MISSING_REQUIRED_AUDIENCE = "missing required audience";
    private static final int JWT_CLOCK_SKEW_IN_SECONDS = 30;
    private static final int JWKSET_TIMEOUT_IN_MILLI_SECONDS = 10000;

    public static JwtDecoder createJwtDecoder(ResourceServerOauth2Properties properties) throws MalformedURLException {
        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
        validators.add(new JwtTimestampValidator(Duration.ofSeconds(JWT_CLOCK_SKEW_IN_SECONDS)));

        validators.add(new JwtIssuerValidator(properties.getIssuerUrl()));

        validators.add(token -> token.getAudience().contains(properties.getAudience())
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error(MISSING_REQUIRED_AUDIENCE)));

        OAuth2TokenValidator<Jwt> jwtValidator = new DelegatingOAuth2TokenValidator<>(validators);
        JWKSource<SecurityContext> jwkSource = new RemoteJWKSet<>(new URL(properties.getJwkSetUrl()),
                new DefaultResourceRetriever(JWKSET_TIMEOUT_IN_MILLI_SECONDS, JWKSET_TIMEOUT_IN_MILLI_SECONDS));

        ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
        jwtProcessor.setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>(
                new JOSEObjectType(properties.getJwtType())));

        jwtProcessor.setJWSKeySelector(new JWSVerificationKeySelector<>(
                new JWSAlgorithm(properties.getJwsAlgorithm()), jwkSource));

        jwtProcessor.setJWTClaimsSetVerifier((claims, context) -> {});

        NimbusJwtDecoder jwtDecoder = new NimbusJwtDecoder(jwtProcessor);
        jwtDecoder.setJwtValidator(jwtValidator);
        return jwtDecoder;
    }

    public static OpenAPI createSwaggerOpenAPI(ResourceServerOauth2Properties properties, String schemaName) {
        Scopes scopes = new Scopes();
        properties.getScopes().forEach(x -> scopes.addString(x, ""));
        OAuthFlow flow = new OAuthFlow()
                .authorizationUrl(properties.getAuthorizationUrl())
                .tokenUrl(properties.getTokenUrl())
                .scopes(scopes);
        OAuthFlows flows = new OAuthFlows();
        flows.setAuthorizationCode(flow);
        return new OpenAPI().components(new Components()
                        .addSecuritySchemes(schemaName,
                                new SecurityScheme().type(SecurityScheme.Type.OAUTH2).flows(flows)));
    }
}
