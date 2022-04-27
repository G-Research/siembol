package uk.co.gresearch.siembol.configeditor.model;

public enum ErrorTitles {
    ADD_CONFIG("Problem storing configuration in git repository"),
    ADD_TEST_CASE("Problem storing test case in git repository"),
    UPDATE_CONFIG("Problem updating configuration in git repository"),
    UPDATE_TEST_CASE("Problem updating test case in git repository"),
    CREATE_RELEASE_PR("Problem creating release PR in git repository"),
    CREATE_ADMIN_CONFIG_PR("Problem creating admin config PR in git repository"),
    DELETE_CONFIG("Problem deleting config %s in git repository"),
    DELETE_TEST_CASE("Problem deleting test case %s in git repository"),
    VALIDATION_GENERIC("Validation failure"),
    TESTING_GENERIC("Testing failure"),
    IMPORTING_CONFIG_GENERIC("Importing configuration failed"),
    GENERIC_BAD_REQUEST("Invalid request"),
    GENERIC_INTERNAL_ERROR("Internal server error");

    private final String title;

    ErrorTitles(String title) {
        this.title = title;
    }

    public String getTitle(Object ...args) {
        return String.format(title, args);
    }
}

