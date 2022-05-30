package uk.co.gresearch.siembol.common.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AlertingSparkTestingResultDto {
    private static final ObjectReader JSON_GENERIC_READER = new ObjectMapper()
            .readerFor(new TypeReference<Map<String, Object>>() {
            });
    @JsonProperty("matches_total")
    private int matchesTotal;

    @JsonProperty("exceptions_total")
    private int exceptionsTotal;

    private List<Map<String, Object>> matches;
    private List<Map<String, Object>> exceptions;

    public int getMatchesTotal() {
        return matchesTotal;
    }

    public void setMatchesTotal(int matchesTotal) {
        this.matchesTotal = matchesTotal;
    }

    public int getExceptionsTotal() {
        return exceptionsTotal;
    }

    public void setExceptionsTotal(int exceptionsTotal) {
        this.exceptionsTotal = exceptionsTotal;
    }

    public List<Map<String, Object>> getMatches() {
        return matches;
    }

    public void setMatches(List<Map<String, Object>> matches) {
        this.matches = matches;
    }

    public List<Map<String, Object>> getExceptions() {
        return exceptions;
    }

    public void setExceptions(List<Map<String, Object>> exceptions) {
        this.exceptions = exceptions;
    }

    @JsonIgnore
    public void setMatchesStrings(List<String> matchesStrings) {
        this.matches = convertToMapList(matchesStrings);
    }

    @JsonIgnore
    public void setExceptionsStrings(List<String> exceptionsStrings) {
        this.exceptions = convertToMapList(exceptionsStrings);
    }

    private List<Map<String, Object>> convertToMapList(List<String> objects) {
        return objects.stream().map(x -> {
            try {
                return (Map<String, Object>) JSON_GENERIC_READER.readValue(x);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
    }
}
