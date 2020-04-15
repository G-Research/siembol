package uk.co.gresearch.siembol.enrichments.common;

import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class EnrichmentCommand implements Serializable {
    private String tableName;
    private String key;
    private ArrayList<Pair<String, String>> tags;
    private ArrayList<Pair<String, String>> enrichmentFields;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public ArrayList<Pair<String, String>> getTags() {
        return tags;
    }

    public void setTags(ArrayList<Pair<String, String>> tags) {
        this.tags = tags;
    }

    public ArrayList<Pair<String, String>> getEnrichmentFields() {
        return enrichmentFields;
    }

    public void setEnrichmentFields(ArrayList<Pair<String, String>> enrichmentFields) {
        this.enrichmentFields = enrichmentFields;
    }

    public List<String> getTableFields() {
        return enrichmentFields == null ? Collections.emptyList() : enrichmentFields
                .stream()
                .map(x -> x.getKey())
                .collect(Collectors.toList());
    }

    public String getEnrichedEventNameByTableName(String tableName) {
        for (Pair<String, String> fieldPair : enrichmentFields) {
            if (fieldPair.getKey().equalsIgnoreCase(tableName)) {
                return fieldPair.getValue();
            }
        }
        throw new IllegalArgumentException();
    }
}
