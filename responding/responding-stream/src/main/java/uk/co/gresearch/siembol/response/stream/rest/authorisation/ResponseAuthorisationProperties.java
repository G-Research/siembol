package uk.co.gresearch.siembol.response.stream.rest.authorisation;

import org.springframework.boot.context.properties.ConfigurationProperties;
import uk.co.gresearch.siembol.common.authorisation.SiembolAuthorisationProperties;

@ConfigurationProperties(prefix = "siembol-response-auth")
public class ResponseAuthorisationProperties extends SiembolAuthorisationProperties {
}
