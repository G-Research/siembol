package uk.co.gresearch.siembol.deployment.storm.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class TopologyManagerInfoDto {
    @JsonProperty("number_synchronised")
    private int numberSynchronised;
    @JsonProperty("number_different")
    private int numberDifferent;
    private Map<String, TopologyStateDto> topologies;

    public Map<String, TopologyStateDto> getTopologies() {
        return topologies;
    }

    public void setTopologies(Map<String, TopologyStateDto> topologies) {
        this.topologies = topologies;
    }

    public int getNumberSynchronised() {
        return numberSynchronised;
    }

    public void setNumberSynchronised(int numberSynchronised) {
        this.numberSynchronised = numberSynchronised;
    }

    public int getNumberDifferent() {
        return numberDifferent;
    }

    public void setNumberDifferent(int numberDifferent) {
        this.numberDifferent = numberDifferent;
    }
}
