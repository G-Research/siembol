package uk.co.gresearch.siembol.configeditor.sync.service;

import org.springframework.boot.actuate.health.Health;
import uk.co.gresearch.siembol.common.model.StormTopologyDto;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;

import java.util.List;
import java.util.Set;

public interface StormApplicationProvider {
    ConfigEditorResult getStormTopologies();

    ConfigEditorResult getStormTopologies(String serviceName);

    ConfigEditorResult updateStormTopologies(List<StormTopologyDto> topologies, Set<String> serviceNames);

    ConfigEditorResult restartStormTopology(String serviceName, String topologyName);

    ConfigEditorResult restartStormTopologiesOfServices(List<String> serviceNames);

    Health checkHealth();
}
