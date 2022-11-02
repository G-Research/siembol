package uk.co.gresearch.siembol.alerts.protection;

import uk.co.gresearch.siembol.alerts.common.AlertingAttributes;
import uk.co.gresearch.siembol.alerts.common.AlertingResult;

import java.util.Map;
import java.util.HashMap;
/**
 * An object for counting rule matches using simple in memory counter
 *
 * <p>This class implements RuleProtectionSystem interface using simple in-memory counting.
 *
 *
 * @author  Marian Novotny
 * @see RuleProtectionSystem
 * @see SimpleCounter
 *
 */
public class RuleProtectionSystemImpl implements RuleProtectionSystem {
    private static final String UNKNOWN_RULE = "No matches of the rule %s";
    private final Map<String, SimpleCounter> ruleCounters = new HashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public AlertingResult incrementRuleMatches(String fullRuleName) {
        if (!ruleCounters.containsKey(fullRuleName)) {
            ruleCounters.put(fullRuleName, new SimpleCounter());
        }

        SimpleCounter counter = ruleCounters.get(fullRuleName);
        counter.updateAndIncrement();

        AlertingAttributes attr = new AlertingAttributes();
        attr.setHourlyMatches(counter.getHourlyMatches());
        attr.setDailyMatches(counter.getDailyMatches());
        return new AlertingResult(AlertingResult.StatusCode.OK, attr);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AlertingResult getRuleMatches(String fullRuleName) {
        if (!ruleCounters.containsKey(fullRuleName)) {
            return AlertingResult.fromErrorMessage(String.format(UNKNOWN_RULE, fullRuleName));
        }

        SimpleCounter counter = ruleCounters.get(fullRuleName);
        AlertingAttributes attr = new AlertingAttributes();
        attr.setHourlyMatches(counter.getHourlyMatches());
        attr.setDailyMatches(counter.getDailyMatches());
        return new AlertingResult(AlertingResult.StatusCode.OK, attr);
    }
}
