package uk.co.gresearch.nortem.nikita.common;

public enum NikitaTags {
    DETECTION_SOURCE_TAG_NAME("detection:source"),
    NIKITA_DETECTION_SOURCE_TAG_VALUE("nikita"),
    NIKITA_CORRELATION_DETECTION_SOURCE_TAG_VALUE("nikita_correlation"),
    CORRELATION_KEY_TAG_NAME("correlation_key"),
    CORRELATION_ALERT_VISIBLE_TAG_NAME("correlation_alert_visible"),
    TAG_TRUE_VALUE("true");

    private final String name;

    NikitaTags(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
