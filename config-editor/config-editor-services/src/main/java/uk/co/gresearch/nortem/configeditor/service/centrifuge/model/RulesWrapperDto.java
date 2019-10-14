package uk.co.gresearch.nortem.configeditor.service.centrifuge.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class RulesWrapperDto {
    @JsonProperty("rules_version")
    private int rulesVersion = 1;
    @JsonProperty("rules")
    private List<Object> rules = new ArrayList<>();

    public void setRulesVersion(int rulesVersion) {
        this.rulesVersion = rulesVersion;
    }
    public void setRules(List<Object> rules){
        this.rules = rules;
    }

    public int getRulesVersion() {return this.rulesVersion;}
    public List<Object> getRules() {return this.rules;}
    public void addToRules(Object rule){
        this.rules.add(rule);
    }
}
