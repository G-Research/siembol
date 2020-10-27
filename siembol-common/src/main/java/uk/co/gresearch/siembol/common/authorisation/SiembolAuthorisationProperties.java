package uk.co.gresearch.siembol.common.authorisation;

import org.springframework.boot.context.properties.NestedConfigurationProperty;
public class SiembolAuthorisationProperties {
    public static final String SWAGGER_AUTH_SCHEMA = "security_auth";

    private SiembolAuthorisationType type;
    @NestedConfigurationProperty
    private ResourceServerOauth2Properties oauth2;

    public SiembolAuthorisationType getType() {
        return type;
    }

    public void setType(SiembolAuthorisationType type) {
        this.type = type;
    }

    public ResourceServerOauth2Properties getOauth2() {
        return oauth2;
    }

    public void setOauth2(ResourceServerOauth2Properties oauth2) {
        this.oauth2 = oauth2;
    }
}
