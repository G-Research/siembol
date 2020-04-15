package uk.co.gresearch.siembol.configeditor.model;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConfigEditorFile {
    public enum ContentType {
        RAW_JSON_STRING,
        STRING
    }
    @JsonProperty("file_name")
    @NotNull
    private String fileName;
    @JsonRawValue
    private String content;
    private String stringContent;

    @JsonProperty("file_history")
    private List<ConfigEditorFileHistoryItem> fileHistory = new ArrayList<>();

    public ConfigEditorFile() {
    }

    public ConfigEditorFile(String fileName, String content, ContentType type) {
        this.fileName = fileName;
        switch (type) {
            case RAW_JSON_STRING:
                this.content = content;
                break;
            case STRING:
                this.stringContent = content;
                break;
        }
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContent() {
        return content;
    }

    @JsonSetter("content")
    void setContent(JsonNode content) {
        this.content = content.toString();
    }

    @JsonProperty("string_content")
    public String getStringContent() {
        return stringContent;
    }

    @JsonSetter("string_content")
    public void setStringContent(String stringContent) {
        this.stringContent = stringContent;
    }

    @JsonIgnore
    public String getContentValue() {
        return content != null ? content : stringContent;
    }

    public List<ConfigEditorFileHistoryItem> getFileHistory() {
        return fileHistory;
    }

    public void setFileHistory(List<ConfigEditorFileHistoryItem> fileHistory) {
        this.fileHistory = fileHistory;
    }
}
