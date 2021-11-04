package uk.co.gresearch.siembol.alerts.correlationengine;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.gresearch.siembol.alerts.common.AlertingFields;
import uk.co.gresearch.siembol.alerts.common.AlertingResult;

import java.util.*;

import static uk.co.gresearch.siembol.alerts.common.EvaluationResult.MATCH;
import static uk.co.gresearch.siembol.alerts.common.EvaluationResult.NO_MATCH;
import static uk.co.gresearch.siembol.alerts.common.AlertingResult.StatusCode.OK;
import static uk.co.gresearch.siembol.alerts.common.AlertingTags.CORRELATION_KEY_TAG_NAME;

public class CorrelationRuleTest {
    EnumSet<CorrelationRule.Flags> ruleFlags = EnumSet.noneOf(CorrelationRule.Flags.class);
    EnumSet< AlertCounterMetadata.Flags> counterFlags = EnumSet.noneOf(AlertCounterMetadata.Flags.class);
    EnumSet< AlertCounterMetadata.Flags> counterMandatoryFlags = EnumSet.of(AlertCounterMetadata.Flags.MANDATORY);
    private CorrelationRule rule;
    private CorrelationRule.Builder<CorrelationRule> builder;
    private final int timeWindowInMs = 10000;
    private final int maxTimeLagInSec = 5;
    private final String ruleName = "test_rule";
    private List<Map<String, Object>> alerts;
    private final String correlationKey = "1.2.3.4";


    @Before
    public void setUp() {
        builder = CorrelationRule.builder();
        builder
                .timeWindowInMs(timeWindowInMs)
                .maxLagTimeInSec(maxTimeLagInSec)
                .addAlertCounter("alert1", 1, counterFlags)
                .addAlertCounter("alert2", 2, counterFlags)
                .addAlertCounter("alert3", 3, counterFlags)
                .name(ruleName)
                .version(1);
    }

