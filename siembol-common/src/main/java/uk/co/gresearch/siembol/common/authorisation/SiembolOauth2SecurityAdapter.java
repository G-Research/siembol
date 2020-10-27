package uk.co.gresearch.siembol.common.authorisation;

import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.jwt.*;

import java.util.*;

import static uk.co.gresearch.siembol.common.authorisation.SiembolAuthorisationProperties.SWAGGER_AUTH_SCHEMA;

public class SiembolOauth2SecurityAdapter extends WebSecurityConfigurerAdapter {
    private ResourceServerOauth2Properties properties;

    public SiembolOauth2SecurityAdapter(ResourceServerOauth2Properties properties) {
        this.properties = properties;
    }

    @Bean
    public OpenAPI openAPI() {
        return Oauth2Helper.createSwaggerOpenAPI(properties, SWAGGER_AUTH_SCHEMA);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        openAPI();
        JwtDecoder decoder = Oauth2Helper.createJwtDecoder(properties);
        List<String> excludedPatterns = properties.getExcludedUrlPatterns();
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
