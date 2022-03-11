package uk.co.gresearch.siembol.common.metrics;

public enum SiembolMetrics {
    PARSING_CONFIGS_UPDATE("siembol_counter_parsing_configs_update"),
    PARSING_CONFIGS_UPDATE_VERSION("siembol_gauge_parsing_configs_update"),

    PARSING_SOURCE_TYPE_PARSED_MESSAGES("siembol_counter_%s_parsed_messages"),
    PARSING_SOURCE_TYPE_FILTERED_MESSAGES("siembol_counter_%s_filtered_messages"),
    PARSING_SOURCE_TYPE_ERROR_MESSAGES("siembol_counter_%s_error_messages"),

    PARSING_APP_PARSED_MESSAGES("siembol_counter_parsed_messages"),
    PARSING_APP_FILTERED_MESSAGES("siembol_counter_filtered_messages"),
    PARSING_APP_ERROR_MESSAGES("siembol_counter_error_messages"),

    ENRICHMENT_RULES_UPDATE("siembol_counter_enrichment_rules_update"),
    ENRICHMENT_RULES_UPDATE_VERSION("siembol_gauge_enrichment_rules_update"),
    ENRICHMENT_RULE_APPLIED("siembol_counter_enrichment_rule_%s_applied"),
    ENRICHMENT_TABLE_APPLIED("siembol_counter_enrichment_table_%s_applied"),
    ENRICHMENT_TABLE_UPDATED("siembol_counter_enrichment_table_%s_updated"),
    ENRICHMENT_TABLE_UPDATE_ERROR("siembol_counter_enrichment_table_%s_error_update"),

    ALERTING_RULE_MATCHED("siembol_counter_%s_rule_matches"),
    ALERTING_RULE_CORRELATION("siembol_counter_%s_rule_for_correlation_matches"),
    ALERTING_RULE_ERROR_MATCH("siembol_counter_%s_rule_error_match"),
    ALERTING_RULE_PROTECTION("siembol_counter_%s_rule_protection"),
    ALERTING_ENGINE_MATCHES("siembol_counter_rules_matches"),
    ALERTING_ENGINE_ERROR_MATCHES("siembol_counter_rules_error_matches"),
    ALERTING_ENGINE_CORRELATION("siembol_counter_rules_for_correlation_matches"),
    ALERTING_ENGINE_RULE_PROTECTION("siembol_counter_rules_protection_matches"),

    RESPONSE_RULE_MATCHED("siembol_%s_%s_rule_matched"),
    RESPONSE_RULE_ERROR_MATCH("siembol_%s_%s_rule_error_match"),
    RESPONSE_RULE_PROTECTION("siembol_%s_%s_rule_protection"),
    RESPONSE_ENGINE_MATCHES("siembol_%s_matches"),
    RESPONSE_ENGINE_ERROR_MATCHES("siembol_%s_error_matches"),

    TOPOLOGY_MANAGER_TOPOLOGY_KILLED("siembol_topology_%s_%s_killed"),
    TOPOLOGY_MANAGER_TOPOLOGY_RELEASED("siembol_topology_%s_%s_released"),

    SIEMBOL_SYNC_RELEASE("siembol_sync_%s_release"),
    SIEMBOL_SYNC_ADMIN_CONFIG("siembol_sync_%s_admin_config"),
    SIEMBOL_SYNC_RULES_VERSION("siembol_sync_%s_config_version"),
    CONFIG_EDITOR_REST_RELEASE_PR_SERVICE("siembol_topology_%s_%s_killed"),
    CONFIG_EDITOR_REST_ADMIN_CONFIG_PR_SERVICE("siembol_topology_%s_%s_killed");

    private final String formatStringName;


    SiembolMetrics(String formatStringName) {
        this.formatStringName = formatStringName;
    }

    public String getMetricName(String... args) {
        return String.format(formatStringName, args);
    }
}
