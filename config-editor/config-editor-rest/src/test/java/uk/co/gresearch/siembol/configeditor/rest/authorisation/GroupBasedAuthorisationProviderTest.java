package uk.co.gresearch.siembol.configeditor.rest.authorisation;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.gresearch.siembol.configeditor.common.AuthorisationProvider;
import uk.co.gresearch.siembol.configeditor.common.ServiceUserRole;
import uk.co.gresearch.siembol.configeditor.common.UserInfo;

import java.util.*;

import static org.mockito.Mockito.when;

public class GroupBasedAuthorisationProviderTest {
    private UserInfo userInfo;
    private Map<String, List<String>> authorisationGroups;
    private Map<String, List<String>> authorisationAdminGroups;
    private GroupBasedAuthorisationProvider provider;
    private ServiceUserRole role;

    private List<String> userGroups;
    @Before
    public void setUp() {
        userInfo = Mockito.mock(UserInfo.class);
        userGroups = Arrays.asList("a", "b", "c", "e");
        when(userInfo.getGroups()).thenReturn(userGroups);

        role = ServiceUserRole.SERVICE_USER;
        when(userInfo.getServiceUserRole()).thenReturn(role);

        authorisationGroups = new HashMap<>();
        authorisationGroups.put("alert", Arrays.asList("b", "c", "d"));

        authorisationAdminGroups = new HashMap<>();
        authorisationAdminGroups.put("alert", Arrays.asList("c", "e"));
        provider = new GroupBasedAuthorisationProvider(authorisationGroups, authorisationAdminGroups);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullUserInfo() {
        AuthorisationProvider.AuthorisationResult result = provider.getUserAuthorisation(null, "alert");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullUserGroups() {
        when(userInfo.getGroups()).thenReturn(null);
        AuthorisationProvider.AuthorisationResult result = provider.getUserAuthorisation(userInfo, "alert");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullService() {
        AuthorisationProvider.AuthorisationResult result = provider.getUserAuthorisation(userInfo, null);
    }

    @Test
    public void testNoAuthGroupForServiceAllowed() {
        AuthorisationProvider.AuthorisationResult result = provider.getUserAuthorisation(userInfo, "parsing");
        Assert.assertEquals(AuthorisationProvider.AuthorisationResult.ALLOWED, result);
    }

    @Test
    public void testAuthGroupIntersectionForServiceAllowed() {
        AuthorisationProvider.AuthorisationResult result = provider.getUserAuthorisation(userInfo, "alert");
        Assert.assertEquals(AuthorisationProvider.AuthorisationResult.ALLOWED, result);
    }

    @Test
    public void testUserForAdminServiceForbidden() {
        when(userInfo.getGroups()).thenReturn(Arrays.asList("a", "b"));
        when(userInfo.getServiceUserRole()).thenReturn(ServiceUserRole.SERVICE_ADMIN);

        AuthorisationProvider.AuthorisationResult result = provider.getUserAuthorisation(userInfo, "alert");
        Assert.assertEquals(AuthorisationProvider.AuthorisationResult.FORBIDDEN, result);
    }

    @Test
    public void testUserForAdminServiceAllowed() {
        when(userInfo.getGroups()).thenReturn(Arrays.asList("c"));
        when(userInfo.getServiceUserRole()).thenReturn(ServiceUserRole.SERVICE_ADMIN);

        AuthorisationProvider.AuthorisationResult result = provider.getUserAuthorisation(userInfo, "alert");
        Assert.assertEquals(AuthorisationProvider.AuthorisationResult.ALLOWED, result);
    }

    @Test
    public void testAuthGroupNoIntersectionForServiceForbidden() {
        when(userInfo.getGroups()).thenReturn(Arrays.asList("x", "y"));
        AuthorisationProvider.AuthorisationResult result = provider.getUserAuthorisation(userInfo, "alert");
        Assert.assertEquals(AuthorisationProvider.AuthorisationResult.FORBIDDEN, result);
    }
}
