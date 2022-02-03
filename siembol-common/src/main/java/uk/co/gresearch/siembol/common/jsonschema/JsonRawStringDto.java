package uk.co.gresearch.siembol.common.jsonschema;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.SchemaIgnore;

import java.util.Map;
import java.util.HashMap;
import java.util.Properties;

@Attributes(title = "json raw string", description = "An arbitrary json object")
public class JsonRawStringDto {
    @SchemaIgnore
    private Map<String, Object> rawMap = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Object> getRawMap() {
        return rawMap;
    }

    @JsonAnySetter
    public void set(String fieldName, Object value){
        this.rawMap.put(fieldName, value);
    }

    @JsonIgnore
    @SchemaIgnore
    public Properties getProperties() {
        var props = new Properties();
        props.putAll(rawMap);
        return props;
    }

}
