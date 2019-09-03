package uk.co.gresearch.nortem.nikita.storm;

import uk.co.gresearch.nortem.nikita.common.NikitaFields;
import uk.co.gresearch.nortem.nikita.common.NikitaTags;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;

public class NikitaAlert implements Serializable {
    private static final String MISSING_FIELDS_MSG = "Missing Nikita alert fields in alert: %s";
    private static final String MISSING_CORRELATION_KEY = "Missing correlation key field in alert: %s";

    public enum Flags implements Serializable {
        NIKITA_CORRELATION_ENGINE,
        CORRELATION_ALERT,
        VISIBLE_ALERT
    }

    private final String fullRuleName;
    private final Number maxHourMatches;
    private final Number maxDayMatches;
    private final String alertJson;
    private final EnumSet<Flags> flags;
    private final String correlationKey;

    public NikitaAlert(Map<String, Object> alert, String alertJson) {
        String correlationKey = null;
        flags = EnumSet.noneOf(Flags.class);
        if (isGeneratedByNikitaCorrelation(alert)) {
            fullRuleName = alert.get(NikitaFields.FULL_RULE_NAME.getNikitaCorrelationName()).toString();
            maxHourMatches = (Number)alert.get(NikitaFields.MAX_PER_HOUR_FIELD.getNikitaCorrelationName());
            maxDayMatches = (Number)alert.get(NikitaFields.MAX_PER_DAY_FIELD.getNikitaCorrelationName());

            flags.add(Flags.NIKITA_CORRELATION_ENGINE);
            flags.add(Flags.VISIBLE_ALERT);
        } else {
            fullRuleName = alert.get(NikitaFields.FULL_RULE_NAME.getNikitaName()).toString();
            maxHourMatches = (Number)alert.get(NikitaFields.MAX_PER_HOUR_FIELD.getNikitaName());
            maxDayMatches = (Number)alert.get(NikitaFields.MAX_PER_DAY_FIELD.getNikitaName());

            if (alert.containsKey(NikitaTags.CORRELATION_KEY_TAG_NAME.toString())) {
                flags.add(Flags.CORRELATION_ALERT);

                Object correlationVisibleTag = alert.get(NikitaTags.CORRELATION_ALERT_VISIBLE_TAG_NAME.toString());
                if (correlationVisibleTag instanceof String
                        && correlationVisibleTag.toString().equalsIgnoreCase(NikitaTags.TAG_TRUE_VALUE.toString())) {
                    this.flags.add(Flags.VISIBLE_ALERT);
                }

                if (!(alert.get(NikitaTags.CORRELATION_KEY_TAG_NAME.toString()) instanceof String)) {
                    throw new IllegalArgumentException(String.format(MISSING_CORRELATION_KEY, alert.toString()));
                }
                correlationKey = (String)alert.get(NikitaTags.CORRELATION_KEY_TAG_NAME.toString());
            } else {
                flags.add(Flags.VISIBLE_ALERT);
            }
        }

        this.alertJson = alertJson;
        if (fullRuleName == null
                || maxHourMatches == null
                || maxDayMatches == null
                || alertJson == null) {
            throw new IllegalArgumentException(String.format(MISSING_FIELDS_MSG, alert.toString()));
        }
        this.correlationKey = correlationKey;
    }

    public String getFullRuleName() {
        return fullRuleName;
    }

    public Number getMaxHourMatches() {
        return maxHourMatches;
    }

    public Number getMaxDayMatches() {
        return maxDayMatches;
    }

    public String getAlertJson() {
        return alertJson;
    }

    public boolean isCorrelationAlert() {
        return flags.contains(Flags.CORRELATION_ALERT);
    }

    public boolean isVisibleAlert() {
        return flags.contains(Flags.VISIBLE_ALERT);
    }

    public Optional<String> getCorrelationKey() {
        return Optional.ofNullable(correlationKey);
    }

    private boolean isGeneratedByNikitaCorrelation(Map<String, Object> alert) {
        return NikitaTags.NIKITA_CORRELATION_DETECTION_SOURCE_TAG_VALUE.toString().equals(
                alert.get(NikitaTags.DETECTION_SOURCE_TAG_NAME.toString()));
    }
}
