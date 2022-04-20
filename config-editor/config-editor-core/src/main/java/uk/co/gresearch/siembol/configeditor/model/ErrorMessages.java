package uk.co.gresearch.siembol.configeditor.model;

public enum ErrorMessages {
    CONFIG_ITEM_ALREADY_EXISTS("%s already exists"),
    CONFIG_ITEM_UNEXPECTED_VERSION("Unexpected version for %s update"),
    PR_UNEXPECTED_VERSION("Unexpected version for %s update"),
    GENERIC_WRONG_REQUEST("The request is invalid");
    private final String message;

    ErrorMessages(String message) {
        this.message = message;
    }

    public String getMessage(Object ...args) {
        return String.format(message, args);
    }
}
