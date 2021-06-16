package uk.co.gresearch.siembol.configeditor.rest.authorisation;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.security.oauth2.jwt.Jwt;
import uk.co.gresearch.siembol.configeditor.common.UserInfo;
import uk.co.gresearch.siembol.configeditor.rest.common.UserInfoProvider;

public class JwtUserInfoProvider implements UserInfoProvider {
    private static final String WRONG_USER_INFO = "Wrong user info in Oauth2 token";

    @Override
    public UserInfo getUserInfo(Object principal) {
        if (!(principal instanceof Jwt)) {
            throw new IllegalArgumentException(WRONG_USER_INFO);
        }

        Jwt userToken = (Jwt)principal;
        UserInfo userInfo = new UserInfo();
        userInfo.setUserName(userToken.getSubject());
        try {
            BeanUtils.populate(userInfo, userToken.getClaims());
        } catch (Exception e) {
            throw new IllegalArgumentException(WRONG_USER_INFO);
        }

        if (userInfo.getUserName() == null || userInfo.getEmail() == null || userInfo.getGroups() == null) {
            throw new IllegalArgumentException(WRONG_USER_INFO);
        }
        return userInfo;
    }
}
