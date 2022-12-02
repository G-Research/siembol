package uk.co.gresearch.siembol.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
/**
 * A data transfer object for representing Storm topologies
 *
 * <p>This class is used for json (de)serialisation of Storm topologies.
 * It is used in storm topology manager and synchronisation service.
 *
 * @author  Marian Novotny
 * @see JsonProperty
 * @see StormTopologyDto
 */
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
