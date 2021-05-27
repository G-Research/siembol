package uk.co.gresearch.siembol.configeditor.service.alerts.sigma.model;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.JsonNode;
import net.minidev.json.annotate.JsonIgnore;

import java.util.HashMap;
import java.util.Map;

public class SigmaDetectionDto {
    @JsonIgnore
    private Map<String, JsonNode> searchesMap = new HashMap<>();
    private String condition;
    private Object timeframe;

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    @JsonAnySetter
    public void set(String fieldName, JsonNode value){
        this.searchesMap.put(fieldName, value);
    }

    @JsonIgnore
    public Map<String, JsonNode> getSearchesMap() {
        return searchesMap;
    }

    public void setSearchesMap(Map<String, JsonNode> searchesMap) {
        this.searchesMap = searchesMap;
    }

    public Object getTimeframe() {
        return timeframe;
    }

    public void setTimeframe(Object timeframe) {
        this.timeframe = timeframe;
    }
}
