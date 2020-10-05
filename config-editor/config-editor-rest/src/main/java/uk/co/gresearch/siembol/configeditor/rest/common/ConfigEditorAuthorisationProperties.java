package uk.co.gresearch.siembol.configeditor.rest.common;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import uk.co.gresearch.siembol.common.authorisation.ResourceServerOauth2Properties;
import uk.co.gresearch.siembol.configeditor.common.UserInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "config-editor-auth")
public class ConfigEditorAuthorisationProperties {
    private ConfigEditorAuthorisationType type;

    private static final String SINGLE_USER_DEFAULT_USER_NAME = "siembol";
    private static final String SINGLE_USER_DEFAULT_EMAIL = "siembol@siembol";
    @NestedConfigurationProperty
    private UserInfo singleUser;
    @NestedConfigurationProperty
    private ResourceServerOauth2Properties oauth2;

    public ConfigEditorAuthorisationProperties() {
        singleUser =  new UserInfo();
        singleUser.setUserName(SINGLE_USER_DEFAULT_USER_NAME);
        singleUser.setEmail(SINGLE_USER_DEFAULT_EMAIL);
    }

    private Map<String, List<String>> authorisationGroups = new HashMap<>();

    public Map<String, List<String>> getAuthorisationGroups() {
        return authorisationGroups;
    }

    public void setAuthorisationGroups(Map<String, List<String>> authorisationGroups) {
        this.authorisationGroups = authorisationGroups;
    }

    public UserInfo getSingleUser() {
        return singleUser;
    }

    public void setSingleUser(UserInfo singleUser) {
        this.singleUser = singleUser;
    }

    public ConfigEditorAuthorisationType getType() {
        return type;
    }

    public void setType(ConfigEditorAuthorisationType type) {
        this.type = type;
    }

    public ResourceServerOauth2Properties getOauth2() {
        return oauth2;
    }

    public void setOauth2(ResourceServerOauth2Properties oauth2) {
        this.oauth2 = oauth2;
    }
}
