package uk.co.gresearch.siembol.common.constants;
/**
 * An enum for representing a Siembol service types
 *
 * @author  Marian Novotny
 *
 * @see #RESPONSE
 * @see #ALERT
 * @see #CORRELATION_ALERT
 * @see #PARSER_CONFIG
 * @see #PARSING_APP
 * @see #ENRICHMENT
 */
public enum ServiceType {
    RESPONSE("response"),
    ALERT("alert"),
    CORRELATION_ALERT("correlationalert"),
    PARSER_CONFIG("parserconfig"),
    PARSING_APP("parsingapp"),
    ENRICHMENT("enrichment");

    private static final String UNSUPPORTED_SERVICE_NAME = "Unsupported service name";
    private final String name;

    ServiceType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static ServiceType fromName(String name) {
        for (ServiceType serviceType : ServiceType.values()) {
            if (serviceType.getName().equalsIgnoreCase(name)) {
                return serviceType;
            }
        }

        throw new IllegalArgumentException(UNSUPPORTED_SERVICE_NAME);
    }
}
