package uk.co.gresearch.siembol.configeditor.model;

public enum ErrorResolutions {
    GENERIC_BAD_REQUEST("Try to inspect error message and try to fix and reply your request"),
    CONCURRENT_USERS("Siembol UI can be used by multiple users in parallel. Try to reload UI, modify and reply your request"),
    VALIDATION("Try to inspect error message and modify your changes. Ask for help administrators if the problems occur");
    private final String resolution;

    ErrorResolutions(String resolution) {
        this.resolution = resolution;
    }

    public String getResolution(Object ...args) {
        return String.format(resolution, args);
    }
}
