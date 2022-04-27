package uk.co.gresearch.siembol.configeditor.model;

public enum ErrorResolutions {
    GENERIC_BAD_REQUEST("Inspect error message and try to fix and replay your request"),
    CONCURRENT_USERS("Siembol UI can be used by multiple users in parallel. " +
            "Refresh UI (F5), modify and replay your request"),
    VALIDATION("Inspect error message and modify your changes. " +
            "Ask administrators for help if the problem persists"),
    GENERIC_INTERNAL_ERROR("Ask administrators for help");
    private final String resolution;

    ErrorResolutions(String resolution) {
        this.resolution = resolution;
    }

    public String getResolution(Object ...args) {
        return String.format(resolution, args);
    }
}
