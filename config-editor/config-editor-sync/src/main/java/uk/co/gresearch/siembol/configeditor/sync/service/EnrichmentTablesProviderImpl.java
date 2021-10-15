package uk.co.gresearch.siembol.configeditor.sync.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.common.model.EnrichmentTableDto;
import uk.co.gresearch.siembol.common.model.EnrichmentTablesUpdateDto;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnector;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorAttributes;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.BAD_REQUEST;
import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.OK;

public class EnrichmentTablesProviderImpl implements EnrichmentTablesProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final ObjectReader ENRICHMENT_TABLES_UPDATE_MSG_READER = new ObjectMapper()
            .readerFor(EnrichmentTablesUpdateDto.class);
    private static final ObjectWriter ENRICHMENT_TABLES_UPDATE_MSG_WRITER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .writerFor(EnrichmentTablesUpdateDto.class);
    private static final String UNSUPPORTED_ENRICHMENT_SERVICE = "The service %s does not support synchronisation " +
            "of enrichment tables";
    private static final String WRONG_TABLE_TO_UPDATE = "Wrong enrichment table to update";
    private static final String INVALID_TABLES_LOG_MSG = "Invalid enrichment tables update message format " +
            "in the service: {}, enrichment tables value: {}";
    private static final String ADD_NEW_EXISTING_TABLE_MSG = "Table with name %s already exists";
    private static final String UPDATE_NON_EXISTING_TABLE_MSG = "Table with name %s does not exist";

    private final Map<String, ZooKeeperConnector> zooKeeperConnectorMap;
    private final Map<String, AtomicReference<String>> enrichmentTablesCache;

    public EnrichmentTablesProviderImpl(Map<String, ZooKeeperConnector> zooKeeperConnectorMap) {
        this.zooKeeperConnectorMap = zooKeeperConnectorMap != null ? zooKeeperConnectorMap : new HashMap<>();
        this.enrichmentTablesCache = new HashMap<>();

        for (Map.Entry<String, ZooKeeperConnector> entry : this.zooKeeperConnectorMap.entrySet()) {
            final String serviceName = entry.getKey();
            enrichmentTablesCache.put(serviceName, new AtomicReference<>());
            entry.getValue().addCacheListener(() -> updateEnrichmentTablesCache(serviceName));
            updateEnrichmentTablesCache(serviceName);
        }
    }

    private EnrichmentTablesUpdateDto getEnrichmentTablesFromMessage(String updateMessageStr) {
        try {
            return ENRICHMENT_TABLES_UPDATE_MSG_READER.readValue(updateMessageStr);
        } catch (JsonProcessingException e) {
            LOGGER.error(INVALID_TABLES_LOG_MSG, updateMessageStr);
            throw new IllegalStateException(e);
        }
    }

    private void updateEnrichmentTablesCache(String serviceName) {
        String updateMessageStr = zooKeeperConnectorMap.get(serviceName).getData();
        try {
            getEnrichmentTablesFromMessage(updateMessageStr);
        } catch (Exception e) {
            LOGGER.error(INVALID_TABLES_LOG_MSG, serviceName, updateMessageStr);
            throw new IllegalStateException(e);
        }
        enrichmentTablesCache.get(serviceName).set(updateMessageStr);
    }

    @Override
    public ConfigEditorResult getEnrichmentTables(String serviceName) {
        ConfigEditorResult checkServiceName = checkServiceName(serviceName);
        if (checkServiceName.getStatusCode() != OK) {
            return checkServiceName;
        }

        try {
            EnrichmentTablesUpdateDto enrichmentTables = getEnrichmentTablesFromMessage(
                    enrichmentTablesCache.get(serviceName).get());
            return ConfigEditorResult.fromEnrichmentTables(enrichmentTables.getEnrichmentTables());
        } catch (Exception e) {
            return ConfigEditorResult.fromException(e);
        }
    }

    @Override
    public ConfigEditorResult addEnrichmentTable(String serviceName, EnrichmentTableDto enrichmentTable) {
        ConfigEditorResult checkServiceName = checkServiceName(serviceName);
        if (checkServiceName.getStatusCode() != OK) {
            return checkServiceName;
        }

        return updateEnrichmentTableInternally(serviceName, enrichmentTable, true);
    }

    @Override
    public ConfigEditorResult updateEnrichmentTable(String serviceName, EnrichmentTableDto enrichmentTable) {
        ConfigEditorResult checkServiceName = checkServiceName(serviceName);
        if (checkServiceName.getStatusCode() != OK) {
            return checkServiceName;
        }

        return updateEnrichmentTableInternally(serviceName, enrichmentTable, false);
    }

    private ConfigEditorResult checkServiceName(String name) {
        return zooKeeperConnectorMap.containsKey(name)
                ? new ConfigEditorResult(OK, new ConfigEditorAttributes())
                : ConfigEditorResult.fromMessage(BAD_REQUEST, String.format(UNSUPPORTED_ENRICHMENT_SERVICE, name));
    }

    private ConfigEditorResult updateEnrichmentTableInternally(String serviceName, EnrichmentTableDto enrichmentTable,
                                                               boolean isNewTable) {
        if (enrichmentTable.getName() == null || enrichmentTable.getPath() == null) {
            return ConfigEditorResult.fromMessage(BAD_REQUEST, WRONG_TABLE_TO_UPDATE);
        }

        try {
            EnrichmentTablesUpdateDto currentTables = ENRICHMENT_TABLES_UPDATE_MSG_READER
                    .readValue(enrichmentTablesCache.get(serviceName).get());
            int selected = -1;
            for (int i = 0; i < currentTables.getEnrichmentTables().size(); i++) {
                if (enrichmentTable.getName().equals(currentTables.getEnrichmentTables().get(i).getName())) {
                    selected = i;
                    break;
                }
            }

            if (selected != -1) {
                if (isNewTable) {
                    return ConfigEditorResult.fromMessage(BAD_REQUEST,
                            String.format(ADD_NEW_EXISTING_TABLE_MSG, enrichmentTable.getName()));
                }

                currentTables.getEnrichmentTables().set(selected, enrichmentTable);
            } else {
                if (!isNewTable) {
                    return ConfigEditorResult.fromMessage(BAD_REQUEST,
                            String.format(UPDATE_NON_EXISTING_TABLE_MSG, enrichmentTable.getName()));
                }

                currentTables.getEnrichmentTables().add(enrichmentTable);
            }

            String updatedTablesStr = ENRICHMENT_TABLES_UPDATE_MSG_WRITER.writeValueAsString(currentTables);
            zooKeeperConnectorMap.get(serviceName).setData(updatedTablesStr);
            enrichmentTablesCache.get(serviceName).set(updatedTablesStr);
            return ConfigEditorResult.fromEnrichmentTables(currentTables.getEnrichmentTables());
        } catch (Exception e) {
            return ConfigEditorResult.fromException(e);
        }
    }
}
