package uk.co.gresearch.siembol.configeditor.model;

public enum ErrorResolution {
    GENERIC_BAD_REQUEST("Try to inspect error message and try to fix and reply your request"),
    CONCURENT_USERS("Siembol UI can be used by multiple users in parallel. Try to reload UI, modify and reply your request.");
    private final String resolution;

    ErrorResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getResolution(Object ...args) {
        return String.format(resolution, args);
    }
}
