package uk.co.gresearch.siembol.deployment.monitoring.heartbeat;

public enum HeartbeatMetrics { // go to siembolMetrics in siembol common
    HEARTBEAT_MESSAGES_SENT("siembol_hearbeat_messages_sent_counter_%s"),
    HEARTBEAT_MESSAGES_READ("siembol_hearbeat_messages_read_counter_%s"),
    HEARTBEAT_LATENCY_PARSING("siembol_hearbeat_latency_parsing_gauge_%s"),
    HEARTBEAT_LATENCY_ENRICHING("siembol_hearbeat_latency_enriching_gauge_%s"),
    HEARTBEAT_LATENCY_RESPONDING("siembol_hearbeat_latency_responding_gauge_%s"),
    HEARTBEAT_LATENCY_TOTAL("siembol_hearbeat_latency_total_gauge_%s"),
    HEARTBEAT_PRODUCER_ERROR("siembol_heartbeat_producer_error_counter_%s"),
    HEARTBEAT_CONSUMER_ERROR("siembol_heartbeat_consumer_error_counter_%s");

    private final String formatStringName;

    HeartbeatMetrics(String formatStringName) {
        this.formatStringName = formatStringName;
    }

    public String getMetricName(Object... args) {
        return String.format(formatStringName, args);
    }

    // question about total latency -> computing timestamp when consumed?
}
