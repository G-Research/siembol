package uk.co.gresearch.siembol.alerts.protection;

import uk.co.gresearch.siembol.alerts.common.AlertingResult;

public interface RuleProtectionSystem {
    AlertingResult incrementRuleMatches(String fullRuleName);
    AlertingResult getRuleMatches(String fullRuleName);
}
