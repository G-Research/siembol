package uk.co.gresearch.siembol.configeditor.model;

public enum ErrorTitles {
    ADD_CONFIG("Problem during storing configuration in git repository"),
    ADD_TEST_CASE("Problem during storing test case in git repository"),
    UPDATE_CONFIG("Problem during updating configuration in git repository"),
    UPDATE_TEST_CASE("Problem during updating test case in git repository");
    private final String title;

    ErrorTitles(String title) {
        this.title = title;
    }

    public String getTitle(Object ...args) {
        return String.format(title, args);
    }
}

