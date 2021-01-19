package uk.co.gresearch.siembol.configeditor.rest.authorisation;

import uk.co.gresearch.siembol.configeditor.common.AuthorisationProvider;
import uk.co.gresearch.siembol.configeditor.common.ServiceUserRole;
import uk.co.gresearch.siembol.configeditor.common.UserInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GroupBasedAuthorisationProvider implements AuthorisationProvider {
    private static final String MISSING_ARGUMENTS = "Missing arguments for authorisation";
    private final Map<String, Set<String>> authorisationUserGroups;
    private final Map<String, Set<String>> authorisationAdminGroups;

    public GroupBasedAuthorisationProvider(
            Map<String, List<String>> authorisationGroups,
            Map<String, List<String>> authorisationAdminGroups) {
        this.authorisationUserGroups = authorisationGroups.entrySet().stream()
                .collect(Collectors.toMap(x -> x.getKey(), x -> new HashSet<>(x.getValue())));
        this.authorisationAdminGroups = authorisationAdminGroups.entrySet().stream()
                .collect(Collectors.toMap(x -> x.getKey(), x -> new HashSet<>(x.getValue())));
    }

    @Override
    public AuthorisationResult getUserAuthorisation(UserInfo user, String serviceName) {
        if (user == null || user.getGroups() == null || serviceName == null || user.getServiceUserRole() == null) {
            throw new IllegalArgumentException(MISSING_ARGUMENTS);
        }

        Map<String, Set<String>> authorisationGroups = user.getServiceUserRole() == ServiceUserRole.SERVICE_ADMIN
                ? authorisationAdminGroups : authorisationUserGroups;

        if (!authorisationGroups.containsKey(serviceName)) {
            return AuthorisationResult.ALLOWED;
        }

        Set<String> userGroups = new HashSet<>(user.getGroups());
        Set<String> serviceGroups = new HashSet<>(authorisationGroups.get(serviceName));
        serviceGroups.retainAll(userGroups);
        return serviceGroups.isEmpty() ? AuthorisationResult.FORBIDDEN : AuthorisationResult.ALLOWED;
    }
}
