package uk.co.gresearch.siembol.configeditor.rest.common;

import uk.co.gresearch.siembol.configeditor.common.UserInfo;

public interface UserInfoProvider {
    UserInfo getUserInfo(Object principal);
}
