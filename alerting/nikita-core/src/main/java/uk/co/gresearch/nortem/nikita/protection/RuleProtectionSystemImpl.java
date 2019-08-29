package uk.co.gresearch.nortem.nikita.protection;

import uk.co.gresearch.nortem.nikita.common.NikitaAttributes;
import uk.co.gresearch.nortem.nikita.common.NikitaResult;

import java.util.Map;
import java.util.HashMap;

public class RuleProtectionSystemImpl implements RuleProtectionSystem {
    private static final String UNKNOWN_RULE = "No matches of the rule %s";
    private final Map<String, SimpleCounter> ruleCounters = new HashMap<>();


    @Override
    public NikitaResult incrementRuleMatches(String fullRuleName) {
        if (!ruleCounters.containsKey(fullRuleName)) {
            ruleCounters.put(fullRuleName, new SimpleCounter());
        }

        SimpleCounter counter = ruleCounters.get(fullRuleName);
        counter.updateAndIncrement();

        NikitaAttributes attr = new NikitaAttributes();
        attr.setHourlyMatches(counter.getHourlyMatches());
        attr.setDailyMatches(counter.getDailyMatches());
        return new NikitaResult(NikitaResult.StatusCode.OK, attr);
    }

    @Override
    public NikitaResult getRuleMatches(String fullRuleName) {
        if (!ruleCounters.containsKey(fullRuleName)) {
            return NikitaResult.fromErrorMessage(String.format(UNKNOWN_RULE, fullRuleName));
        }

        SimpleCounter counter = ruleCounters.get(fullRuleName);
        NikitaAttributes attr = new NikitaAttributes();
        attr.setHourlyMatches(counter.getHourlyMatches());
        attr.setDailyMatches(counter.getDailyMatches());
        return new NikitaResult(NikitaResult.StatusCode.OK, attr);
    }
}
