package uk.co.gresearch.siembol.configeditor.rest.authorisation;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.oauth2.jwt.Jwt;
import uk.co.gresearch.siembol.configeditor.common.UserInfo;

import java.util.*;

import static org.mockito.Mockito.when;

public class JwtUserInfoProviderTest {
    private Jwt principal;
    private JwtUserInfoProvider provider = new JwtUserInfoProvider();
    private Map<String, Object> claims;
    private String userName = "john";
    private String email = "john@secret";
    private List<String> groups = Arrays.asList("a", "b");

    @Before
    public void setUp() {
        principal = Mockito.mock(Jwt.class);
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
        provider.getUserInfo("invalid type");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullPrincipal() {
        provider.getUserInfo(null);
    }

    @Test
    public void testCorrectUserToken() {
        UserInfo user = provider.getUserInfo(principal);
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
        provider.getUserInfo(principal);
    }

    @Test
    public void testMissingGroupsInUserToken() {
        claims.remove("groups");
        UserInfo user = provider.getUserInfo(principal);
        Assert.assertTrue(user.getGroups().isEmpty());
    }
}
