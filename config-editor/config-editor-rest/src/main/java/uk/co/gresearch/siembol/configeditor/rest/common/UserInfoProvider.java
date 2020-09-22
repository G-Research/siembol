package uk.co.gresearch.siembol.configeditor.rest.common;

import org.springframework.security.core.Authentication;
import uk.co.gresearch.siembol.configeditor.common.UserInfo;

public interface UserInfoProvider {
    UserInfo getUserInfo(Authentication authentication);
}
