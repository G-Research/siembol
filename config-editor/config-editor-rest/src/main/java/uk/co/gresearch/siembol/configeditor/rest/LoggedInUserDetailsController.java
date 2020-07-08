package uk.co.gresearch.siembol.configeditor.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorAttributes;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.serviceaggregator.ServiceAggregator;

@RestController
public class LoggedInUserDetailsController {
    @Autowired
    private ServiceAggregator serviceAggregator;

    @RequestMapping(value = "user", method = RequestMethod.GET)
    public ConfigEditorResult getLoggedInUser(@AuthenticationPrincipal Authentication currentUser) {
        ConfigEditorAttributes attr = new ConfigEditorAttributes();
        attr.setUserName(currentUser.getName());
        attr.setServices(serviceAggregator.getConfigEditorServices(
                ConfigEditorHelper.getUserNameFromAuthentication(currentUser)));
        return new ConfigEditorResult(ConfigEditorResult.StatusCode.OK, attr);
    }
}
