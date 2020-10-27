package uk.co.gresearch.siembol.response.stream.rest.authorisation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import uk.co.gresearch.siembol.common.authorisation.SiembolOauth2SecurityAdapter;

@ConditionalOnProperty(prefix = "siembol-response-auth", value = "type", havingValue = "oauth2")
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(ResponseAuthorisationProperties.class)
public class ResponseOauth2SecurityAdapter extends SiembolOauth2SecurityAdapter {
    public ResponseOauth2SecurityAdapter(@Autowired ResponseAuthorisationProperties properties) {
        super(properties.getOauth2());
    }
}
