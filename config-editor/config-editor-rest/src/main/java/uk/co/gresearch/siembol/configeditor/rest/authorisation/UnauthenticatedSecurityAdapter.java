package uk.co.gresearch.siembol.configeditor.rest.authorisation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import uk.co.gresearch.siembol.configeditor.common.AuthorisationProvider;
import uk.co.gresearch.siembol.configeditor.common.UserInfo;
import uk.co.gresearch.siembol.configeditor.rest.common.ConfigEditorAuthorisationProperties;
import uk.co.gresearch.siembol.configeditor.rest.common.UserInfoProvider;

@ConditionalOnProperty(prefix = "config-editor-auth", value = "disabled", havingValue = "true")
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(ConfigEditorAuthorisationProperties.class)
public class UnauthenticatedSecurityAdapter extends WebSecurityConfigurerAdapter {
    @Autowired
    private ConfigEditorAuthorisationProperties properties;

    @Bean
    UserInfoProvider userInfoProvider() {
        final UserInfo singleUser = properties.getSingleUser();
        return (x) -> singleUser;
    }

    @Bean
    AuthorisationProvider authorisationProvider() {
        return (x, y) -> AuthorisationProvider.AuthorisationResult.ALLOWED;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        userInfoProvider();
        authorisationProvider();
        http.authorizeRequests().anyRequest().permitAll();
    }
}
