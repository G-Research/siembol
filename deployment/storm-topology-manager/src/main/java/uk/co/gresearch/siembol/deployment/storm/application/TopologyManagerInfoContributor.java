package uk.co.gresearch.siembol.deployment.storm.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;
import uk.co.gresearch.siembol.deployment.storm.model.TopologyManagerInfoDto;
import uk.co.gresearch.siembol.deployment.storm.service.TopologyManagerService;

@Component
public class TopologyManagerInfoContributor implements InfoContributor {
    private static final String MANAGER_INFO_KEY = "topology_manager_info";

    @Autowired
    private final TopologyManagerService topologyManagerService;

    public TopologyManagerInfoContributor(TopologyManagerService topologyManagerService) {
        this.topologyManagerService = topologyManagerService;
    }

    @Override
    public void contribute(Info.Builder builder) {
        TopologyManagerInfoDto managerInfo = topologyManagerService.getTopologyManagerInfo();
        builder.withDetail(MANAGER_INFO_KEY, managerInfo).build();
    }
}
