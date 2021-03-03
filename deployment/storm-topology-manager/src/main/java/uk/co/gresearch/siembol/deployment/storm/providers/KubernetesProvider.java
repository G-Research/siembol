package uk.co.gresearch.siembol.deployment.storm.providers;

import uk.co.gresearch.siembol.common.model.StormTopologyDto;


public interface KubernetesProvider {
   void createOrReplaceJob(StormTopologyDto attr);
}
