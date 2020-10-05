package uk.co.gresearch.siembol.common.authorisation;

import java.util.ArrayList;
import java.util.List;

public class ResourceServerOauth2Properties {
    private List<String> excludedUrlPatterns = new ArrayList<>();
    private String audience;
    private String authorizationUrl;
    private String tokenUrl;
    private String issuerUrl;
    private String jwkSetUrl;
    private String jwsAlgorithm;
    private String jwtType;
    private List<String> scopes;
    public List<String> getExcludedUrlPatterns() {
        return excludedUrlPatterns;
    }

    public void setExcludedUrlPatterns(List<String> excludedUrlPatterns) {
        this.excludedUrlPatterns = excludedUrlPatterns;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public String getAuthorizationUrl() {
        return authorizationUrl;
    }

    public void setAuthorizationUrl(String authorizationUrl) {
        this.authorizationUrl = authorizationUrl;
    }

    public String getTokenUrl() {
        return tokenUrl;
    }

    public void setTokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
    }

    public String getIssuerUrl() {
        return issuerUrl;
    }

    public void setIssuerUrl(String issuerUrl) {
        this.issuerUrl = issuerUrl;
    }

    public String getJwkSetUrl() {
        return jwkSetUrl;
    }

    public void setJwkSetUrl(String jwkSetUrl) {
        this.jwkSetUrl = jwkSetUrl;
    }

    public String getJwsAlgorithm() {
        return jwsAlgorithm;
    }

    public void setJwsAlgorithm(String jwsAlgorithm) {
        this.jwsAlgorithm = jwsAlgorithm;
    }

    public String getJwtType() {
        return jwtType;
    }

    public void setJwtType(String jwtType) {
        this.jwtType = jwtType;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }
}
