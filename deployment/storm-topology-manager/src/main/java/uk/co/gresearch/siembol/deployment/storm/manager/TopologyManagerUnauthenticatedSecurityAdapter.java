package uk.co.gresearch.siembol.deployment.storm.manager;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import uk.co.gresearch.siembol.common.authorisation.SiembolUnauthenticatedSecurityAdapter;

@Configuration
@EnableWebSecurity
public class TopologyManagerUnauthenticatedSecurityAdapter extends SiembolUnauthenticatedSecurityAdapter {
}
