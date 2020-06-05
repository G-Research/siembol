package uk.co.gresearch.siembol.response.application.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;
import uk.co.gresearch.siembol.response.application.ruleservice.RulesService;
import uk.co.gresearch.siembol.response.common.RespondingResult;
import uk.co.gresearch.siembol.response.common.RespondingResultAttributes;

@Component
public class ResponseInfoContributor implements InfoContributor {
    public static final String RULES_INFO_KEY = "rules_info";

    @Autowired
    private final RulesService streamService;

    public ResponseInfoContributor(RulesService streamService, BuildProperties buildProperties) {
        this.streamService = streamService;
    }

    @Override
    public void contribute(Info.Builder builder) {
        RespondingResult result = streamService.getRulesMetadata();
        RespondingResultAttributes infoAttributes = new RespondingResultAttributes();

        infoAttributes.setCompiledTime(result.getAttributes().getCompiledTime());
        infoAttributes.setRulesVersion(result.getAttributes().getRulesVersion());
        infoAttributes.setNumberOfRules(result.getAttributes().getNumberOfRules());

        builder.withDetail(RULES_INFO_KEY, infoAttributes).build();
    }
}

