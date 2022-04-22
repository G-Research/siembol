package uk.co.gresearch.siembol.configeditor.model;

public enum ErrorTitles {
    ADD_CONFIG("Problem during storing configuration in git repository"),
    ADD_TEST_CASE("Problem during storing test case in git repository"),
    UPDATE_CONFIG("Problem during updating configuration in git repository"),
    UPDATE_TEST_CASE("Problem during updating test case in git repository"),
    CREATE_RELEASE_PR("Problem during creating release PR in git repository"),
    CREATE_ADMIN_CONFIG_PR("Problem during creating admin config PR in git repository"),
    DELETE_CONFIG("Problem during deleting config %s in git repository"),
    DELETE_TEST_CASE("Problem during deleting test case %s in git repository"),
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

