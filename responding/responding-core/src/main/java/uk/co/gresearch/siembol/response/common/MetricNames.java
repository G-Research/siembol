package uk.co.gresearch.siembol.response.common;

public enum MetricNames {
    RULE_MATCHES("response_rule_matches", "Number of matches of the rule"),
    RULE_ERROR_MATCHES("response_rule_error_matches", "Number of error matches of the rule"),
    RULE_FILTERS("response_rule_filters", "Number of messages filtered by the rule"),
    ENGINE_PROCESSED_MESSAGES("response_engine_processed_alerts", "Number of messages processed by response engine"),
    ENGINE_FILTERED_MESSAGES("response_engine_filtered_alerts", "Number of messages filtered by response engine"),
    ENGINE_ERROR_MESSAGES("response_engine_errors", "Number of messages with error result by response engine"),
    ENGINE_NO_MATCH_MESSAGES("response_engine_no_matches", "Number of messages with not match result by response engine");

    private final String name;
    private final String description;
    MetricNames(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getNameWithSuffix(String suffix) {
        return String.format("%s_%s", name, suffix);
    }
}
