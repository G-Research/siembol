package uk.co.gresearch.siembol.deployment.storm.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import uk.co.gresearch.siembol.common.model.StormTopologiesDto;
import uk.co.gresearch.siembol.common.model.StormTopologyDto;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnector;
import uk.co.gresearch.siembol.deployment.storm.model.StormResponseTopologyDto;
import uk.co.gresearch.siembol.deployment.storm.model.TopologyManagerInfoDto;
import uk.co.gresearch.siembol.deployment.storm.model.TopologyStateDto;
import uk.co.gresearch.siembol.deployment.storm.providers.KubernetesProvider;
import uk.co.gresearch.siembol.deployment.storm.providers.StormProvider;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TopologyManagerServiceImpl implements TopologyManagerService {
    private static final Logger LOG = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());
    private static final ObjectReader TOPOLOGY_READER = new ObjectMapper()
            .readerFor(StormTopologiesDto.class);

    private final StormProvider stormProvider;
    private final ZooKeeperConnector zookeeperDesiredState;
    private final ZooKeeperConnector zookeeperSavedState;
    private final KubernetesProvider kubernetesProvider;
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final AtomicReference<Optional<Exception>> exception = new AtomicReference<>(Optional.empty());

    public TopologyManagerServiceImpl(StormProvider stormProvider,
                                      KubernetesProvider kubernetesProvider,
                                      ZooKeeperConnector zookeeperDesiredState,
                                      ZooKeeperConnector zookeeperSavedState,
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

    @Override
    public void invokeSynchronise() {
        this.executorService.execute(this::synchronise);
    }

    @Override
    public TopologyManagerInfoDto getTopologyManagerInfo() {
        TopologyManagerInfoDto ret = new TopologyManagerInfoDto();
        try {
            Map<String, StormTopologyDto> desiredState = getZookeeperState(zookeeperDesiredState.getData());
            Map<String, StormTopologyDto> savedState = getZookeeperState(zookeeperSavedState.getData());

            Set<String> topologyNames = new HashSet<>();
            topologyNames.addAll(desiredState.keySet());
            topologyNames.addAll(savedState.keySet());

            Map<String, TopologyStateDto> topologies = new HashMap<>();
            int numSynchronised = 0;
            for (String topologyName : topologyNames) {
                TopologyStateDto state = getTopologyState(topologyName, desiredState, savedState);
                topologies.put(topologyName, state);
                if (state.equals(TopologyStateDto.SYNCHRONISED)) {
                    numSynchronised++;
                }
            }
            ret.setTopologies(topologies);
            ret.setNumberSynchronised(numSynchronised);
            ret.setNumberDifferent(topologies.size() - numSynchronised);

        } catch (JsonProcessingException e) {
            LOG.error("Exception during getting topology manager Info: ", e);
            exception.set(Optional.of(e));
        }
        return ret;
    }

    @Override
    public Health checkHealth() {
        Optional<Exception> e = exception.get();
        return e.isPresent() ? Health.down(e.get()).build() : Health.up().build();
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
            exception.set(Optional.of(e));
            executorService.shutdown();
        }
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

    private TopologyStateDto getTopologyState(String name,
                                              Map<String, StormTopologyDto> desiredStateMap,
                                              Map<String, StormTopologyDto> savedStateMap) {
        if (desiredStateMap.containsKey(name)) {
            if (savedStateMap.containsKey(name)) {
                return desiredStateMap.get(name).getTopologyId().equals(savedStateMap.get(name).getTopologyId())
                        ? TopologyStateDto.SYNCHRONISED : TopologyStateDto.DIFFERENT;
            } else {
                return TopologyStateDto.DESIRED_STATE_ONLY;
            }
        } else {
            return TopologyStateDto.SAVED_STATE_ONLY;
        }
    }
}
