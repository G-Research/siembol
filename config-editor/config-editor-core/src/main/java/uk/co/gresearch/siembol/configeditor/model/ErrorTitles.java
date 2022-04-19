package uk.co.gresearch.siembol.configeditor.model;

public enum ErrorTitles {
    ;
    private final String title;

    ErrorTitles(String title) {
        this.title = title;
    }

    public String getTitle(String ...args) {
        return String.format(title, args);
    }
}

