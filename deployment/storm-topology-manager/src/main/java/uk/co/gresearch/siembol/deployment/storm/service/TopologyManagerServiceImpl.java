package uk.co.gresearch.siembol.deployment.storm.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.common.model.StormTopologiesDto;
import uk.co.gresearch.siembol.common.model.StormTopologyDto;
import uk.co.gresearch.siembol.common.zookeper.ZookeeperConnector;
import uk.co.gresearch.siembol.deployment.storm.model.StormResponseTopologyDto;
import uk.co.gresearch.siembol.deployment.storm.providers.KubernetesProvider;
import uk.co.gresearch.siembol.deployment.storm.providers.StormProvider;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TopologyManagerServiceImpl implements TopologyManagerService {
    private static final Logger LOG = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());

    private final StormProvider stormProvider;
    private final ZookeeperConnector zookeeperDesiredState;
    private final ZookeeperConnector zookeeperSavedState;
    private final KubernetesProvider kubernetesProvider;
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private static final ObjectReader TOPOLOGY_READER = new ObjectMapper()
            .readerFor(StormTopologiesDto.class);

    public TopologyManagerServiceImpl(StormProvider stormProvider,
                                      KubernetesProvider kubernetesProvider,
                                      ZookeeperConnector zookeeperDesiredState,
                                      ZookeeperConnector zookeeperSavedState,
                                      int scheduleAtFixedRate) {
        this.stormProvider = stormProvider;
        this.kubernetesProvider = kubernetesProvider;
        this.zookeeperDesiredState = zookeeperDesiredState;
        this.zookeeperSavedState = zookeeperSavedState;

        this.zookeeperDesiredState.addCacheListener(() -> this.executorService.execute(this::synchronise));

        if(scheduleAtFixedRate > 0) {
            this.executorService.scheduleAtFixedRate(() -> this.executorService.execute(this::synchronise),
                    scheduleAtFixedRate,
                    scheduleAtFixedRate,
                    TimeUnit.SECONDS);
        }
    }

    private void synchronise() {
        try {
            String desiredStateRaw = zookeeperDesiredState.getData();
            String savedStateRaw = zookeeperSavedState.getData();

            Map<String, StormTopologyDto> desiredState = getZookeeperState(desiredStateRaw);
            Map<String, StormTopologyDto> savedState = getZookeeperState(savedStateRaw);
            Map<String, StormResponseTopologyDto> runningInStorm = getStormState();

            LOG.debug("Desired State: {}, Saved State: {}, Storm State: {}", desiredState.keySet(),
                    savedState.keySet(), runningInStorm.keySet());

            Stream<Boolean> responseStream = runningInStorm.values()
                    .stream()
                    .filter(t -> shouldKillTopology(t, desiredState, savedState))
                    .map(t -> stormProvider.killTopology(t.getId()));

            if(responseStream.allMatch(response -> response)){
                saveState(desiredStateRaw);
            } else {
                throw new Exception("Failed to kill all storm topologies");
            }

            desiredState.values()
                .stream()
                .filter(t -> shouldStartTopology(t, savedState, runningInStorm))
                .forEach(kubernetesProvider::createOrReplaceJob);

        } catch (Exception e) {
            LOG.error("Exception in synchronise: ", e);
        }
    }

    public void invokeSynchronise() {
        this.executorService.execute(this::synchronise);
    }

    private Map<String, StormTopologyDto> getZookeeperState(String zookeeperState) throws JsonProcessingException {
        StormTopologiesDto desiredState = TOPOLOGY_READER.readValue(zookeeperState);

        return desiredState.getTopologies()
                    .stream()
                    .collect(Collectors.toMap(StormTopologyDto::getTopologyName, Function.identity()));
    }

    private Map<String, StormResponseTopologyDto> getStormState() throws IOException {
         return stormProvider.listTopologies()
                    .stream()
                    .collect(Collectors.toMap(StormResponseTopologyDto::getName, Function.identity()));
    }

    private boolean shouldKillTopology(StormResponseTopologyDto topology, Map<String, StormTopologyDto> desiredStateMap,
                                       Map<String, StormTopologyDto> savedStateMap){
        String runningInstanceTopologyName = topology.getName();
        Optional<StormTopologyDto> desiredInstance = Optional.ofNullable(desiredStateMap.get(runningInstanceTopologyName));
        Optional<StormTopologyDto> savedInstance = Optional.ofNullable(savedStateMap.get(runningInstanceTopologyName));

        boolean deletedTopology = !desiredInstance.isPresent() && savedInstance.isPresent();

        boolean topologyUpgraded = desiredInstance.isPresent() && savedInstance.isPresent() &&
                !Objects.equals(desiredInstance.get().getTopologyId(), savedInstance.get().getTopologyId());

        boolean runningTopologyWithNameConflict = desiredInstance.isPresent() && !savedInstance.isPresent();

        if (runningTopologyWithNameConflict)
            LOG.warn("Existing topology running in storm with conflicting name: {}. Killing...", topology.getName());

        return deletedTopology || topologyUpgraded || runningTopologyWithNameConflict;
    }

    private boolean shouldStartTopology(StormTopologyDto topology, Map<String, StormTopologyDto> savedStateMap,
                                        Map<String, StormResponseTopologyDto> runningInStorm) {
        String topologyName = topology.getTopologyName();
        Optional<StormTopologyDto> savedInstance = Optional.ofNullable(savedStateMap.get(topologyName));
        Optional<StormResponseTopologyDto> runningInstance = Optional.ofNullable(runningInStorm.get(topologyName));

        boolean newTopology = !savedInstance.isPresent();

        boolean topologyUpgraded = savedInstance.isPresent() &&
                !Objects.equals(topology.getTopologyId(), savedInstance.get().getTopologyId());

        boolean missingFromStorm = !runningInstance.isPresent();

        return newTopology || topologyUpgraded || missingFromStorm;
    }

    private void saveState(String stateToSave) throws Exception {
        zookeeperSavedState.setData(stateToSave);
    }
}
