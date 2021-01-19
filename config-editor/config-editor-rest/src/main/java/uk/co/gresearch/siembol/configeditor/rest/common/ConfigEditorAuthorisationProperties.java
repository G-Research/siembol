package uk.co.gresearch.siembol.configeditor.rest.common;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import uk.co.gresearch.siembol.common.authorisation.SiembolAuthorisationProperties;
import uk.co.gresearch.siembol.common.authorisation.SiembolAuthorisationType;
import uk.co.gresearch.siembol.common.authorisation.ResourceServerOauth2Properties;
import uk.co.gresearch.siembol.configeditor.common.UserInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "config-editor-auth")
public class ConfigEditorAuthorisationProperties extends SiembolAuthorisationProperties {

    private static final String SINGLE_USER_DEFAULT_USER_NAME = "siembol";
    private static final String SINGLE_USER_DEFAULT_EMAIL = "siembol@siembol";
    @NestedConfigurationProperty
    private UserInfo singleUser;

    public ConfigEditorAuthorisationProperties() {
        singleUser =  new UserInfo();
        singleUser.setUserName(SINGLE_USER_DEFAULT_USER_NAME);
        singleUser.setEmail(SINGLE_USER_DEFAULT_EMAIL);
    }

    private Map<String, List<String>> authorisationGroups = new HashMap<>();
    private Map<String, List<String>> authorisationAdminGroups = new HashMap<>();

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

    public Map<String, List<String>> getAuthorisationAdminGroups() {
        return authorisationAdminGroups;
    }

    public void setAuthorisationAdminGroups(Map<String, List<String>> authorisationAdminGroups) {
        this.authorisationAdminGroups = authorisationAdminGroups;
    }
}
