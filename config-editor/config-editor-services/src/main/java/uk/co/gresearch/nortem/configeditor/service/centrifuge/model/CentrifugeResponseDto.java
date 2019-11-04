package uk.co.gresearch.nortem.configeditor.service.centrifuge.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;

import java.util.LinkedHashMap;
import java.util.List;

public class CentrifugeResponseDto {
    public static class Attributes {
        private String exception;
        private String message;
        @JsonProperty("json_rules")
        @JsonRawValue
        private String jsonRules;
        @JsonRawValue
        private String event;

        @JsonProperty("rules_schema")
        private LinkedHashMap<String, Object> schema;

        private List<LinkedHashMap<String, Object>> fields;

        public String getException(){ return exception; }
        public String getMessage(){ return message; }
        public void setException(String exception){
            this.exception = exception;
        }
        public void setMessage(String message){
            this.message = message;
        }

        public LinkedHashMap<String, Object> getSchema() {
            return schema;
        }

        public void setSchema(LinkedHashMap<String, Object> schema) {
            this.schema = schema;
        }

        public List<LinkedHashMap<String, Object>> getFields() {
            return fields;
        }

        public void setFields(List<LinkedHashMap<String, Object>> fields) {
            this.fields = fields;
        }

        public String getJsonRules() {
            return jsonRules;
        }

        public void setJsonRules(String jsonRules) {
            this.jsonRules = jsonRules;
        }

        public String getEvent() {
            return event;
        }

        public void setEvent(String event) {
            this.event = event;
        }
    }

    public enum StatusCode {
        OK,
        ERROR,
        BAD_REQUEST,
        UNAUTHORISED,
    }

    private StatusCode statusCode;
    private Attributes attributes = new Attributes();

    public StatusCode getStatusCode() {return this.statusCode;}

    public Attributes getAttributes() {return this.attributes;}

    public void setStatusCode(StatusCode statusCode){
        this.statusCode = statusCode;
    }

    public void setAttributes(Attributes attributes){
        this.attributes = attributes;
    }

}
