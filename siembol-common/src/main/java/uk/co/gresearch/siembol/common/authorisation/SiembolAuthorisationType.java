package uk.co.gresearch.siembol.common.authorisation;
/**
 * An enum for representing a Siembol authorisation type
 *
 * @author  Marian Novotny
 * @see #DISABLED
 * @see #OAUTH2
 */
public enum SiembolAuthorisationType {
    DISABLED("disabled"),
    OAUTH2("oauth2");
    private final String name;

    SiembolAuthorisationType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
