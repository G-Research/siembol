package uk.co.gresearch.siembol.deployment.storm.service;

import org.springframework.boot.actuate.health.Health;
import uk.co.gresearch.siembol.deployment.storm.model.TopologyManagerInfoDto;

public interface TopologyManagerService {
    void invokeSynchronise();
    TopologyManagerInfoDto getTopologyManagerInfo();
    Health checkHealth();
}
