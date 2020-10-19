package uk.co.gresearch.siembol.configeditor.rest.authorisation;

import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.jwt.*;
import uk.co.gresearch.siembol.configeditor.common.AuthorisationProvider;
import uk.co.gresearch.siembol.configeditor.rest.common.ConfigEditorAuthorisationProperties;
import uk.co.gresearch.siembol.common.authorisation.Oauth2Helper;
import uk.co.gresearch.siembol.configeditor.rest.common.UserInfoProvider;
import java.util.*;

import static uk.co.gresearch.siembol.configeditor.rest.common.ConfigEditorHelper.SWAGGER_AUTH_SCHEMA;

@ConditionalOnProperty(prefix = "config-editor-auth", value = "type", havingValue = "oauth2")
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(ConfigEditorAuthorisationProperties.class)
public class Oauth2SecurityAdapter extends WebSecurityConfigurerAdapter {
    @Autowired
    private ConfigEditorAuthorisationProperties properties;

    @Bean
    UserInfoProvider userInfoProvider() {
        return new JwtUserInfoProvider();
    }

    @Bean
    AuthorisationProvider authorisationProvider() {
        return new GroupBasedAuthorisationProvider(properties.getAuthorisationGroups());
    }

    @Bean
    OpenAPI openAPI() {
        return Oauth2Helper.createSwaggerOpenAPI(properties.getOauth2(), SWAGGER_AUTH_SCHEMA);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        JwtDecoder decoder = Oauth2Helper.createJwtDecoder(properties.getOauth2());
        userInfoProvider();
        authorisationProvider();
        openAPI();

        List<String> excludedPatterns = properties.getOauth2().getExcludedUrlPatterns();
        http
                .cors()
                .and()
                .csrf().disable()
                .authorizeRequests()
                .antMatchers(excludedPatterns.toArray(new String[excludedPatterns.size()]))
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .oauth2ResourceServer()
                .jwt(x -> x.decoder(decoder));
    }
}
