package uk.co.gresearch.nortem.configeditor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

public class ConfigEditorFileHistoryItem {
    private String author;
    private String date;
    @JsonProperty("added")
    private Integer addedLines;
    @JsonProperty("removed")
    private Integer removedLines;

    @JsonIgnore
    private Integer timestamp;

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getAddedLines() {
        return addedLines;
    }

    public void setAddedLines(Integer addedLines) {
        this.addedLines = addedLines;
    }

    public Integer getRemoved() {
        return removedLines;
    }

    public void setRemoved(Integer removed) {
        this.removedLines = removed;
    }

    @JsonIgnore
    public Integer getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Integer timestamp) {
        this.timestamp = timestamp;
        date = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp),
                TimeZone.getDefault().toZoneId()).toString();
    }

}
