package uk.co.gresearch.siembol.configeditor.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.co.gresearch.siembol.configeditor.common.UserInfo;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorAttributes;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.rest.common.UserInfoProvider;
import uk.co.gresearch.siembol.configeditor.serviceaggregator.ServiceAggregator;

@RestController
public class UserServicesController {
    @Autowired
    private ServiceAggregator serviceAggregator;
    @Autowired
    private UserInfoProvider userInfoProvider;

    @RequestMapping(value = "user", method = RequestMethod.GET)
    public ConfigEditorResult getLoggedInUser(@AuthenticationPrincipal Authentication authentication) {
        UserInfo user = userInfoProvider.getUserInfo(authentication);
        ConfigEditorAttributes attr = new ConfigEditorAttributes();
        attr.setUserName(user.getUserName());
        attr.setServices(serviceAggregator.getConfigEditorServices(user));
        return new ConfigEditorResult(ConfigEditorResult.StatusCode.OK, attr);
    }
}
