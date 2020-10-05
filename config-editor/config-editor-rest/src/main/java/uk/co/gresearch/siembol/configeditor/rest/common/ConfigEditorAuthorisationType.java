package uk.co.gresearch.siembol.configeditor.rest.common;

public enum ConfigEditorAuthorisationType {
    DISABLED("disabled"),
    OAUTH2("oauth2");
    private final String name;

    ConfigEditorAuthorisationType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
