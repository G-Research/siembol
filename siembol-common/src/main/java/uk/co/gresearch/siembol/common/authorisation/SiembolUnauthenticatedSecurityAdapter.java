package uk.co.gresearch.siembol.common.authorisation;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
/**
 * An object that implement disabled authentication in Spring Boot projects
 *
 * <p>This class extends WebSecurityConfigurerAdapter from Spring framework.
 * It permits all HTTP request in the project.
 *
 * @author  Marian Novotny
 */
public class SiembolUnauthenticatedSecurityAdapter extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable().authorizeRequests().anyRequest().permitAll();
    }
}
