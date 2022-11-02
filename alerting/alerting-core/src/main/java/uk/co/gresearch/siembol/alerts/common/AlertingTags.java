package uk.co.gresearch.siembol.alerts.common;
/**
 * An enum of matching results returned in AlertingResult after engine evaluation of an event.
 *
 * @author  Marian Novotny
 * @see #DETECTION_SOURCE_TAG_NAME
 * @see #DETECTION_SOURCE_TAG_VALUE
 * @see #CORRELATION_ENGINE_DETECTION_SOURCE_TAG_VALUE
 * @see #CORRELATION_KEY_TAG_NAME
 * @see #CORRELATION_ALERT_VISIBLE_TAG_NAME
 * @see #TAG_TRUE_VALUE
 *
 */
public enum AlertingTags {
    DETECTION_SOURCE_TAG_NAME("detection_source"),
    DETECTION_SOURCE_TAG_VALUE("siembol_alerts"),
    CORRELATION_ENGINE_DETECTION_SOURCE_TAG_VALUE("siembol_correlation_alerts"),
    CORRELATION_KEY_TAG_NAME("correlation_key"),
    CORRELATION_ALERT_VISIBLE_TAG_NAME("correlation_alert_visible"),
    TAG_TRUE_VALUE("true");

    private final String name;

    AlertingTags(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
