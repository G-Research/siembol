package uk.co.gresearch.nortem.nikita.protection;

import uk.co.gresearch.nortem.nikita.common.NikitaResult;

public interface RuleProtectionSystem {
    NikitaResult incrementRuleMatches(String fullRuleName);
    NikitaResult getRuleMatches(String fullRuleName);
}
