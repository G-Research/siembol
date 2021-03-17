package uk.co.gresearch.siembol.common.model;

import java.util.ArrayList;
import java.util.List;

public class StormTopologiesDto {
    private Long timestamp;
    private List<StormTopologyDto> topologies = new ArrayList<>();

    public List<StormTopologyDto> getTopologies() {
        return topologies;
    }

    public void setTopologies(List<StormTopologyDto> topologies) {
        this.topologies = topologies;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
