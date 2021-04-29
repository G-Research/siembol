package uk.co.gresearch.siembol.configeditor.rest.authorisation;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import uk.co.gresearch.siembol.configeditor.common.UserInfo;

import java.util.*;

import static org.mockito.Mockito.when;

public class JwtUserInfoProviderTest {
    private Authentication authentication;
    private Jwt principal;
    private JwtUserInfoProvider provider = new JwtUserInfoProvider();
    private Map<String, Object> claims;
    private String userName = "john";
    private String email = "john@secret";
    private List<String> groups = Arrays.asList("a", "b");

    @Before
    public void setUp() {
        authentication = Mockito.mock(Authentication.class);
        principal = Mockito.mock(Jwt.class);
        when(authentication.getPrincipal()).thenReturn(principal);
        claims = new HashMap<>();
        when(principal.getSubject()).thenReturn(userName);
        when(principal.getClaims()).thenReturn(claims);
        claims.put("email", email);
        claims.put("groups", groups);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullAuthentication() {
        provider.getUserInfo(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongPrincipalType() {
        when(authentication.getPrincipal()).thenReturn("string");
        provider.getUserInfo(authentication);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullPrincipal() {
        when(authentication.getPrincipal()).thenReturn(null);
        provider.getUserInfo(authentication);
    }

    @Test
    public void testCorrectUserToken() {
        UserInfo user = provider.getUserInfo(authentication);
        Assert.assertEquals(userName, user.getUserName());
        Assert.assertEquals(email, user.getEmail());
        Assert.assertEquals(groups.size(), user.getGroups().size());
        for (int i = 0; i < groups.size(); i++) {
            Assert.assertEquals(groups.get(i), user.getGroups().get(i));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingEmailInUserToken() {
        claims.remove("email");
        provider.getUserInfo(authentication);
    }

    @Test
    public void testMissingGroupsInUserToken() {
        claims.remove("groups");
        UserInfo user = provider.getUserInfo(authentication);
        Assert.assertTrue(user.getGroups().isEmpty());
    }
}
