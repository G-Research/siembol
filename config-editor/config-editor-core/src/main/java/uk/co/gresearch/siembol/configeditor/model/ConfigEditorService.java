package uk.co.gresearch.siembol.configeditor.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.gresearch.siembol.configeditor.common.ServiceUserRole;

import java.util.List;
/**
 * A data transfer object that represents config editor services
 *
 * <p>This class represents config editor services. It provides the name, the type and possible user roles.
 *
 * @author  Marian Novotny
 * @see JsonProperty
 */
public class ConfigEditorService {
    private String name;
    private String type;
    @JsonProperty("user_roles")
    private List<ServiceUserRole> userRoles;
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<ServiceUserRole> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(List<ServiceUserRole> userRoles) {
        this.userRoles = userRoles;
    }
}
