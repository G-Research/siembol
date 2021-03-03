package uk.co.gresearch.siembol.deployment.storm.providers;

import uk.co.gresearch.siembol.deployment.storm.model.StormResponseTopologyDto;

import java.io.IOException;
import java.util.List;

public interface StormProvider {
    List<StormResponseTopologyDto> listTopologies() throws IOException;
    boolean killTopology(String id);
}
