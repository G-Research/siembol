package uk.co.gresearch.siembol.configeditor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class TemplateField {
    private String name;

    public TemplateField(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonIgnore
    @Override
    public String toString() {
        return name;
    }
}
