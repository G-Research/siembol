package uk.co.gresearch.siembol.alerts.protection;

import uk.co.gresearch.siembol.alerts.common.AlertingResult;

/**
 * An object for counting rule matches
 *
 * <p>This interface is used for counting rule matches and
 * providing hourly and daily matches of a rule.
 *
 * @author  Marian Novotny
 * @see RuleProtectionSystemImpl
 *
 */
public interface RuleProtectionSystem {
    /**
     * Increments rule matches and returns hourly and daily matches
     *
     * @param fullRuleName full rule name
     * @return alerting result with hourly and daily matches of the rule after incrementing
     * @see AlertingResult
     */
    AlertingResult incrementRuleMatches(String fullRuleName);

    /**
     * Returns hourly and daily matches
     *
     * @param fullRuleName full rule name
     * @return alerting result with hourly and daily matches of the rule
     * @see AlertingResult
     */
    AlertingResult getRuleMatches(String fullRuleName);
}