    @Test
    public void getAlertNames() {
        rule = builder.build();
        List<String> names = rule.getAlertNames();
        Assert.assertEquals(3, names.size());
        Assert.assertTrue(names.contains("alert1"));
        Assert.assertTrue(names.contains("alert2"));
        Assert.assertTrue(names.contains("alert3"));
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void addDuplicateAlertName() {
        builder.addAlertCounter("alert1", 1, counterFlags).build();
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void addNonPositiveAlertCounter() {
        builder.addAlertCounter("alert5", 0, counterFlags).build();
    }

    @Test
    public void matchProcessingTime() {
        rule = builder.build();
        alerts = createAlert(1, correlationKey, "alert1", 30000);
        for (Map<String, Object> alert : alerts) {
            AlertingResult ret = rule.match(alert);
            Assert.assertEquals(OK, ret.getStatusCode());
            Assert.assertEquals(NO_MATCH, ret.getAttributes().getEvaluationResult());
        }

        alerts = createAlert(2, correlationKey, "alert2", 30001);
        for (Map<String, Object> alert : alerts) {
            AlertingResult ret = rule.match(alert);
            Assert.assertEquals(OK, ret.getStatusCode());
            Assert.assertEquals(NO_MATCH, ret.getAttributes().getEvaluationResult());
        }

        alerts = createAlert(2, correlationKey, "alert3", 30002);
        for (Map<String, Object> alert : alerts) {
            AlertingResult ret = rule.match(alert);
            Assert.assertEquals(OK, ret.getStatusCode());
            Assert.assertEquals(NO_MATCH, ret.getAttributes().getEvaluationResult());
        }
        alerts = createAlert(1, correlationKey, "alert3", 30003);
        AlertingResult ret = rule.match(alerts.get(0));
        Assert.assertEquals(OK, ret.getStatusCode());
        Assert.assertEquals(MATCH, ret.getAttributes().getEvaluationResult());
        Assert.assertTrue(ret.getAttributes().getEvent().containsKey(
                AlertingFields.RULE_NAME.getCorrelationAlertingName()));
        Assert.assertEquals(ret.getAttributes().getEvent().get(AlertingFields.RULE_NAME.getCorrelationAlertingName()),
                ruleName);
        Assert.assertEquals(ret.getAttributes().getEvent().get(AlertingFields.FULL_RULE_NAME.getCorrelationAlertingName()),
                ruleName + "_v1");

        Assert.assertTrue(ret.getAttributes().getEvent().containsKey(
                AlertingFields.FULL_RULE_NAME.getCorrelationAlertingName()));

        alerts = createAlert(1, correlationKey, "alert3", 30003);

        ret = rule.match(alerts.get(0));
        Assert.assertEquals(OK, ret.getStatusCode());
        Assert.assertEquals(NO_MATCH, ret.getAttributes().getEvaluationResult());
    }

    @Test
    public void matchEventTimeTime() {
        ruleFlags = EnumSet.of(CorrelationRule.Flags.USE_EVENT_TIME);
        rule = builder.flags(ruleFlags).build();

        alerts = createAlert(1, correlationKey, "alert1", 30000);
        for (Map<String, Object> alert : alerts) {
            AlertingResult ret = rule.match(alert);
            Assert.assertEquals(OK, ret.getStatusCode());
            Assert.assertEquals(NO_MATCH, ret.getAttributes().getEvaluationResult());
        }

        alerts = createAlert(2, correlationKey, "alert2", 30001);
        for (Map<String, Object> alert : alerts) {
            AlertingResult ret = rule.match(alert);
            Assert.assertEquals(OK, ret.getStatusCode());
            Assert.assertEquals(NO_MATCH, ret.getAttributes().getEvaluationResult());
        }

        alerts = createAlert(2, correlationKey, "alert3", 30002);
        for (Map<String, Object> alert : alerts) {
            AlertingResult ret = rule.match(alert);
            Assert.assertEquals(OK, ret.getStatusCode());
            Assert.assertEquals(NO_MATCH, ret.getAttributes().getEvaluationResult());
        }
        alerts = createAlert(1, correlationKey, "alert3", 30003);
        AlertingResult ret = rule.match(alerts.get(0));
        Assert.assertEquals(OK, ret.getStatusCode());
        Assert.assertEquals(MATCH, ret.getAttributes().getEvaluationResult());
        alerts = createAlert(1, correlationKey, "alert3", 30003);

        ret = rule.match(alerts.get(0));
        Assert.assertEquals(OK, ret.getStatusCode());
        Assert.assertEquals(NO_MATCH, ret.getAttributes().getEvaluationResult());
    }

    @Test
    public void clean() {
        ruleFlags = EnumSet.of(CorrelationRule.Flags.USE_EVENT_TIME);
        rule = builder.flags(ruleFlags).alertsThresholds(1).build();

        alerts = createAlert(1, correlationKey, "alert2", 30001);
        AlertingResult ret = rule.match(alerts.get(0));
        Assert.assertEquals(OK, ret.getStatusCode());
        Assert.assertEquals(NO_MATCH, ret.getAttributes().getEvaluationResult());

        alerts = createAlert(2, correlationKey, "alert3", 30003);
        for (Map<String, Object> alert : alerts) {
            ret = rule.match(alert);
            Assert.assertEquals(OK, ret.getStatusCode());
            Assert.assertEquals(NO_MATCH, ret.getAttributes().getEvaluationResult());
        }

        rule.clean(30002 + 15000);
        alerts = createAlert(1, correlationKey, "alert2", 30004);
        ret = rule.match(alerts.get(0));
        Assert.assertEquals(OK, ret.getStatusCode());
        Assert.assertEquals(NO_MATCH, ret.getAttributes().getEvaluationResult());

        alerts = createAlert(1, correlationKey, "alert3", 30005);
        ret = rule.match(alerts.get(0));
        Assert.assertEquals(OK, ret.getStatusCode());
        Assert.assertEquals(MATCH, ret.getAttributes().getEvaluationResult());
        rule.clean(30006 + 15000);
    }

    @Test
    public void cleanAndRemove() {
        ruleFlags = EnumSet.of(CorrelationRule.Flags.USE_EVENT_TIME);
        rule = builder.flags(ruleFlags).alertsThresholds(1).build();

        for (int i = 1; i < 100; i++) {
            alerts = createAlert(2, correlationKey + i,
                    "alert3",
                    30000 + i);
            for (Map<String, Object> alert : alerts) {
                AlertingResult ret = rule.match(alert);
                Assert.assertEquals(OK, ret.getStatusCode());
                Assert.assertEquals(NO_MATCH, ret.getAttributes().getEvaluationResult());
            }
        }

        rule.clean(30050 + 15000);

    }

    @Test
    public void mandatoryCounter() {
        rule = builder
                .alertsThresholds(3)
                .addAlertCounter("alert4", 1, counterMandatoryFlags)
                .build();

        alerts = createAlert(1, correlationKey, "alert1", 30000);
        for (Map<String, Object> alert : alerts) {
            AlertingResult ret = rule.match(alert);
            Assert.assertEquals(OK, ret.getStatusCode());
            Assert.assertEquals(NO_MATCH, ret.getAttributes().getEvaluationResult());
        }

        alerts = createAlert(2, correlationKey, "alert2", 30001);
        for (Map<String, Object> alert : alerts) {
            AlertingResult ret = rule.match(alert);
            Assert.assertEquals(OK, ret.getStatusCode());
            Assert.assertEquals(NO_MATCH, ret.getAttributes().getEvaluationResult());
        }

        alerts = createAlert(3, correlationKey, "alert3", 30002);
        for (Map<String, Object> alert : alerts) {
            AlertingResult ret = rule.match(alert);
            Assert.assertEquals(OK, ret.getStatusCode());
            Assert.assertEquals(NO_MATCH, ret.getAttributes().getEvaluationResult());
        }

        alerts = createAlert(1, correlationKey, "alert4", 30005);
        AlertingResult ret = rule.match(alerts.get(0));
        Assert.assertEquals(OK, ret.getStatusCode());
        Assert.assertEquals(MATCH, ret.getAttributes().getEvaluationResult());
    }


    private List<Map<String, Object>> createAlert(int numbers, String key, String alertName, long processingTime) {
        Map<String, Object> alert = new HashMap<>();
        alert.put(AlertingFields.RULE_NAME.getAlertingName(), alertName);
        alert.put(CORRELATION_KEY_TAG_NAME.toString(), key);
        alert.put(AlertingFields.PROCESSING_TIME.getCorrelationAlertingName(), processingTime);
        alert.put("timestamp", processingTime);
        return new ArrayList<>(Collections.nCopies(numbers, alert));
    }



}
