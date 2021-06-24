package uk.co.gresearch.siembol.configeditor.sync.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import uk.co.gresearch.siembol.common.model.StormTopologiesDto;
import uk.co.gresearch.siembol.common.model.StormTopologyDto;
import uk.co.gresearch.siembol.common.model.ZooKeeperAttributesDto;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnector;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnectorFactory;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorAttributes;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.BAD_REQUEST;
import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.OK;

public class StormApplicationProviderImpl implements StormApplicationProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final ObjectReader TOPOLOGIES_READER = new ObjectMapper()
            .readerFor(StormTopologiesDto.class);
    private static final ObjectWriter TOPOLOGIES_WRITER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .writerFor(StormTopologiesDto.class);
    private static final String WRONG_TOPOLOGIES_FORMAT = "Wrong format of storm topologies {}";
    private static final String NOTHING_TO_UPDATE_MSG = "nothing to update";
    private static final String WRONG_TOPOLOGY_NAME_MSG = "Topology with the name %s of the service %s is not released";
    private static final String REQUESTED_RESTART_TOPOLOGY_WITH_NAME = "Requested restart topology with name {}";
    private static final String INIT_START_MSG = "Starting initialising storm application provider";
    private static final String INIT_COMPLETED_MSG = "Initialisation of storm application provider completed";
    private static final String DUPLICATE_NAME_MSG = "Duplicate topology name %s in updated topologies";
    private static final String UPDATING_TOPOLOGIES_IN_ZOOKEEPER = "Updating topologies in zookeeper {}";

    private final ZooKeeperConnector zooKeeperConnector;
    private final AtomicReference<String> topologies = new AtomicReference<>();
    private final AtomicReference<Exception> exception = new AtomicReference<>();

    public StormApplicationProviderImpl(ZooKeeperConnector zooKeeperConnector) {
        this.zooKeeperConnector = zooKeeperConnector;
        this.updateTopologiesCallback();
        this.zooKeeperConnector.addCacheListener(this::updateTopologiesCallback);
    }

    private void updateTopologiesCallback() {
        String newTopologiesString = zooKeeperConnector.getData();
        topologies.set(newTopologiesString);
        getCurrentTopologies();
    }

    private StormTopologiesDto getCurrentTopologies() {
        String currentTopologies = topologies.get();
        try {
            return TOPOLOGIES_READER.readValue(topologies.get());
        } catch (IOException e) {
            exception.set(e);
            LOGGER.error(WRONG_TOPOLOGIES_FORMAT, currentTopologies);
            throw new IllegalStateException(e);
        }
    }

    private ConfigEditorResult sendTopologiesToZookeeper(List<StormTopologyDto> updatedTopologies) {
        Set<String> names = new HashSet<>();
        for (StormTopologyDto topology : updatedTopologies) {
            if (names.contains(topology.getTopologyName())) {
                String msg = String.format(DUPLICATE_NAME_MSG, topology.getTopologyName());
                LOGGER.error(msg);
                Exception e = new IllegalStateException(msg);
                exception.set(e);
                return ConfigEditorResult.fromException(e);
            } else {
                names.add(topology.getTopologyName());
            }
        }

        updatedTopologies.sort(Comparator.comparing(StormTopologyDto::getTopologyName));
        StormTopologiesDto topologiesToSend = new StormTopologiesDto();
        topologiesToSend.setTopologies(updatedTopologies);
        topologiesToSend.setTimestamp(System.currentTimeMillis());
        try {
            final String updatedString = TOPOLOGIES_WRITER.writeValueAsString(topologiesToSend);
            LOGGER.info(UPDATING_TOPOLOGIES_IN_ZOOKEEPER, updatedString);
            zooKeeperConnector.setData(updatedString);

            ConfigEditorAttributes attributes = new ConfigEditorAttributes();
            attributes.setTopologies(updatedTopologies);
            return new ConfigEditorResult(OK, attributes);
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            return ConfigEditorResult.fromException(e);
        }
    }

    private static boolean areEqualTopologies(StormTopologyDto topology1, StormTopologyDto topology2) {
        return topology1.getTopologyName().equals(topology2.getTopologyName())
                && topology1.getImage().equals(topology2.getImage())
                && topology1.getServiceName().equals(topology2.getServiceName())
                && topology1.getAttributes().equals(topology2.getAttributes());
    }

    @Override
    public ConfigEditorResult getStormTopologies() {
        ConfigEditorAttributes attributes = new ConfigEditorAttributes();
        attributes.setTopologies(getCurrentTopologies().getTopologies());
        return new ConfigEditorResult(OK, attributes);
    }

    @Override
    public ConfigEditorResult getStormTopologies(String serviceName) {
        List<StormTopologyDto> currentTopologies = getCurrentTopologies().getTopologies().stream()
                .filter(x -> x.getServiceName().equals(serviceName))
                .collect(Collectors.toList());

        ConfigEditorAttributes attributes = new ConfigEditorAttributes();
        attributes.setTopologies(currentTopologies);
        return new ConfigEditorResult(OK, attributes);
    }

    @Override
    public ConfigEditorResult updateStormTopologies(List<StormTopologyDto> topologiesToUpdate,
                                                    Set<String> serviceNames) {
        StormTopologiesDto currentTopologies = getCurrentTopologies();
        List<StormTopologyDto> newTopologies = new ArrayList<>();
        Map<String, StormTopologyDto> relatedTopologies = new HashMap<>();
        currentTopologies.getTopologies().forEach(x -> {
            if (serviceNames.contains(x.getServiceName())) {
                relatedTopologies.put(x.getTopologyName(), x);
            } else {
                newTopologies.add(x);
            }
        });

        boolean changed = false;
        for (StormTopologyDto topologyToUpdate: topologiesToUpdate) {
            String topologyName = topologyToUpdate.getTopologyName();
            if (!relatedTopologies.containsKey(topologyName)
                    || !areEqualTopologies(relatedTopologies.get(topologyName), topologyToUpdate)) {
                changed = true;
                newTopologies.add(topologyToUpdate);
            } else {
                newTopologies.add(relatedTopologies.get(topologyName));
            }
        }

        return !changed && newTopologies.size() == currentTopologies.getTopologies().size()
                ? ConfigEditorResult.fromMessage(OK, NOTHING_TO_UPDATE_MSG)
                : sendTopologiesToZookeeper(newTopologies);
    }

    @Override
    public ConfigEditorResult restartStormTopology(String serviceName, String topologyName) {
        LOGGER.info(REQUESTED_RESTART_TOPOLOGY_WITH_NAME, topologyName);
        StormTopologiesDto currentTopologies = getCurrentTopologies();

        for (StormTopologyDto topology: currentTopologies.getTopologies()) {
            if (topology.getTopologyName().equals(topologyName)
                    && topology.getServiceName().equals(serviceName)) {
                topology.setTopologyId(UUID.randomUUID().toString());
                return sendTopologiesToZookeeper(currentTopologies.getTopologies());
            }
        }
        String msg = String.format(WRONG_TOPOLOGY_NAME_MSG, topologyName, serviceName);
        LOGGER.info(msg);
        return ConfigEditorResult.fromMessage(BAD_REQUEST, msg);
    }

    @Override
    public Health checkHealth() {
        return exception.get() == null
                ? Health.up().build()
                : Health.down().withException(exception.get()).build();
    }

    public static StormApplicationProviderImpl create(ZooKeeperConnectorFactory zooKeeperConnectorFactory,
                                                      ZooKeeperAttributesDto zookeeperAttributes) throws Exception {
        LOGGER.info(INIT_START_MSG);
        ZooKeeperConnector zooKeeperConnector = zooKeeperConnectorFactory.createZookeeperConnector(zookeeperAttributes);
        LOGGER.info(INIT_COMPLETED_MSG);
        return new StormApplicationProviderImpl(zooKeeperConnector);
    }
}
