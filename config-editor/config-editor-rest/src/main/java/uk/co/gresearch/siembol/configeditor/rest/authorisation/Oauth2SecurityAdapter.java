package uk.co.gresearch.siembol.configeditor.rest.authorisation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import uk.co.gresearch.siembol.common.authorisation.SiembolOauth2SecurityAdapter;
import uk.co.gresearch.siembol.configeditor.rest.common.ConfigEditorAuthorisationProperties;

@ConditionalOnProperty(prefix = "config-editor-auth", value = "type", havingValue = "oauth2")
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(ConfigEditorAuthorisationProperties.class)
public class Oauth2SecurityAdapter extends SiembolOauth2SecurityAdapter {

    public Oauth2SecurityAdapter(@Autowired ConfigEditorAuthorisationProperties properties) {
        super(properties.getOauth2());
    }
}
