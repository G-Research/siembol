package uk.co.gresearch.siembol.common.metrics;

public enum SiembolMetrics {
    PARSING_CONFIGS_UPDATE("siembol_counter_parsing_configs_update"),
    PARSING_CONFIGS_ERROR_UPDATE("siembol_counter_parsing_configs_error_update"),
    PARSING_CONFIGS_UPDATE_VERSION("siembol_gauge_parsing_configs_update"),

    PARSING_SOURCE_TYPE_PARSED_MESSAGES("siembol_counter_parsed_messages_%s"),
    PARSING_SOURCE_TYPE_FILTERED_MESSAGES("siembol_counter_filtered_messages_%s"),
    PARSING_SOURCE_TYPE_REMOVED_FIELDS_MESSAGES("siembol_counter_removed_fields_messages_%s"),
    PARSING_SOURCE_TYPE_TRUNCATED_FIELDS_MESSAGES("siembol_counter_truncated_fields_messages_%s"),
    PARSING_SOURCE_TYPE_TRUNCATED_ORIGINAL_STRING_MESSAGES(
            "siembol_counter_truncated_original_string_messages_%s"),
    PARSING_SOURCE_TYPE_SENT_ORIGINAL_STRING_MESSAGES(
            "siembol_counter_sent_original_string_messages_%s"),
    PARSING_APP_PARSED_MESSAGES("siembol_counter_app_parsed_messages"),
    PARSING_APP_FILTERED_MESSAGES("siembol_counter_app_filtered_messages"),
    PARSING_APP_ERROR_MESSAGES("siembol_counter_app_error_messages"),

    ENRICHMENT_RULES_UPDATE("siembol_counter_enrichment_rules_update"),
    ENRICHMENT_RULES_ERROR_UPDATE("siembol_counter_enrichment_rules_error_update"),
    ENRICHMENT_RULES_UPDATE_VERSION("siembol_gauge_enrichment_rules_update"),
    ENRICHMENT_RULE_APPLIED("siembol_counter_enrichment_rule_applied_%s"),
    ENRICHMENT_TABLE_APPLIED("siembol_counter_enrichment_table_applied_%s"),
    ENRICHMENT_TABLE_UPDATED("siembol_counter_enrichment_table_updated_%s"),
    ENRICHMENT_TABLE_UPDATE_ERROR("siembol_counter_enrichment_table_error_update_%s"),

    ALERTING_RULES_UPDATE("siembol_counter_alerting_rules_update"),
    ALERTING_RULES_ERROR_UPDATE("siembol_counter_alerting_rules_error_update"),
    ALERTING_RULES_UPDATE_VERSION("siembol_gauge_alerting_rules_update"),
    ALERTING_RULE_MATCHES("siembol_counter_alerting_rule_matches_%s"),
    ALERTING_RULE_CORRELATION_MATCHES("siembol_counter_alerting_rule_for_correlation_matches_%s"),
    ALERTING_RULE_ERROR_MATCHES("siembol_counter_alerting_rule_error_match_%s"),
    ALERTING_RULE_PROTECTION("siembol_counter_alerting_rule_protection_%s"),
    ALERTING_ENGINE_MATCHES("siembol_counter_alerting_engine_rules_matches"),
    ALERTING_ENGINE_ERROR_MATCHES("siembol_counter_alerting_engine_error_matches"),
    ALERTING_ENGINE_CORRELATION_MATCHES("siembol_counter_alerting_engine_rules_for_correlation_matches"),
    ALERTING_ENGINE_RULE_PROTECTION("siembol_counter_alerting_engine_rules_protection_matches"),

    RESPONSE_RULE_MATCHES("siembol_counter_response_rule_matches_%s"),
    RESPONSE_RULE_ERROR_MATCHES("siembol_counter_response_rule_error_matches_%s"),
    RESPONSE_RULE_FILTERED_ALERTS("siembol_counter_response_rule_filters_%s"),
    RESPONSE_ENGINE_PROCESSED_ALERTS("siembol_counter_response_engine_processed_alerts"),
    RESPONSE_ENGINE_FILTERED_ALERTS("siembol_counter_response_engine_filtered_alerts"),
    RESPONSE_ENGINE_ERRORS("siembol_counter_response_engine_errors"),
    RESPONSE_ENGINE_NO_MATCHES("siembol_counter_response_engine_no_matches"),
    RESPONSE_RULES_UPDATE("siembol_counter_response_rules_update"),
    RESPONSE_RULES_ERROR_UPDATE("siembol_counter_response_rules_error_update"),

    TOPOLOGY_MANAGER_TOPOLOGY_KILLED("siembol_counter_topology_killed_%s_%s"),
    TOPOLOGY_MANAGER_TOPOLOGY_RELEASED("siembol_counter_topology_released_%s_%s"),

    SIEMBOL_SYNC_RELEASE("siembol_counter_sync_release_%s"),
    SIEMBOL_SYNC_ADMIN_CONFIG("siembol_counter_sync_admin_config_%s"),
    SIEMBOL_SYNC_RULES_VERSION("siembol_counter_sync_config_version_%s"),
    CONFIG_EDITOR_REST_RELEASE_PR_SERVICE("siembol_counter_release_pr_%s"),
    CONFIG_EDITOR_REST_ADMIN_CONFIG_PR_SERVICE("siembol_counter_admin_counfig_pr_%s"),

    HEARTBEAT_MESSAGES_SENT("siembol_counter_hearbeat_messages_sent_%s"),
    HEARTBEAT_MESSAGES_READ("siembol_counter_hearbeat_messages_read"),
    HEARTBEAT_LATENCY_PARSING_MS("siembol_gauge_hearbeat_latency_ms_parsing"),
    HEARTBEAT_LATENCY_ENRICHING_MS("siembol_gauge_hearbeat_latency_ms_enriching"),
    HEARTBEAT_LATENCY_RESPONDING_MS("siembol_gauge_hearbeat_latency_ms_responding"),
    HEARTBEAT_LATENCY_TOTAL_MS("siembol_gauge_hearbeat_latency_ms_total"),
    HEARTBEAT_PRODUCER_ERROR("siembol_counter_heartbeat_producer_error_%s"),
    HEARTBEAT_CONSUMER_ERROR("siembol_counter_heartbeat_consumer_error");

    private final String formatStringName;

    SiembolMetrics(String formatStringName) {
        this.formatStringName = formatStringName;
    }

    public String getMetricName(Object... args) {
        return String.format(formatStringName, args);
    }
}
