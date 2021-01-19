package uk.co.gresearch.siembol.configeditor.rest.authorisation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import uk.co.gresearch.siembol.common.authorisation.SiembolOauth2SecurityAdapter;
import uk.co.gresearch.siembol.configeditor.common.AuthorisationProvider;
import uk.co.gresearch.siembol.configeditor.rest.common.ConfigEditorAuthorisationProperties;

import uk.co.gresearch.siembol.configeditor.rest.common.UserInfoProvider;

@ConditionalOnProperty(prefix = "config-editor-auth", value = "type", havingValue = "oauth2")
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(ConfigEditorAuthorisationProperties.class)
public class Oauth2SecurityAdapter extends SiembolOauth2SecurityAdapter {
    private ConfigEditorAuthorisationProperties properties;

    public Oauth2SecurityAdapter(@Autowired ConfigEditorAuthorisationProperties properties) {
        super(properties.getOauth2());
        this.properties = properties;
    }

    @Bean
    UserInfoProvider userInfoProvider() {
        return new JwtUserInfoProvider();
    }

    @Bean
    AuthorisationProvider authorisationProvider() {
        return new GroupBasedAuthorisationProvider(properties.getAuthorisationGroups(),
                properties.getAuthorisationAdminGroups());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        userInfoProvider();
        authorisationProvider();
        super.configure(http);
    }
}
