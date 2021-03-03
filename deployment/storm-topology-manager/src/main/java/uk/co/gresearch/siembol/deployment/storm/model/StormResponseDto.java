package uk.co.gresearch.siembol.deployment.storm.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StormResponseDto {
    private List<StormResponseTopologyDto> topologies;
    private String topologyOperation;
    private String topologyId;
    private String status;

    public List<StormResponseTopologyDto> getTopologies() {
        return topologies;
    }

    public void setTopologies(List<StormResponseTopologyDto> topologies) {
        this.topologies = topologies;
    }

    public String getTopologyOperation() {
        return topologyOperation;
    }

    public void setTopologyOperation(String topologyOperation) {
        this.topologyOperation = topologyOperation;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTopologyId() {
        return topologyId;
    }

    public void setTopologyId(String topologyId) {
        this.topologyId = topologyId;
    }
}
