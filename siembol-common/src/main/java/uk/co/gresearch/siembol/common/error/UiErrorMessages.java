package uk.co.gresearch.siembol.common.error;

public enum UiErrorMessages {
    CONFIG_ITEM_VALIDATION_ERROR("Validation of %s failed",
            "Try to edit %s and fix the issues in Siembol UI"), //config, rule, test case, admin config test specification
    RELEASE_VALIDATION_ERROR("Validation of release failed",
            "Try to remove wrong configuration(s) and try to release again"),

    CONFIG_SUBMIT_ITEM_ALREADY_EXISTS("Submitted %s with the same name already exists",
            "Please use other name for your %s"), //config, rule, test case
    CONFIG_SUBMIT_WRONG_VERSION("Submitted %s with the same name has different version in git store repository",
            "Configuration can be edited by other users. Please reload Siembol UI and edit your %s"), //config, rule, test case


    PR_OPEN("Pull Request is already open in %s repository", "Only one PR is allowed to be open in %s repository. Please try to resolve existing PR and try again."),
    RELEASE_PR_WRONG_VERSION("", "y"),
    ADMIN_CONFIG_PR_WRONG_VERSION("x", "y"),
    INTERNAL_ERROR("Unexpected error occurs in Siembol UI", "Try to inspect detailed error message and contact administrators if needed");

    private final String title;
    private final String resolution;

    UiErrorMessages(String title, String resolution) {
        this.title = title;
        this.resolution = resolution;
    }

    public String getTitle(String... args) {
        return String.format(title, args);
    }

    public String getResolution(String... args) {
        return String.format(resolution, args);
    }
}
