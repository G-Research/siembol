package uk.co.gresearch.siembol.configeditor.rest.authorisation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import uk.co.gresearch.siembol.configeditor.common.AuthorisationProvider;
import uk.co.gresearch.siembol.configeditor.common.UserInfo;
import uk.co.gresearch.siembol.configeditor.rest.common.ConfigEditorAuthorisationProperties;
import uk.co.gresearch.siembol.configeditor.rest.common.UserInfoProvider;

@Configuration
@EnableConfigurationProperties(ConfigEditorAuthorisationProperties.class)
public class AuthorisationConfiguration {
    private static final String UNSUPPORTED_AUTH_TYPE = "Unsupported authorisation type: %s";

    private final UserInfoProvider userInfoProvider;
    private final AuthorisationProvider authorisationProvider;
    private final ConfigEditorAuthorisationProperties properties;

    public AuthorisationConfiguration(@Autowired ConfigEditorAuthorisationProperties properties) {
        this.properties = properties;
        switch (properties.getType()) {
            case DISABLED:
                this.userInfoProvider = disabledUserInfoProvider();
                this.authorisationProvider = disabledAuthorisationProvider();
                break;
            case OAUTH2:
                this.userInfoProvider = oauth2UserInfoProvider();
                this.authorisationProvider = oauth2AuthorisationProvider();
                break;
            default:
                throw new IllegalArgumentException(String.format(UNSUPPORTED_AUTH_TYPE, properties.getType()));
        }
    }

    @Bean
    public UserInfoProvider userInfoProvider() {
        return userInfoProvider;
    }

    @Bean
    public AuthorisationProvider authorisationProvider() {
        return authorisationProvider;
    }

    private UserInfoProvider oauth2UserInfoProvider() {
        return new JwtUserInfoProvider();
    }

    private AuthorisationProvider oauth2AuthorisationProvider() {
        return new GroupBasedAuthorisationProvider(properties.getAuthorisationGroups(),
                properties.getAuthorisationAdminGroups());
    }

    private UserInfoProvider disabledUserInfoProvider() {
        final UserInfo singleUser = properties.getSingleUser();
        return (principal) -> singleUser;
    }

    private AuthorisationProvider disabledAuthorisationProvider() {
        return (x, y) -> AuthorisationProvider.AuthorisationResult.ALLOWED;
    }
}
