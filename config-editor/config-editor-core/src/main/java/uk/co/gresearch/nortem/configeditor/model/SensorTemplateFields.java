package uk.co.gresearch.nortem.configeditor.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SensorTemplateFields {
    @JsonProperty("sensor_name")
    private String sensorName;
    private List<TemplateField> fields;

    public SensorTemplateFields(String sensorName, List<TemplateField> fields) {
        this.sensorName = sensorName;
        this.fields = fields;
    }

    public String getSensorName() {
        return sensorName;
    }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }

    public List<TemplateField> getFields() {
        return fields;
    }

    public void setFields(List<TemplateField> fields) {
        this.fields = fields;
    }
}
