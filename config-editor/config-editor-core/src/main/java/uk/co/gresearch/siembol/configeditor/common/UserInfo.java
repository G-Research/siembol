package uk.co.gresearch.siembol.configeditor.common;

import java.util.ArrayList;
import java.util.List;
/**
 * An object that represents information about a user
 *
 * <p>This class represents information about user such as the name, email address and groups that the user belongs to.
 * Moreover, it includes the user role under which is trying to access Siembol services.
 *
 * @author  Marian Novotny
 */
public class UserInfo {
    private String userName;
    private String email;
    private List<String> groups = new ArrayList<>();
    private ServiceUserRole serviceUserRole = ServiceUserRole.SERVICE_USER;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public ServiceUserRole getServiceUserRole() {
        return serviceUserRole;
    }

    public void setServiceUserRole(ServiceUserRole serviceUserRole) {
        this.serviceUserRole = serviceUserRole;
    }
}
