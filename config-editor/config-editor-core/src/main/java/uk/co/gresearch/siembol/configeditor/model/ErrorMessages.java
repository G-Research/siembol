package uk.co.gresearch.siembol.configeditor.model;

public enum ErrorMessages {
    CONFIG_ITEM_ALREADY_EXISTS("%s already exists"),
    CONFIG_ITEM_UNEXPECTED_VERSION("Unexpected version for %s update"),
    PR_PENDING("Can not create PR for %s because another PR: %s is pending in git repository"),
    PR_UNEXPECTED_VERSION("Can not create PR for %s with the version the version %d. Expected version: %d"),
    GENERIC_WRONG_REQUEST("The request is invalid"),
    VALIDATION_GENERIC("Attempt to validate changes failed"),
    TESTING_GENERIC("Attempt to test configuration failed");
    
    private final String message;

    ErrorMessages(String message) {
        this.message = message;
    }

    public String getMessage(Object ...args) {
        return String.format(message, args);
    }
}
