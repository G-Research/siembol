package uk.co.gresearch.siembol.enrichments.storm;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.common.filesystem.SiembolFileSystem;
import uk.co.gresearch.siembol.common.filesystem.SiembolFileSystemFactory;
import uk.co.gresearch.siembol.common.filesystem.SupportedFileSystem;
import uk.co.gresearch.siembol.common.metrics.SiembolMetrics;
import uk.co.gresearch.siembol.common.metrics.SiembolMetricsRegistrar;
import uk.co.gresearch.siembol.common.metrics.storm.StormMetricsRegistrar;
import uk.co.gresearch.siembol.common.metrics.storm.StormMetricsRegistrarFactory;
import uk.co.gresearch.siembol.common.metrics.storm.StormMetricsRegistrarFactoryImpl;
import uk.co.gresearch.siembol.common.model.EnrichmentTableDto;
import uk.co.gresearch.siembol.common.model.EnrichmentTablesUpdateDto;
import uk.co.gresearch.siembol.common.model.StormEnrichmentAttributesDto;
import uk.co.gresearch.siembol.common.model.ZooKeeperAttributesDto;
import uk.co.gresearch.siembol.common.storm.SiembolMetricsCounters;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnectorFactory;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnector;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnectorFactoryImpl;
import uk.co.gresearch.siembol.enrichments.common.EnrichmentCommand;
import uk.co.gresearch.siembol.enrichments.storm.common.*;
import uk.co.gresearch.siembol.enrichments.table.EnrichmentMemoryTable;
import uk.co.gresearch.siembol.enrichments.table.EnrichmentTable;

import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryTableEnrichmentBolt extends BaseRichBolt {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final ObjectReader TABLES_UPDATE_READER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .readerFor(EnrichmentTablesUpdateDto.class);

    private static final String TABLES_INIT_START = "Initialisation of enrichment tables started";
    private static final String TABLES_INIT_COMPLETED = "Initialisation of enrichment tables completed";
    private static final String TABLES_UPDATES_START = "Updating enrichment tables";
    private static final String TABLES_UPDATES_COMPLETED = "Updating enrichment tables completed";
    private static final String TABLES_UPDATE_MESSAGE_FORMAT = "Updating enrichment tables: %s";
    private static final String TABLES_UPDATE_EXCEPTION_FORMAT = "Exception during update of enrichment tables: {}";
    private static final String TABLE_UPDATE_EXCEPTION_FORMAT = "Exception during update of an enrichment table: {} " +
            "path: {}, exception : {}";
    private static final String TABLE_INIT_START = "Trying to initialise enrichment table: {} from the file: {}";
    private static final String TABLE_INIT_COMPLETED = "Initialisation of enrichment table: {} completed";
    private static final String TABLES_UPDATE_EMPTY_TABLES = "No enrichment tables provided";
    private static final String INIT_EXCEPTION_MSG_FORMAT = "Exception during loading memory table: %s";
    private static final String INVALID_TYPE_IN_TUPLES = "Invalid type in tuple provided";

    private final ConcurrentHashMap<String, Pair<String, EnrichmentTable>> enrichmentTables = new ConcurrentHashMap<>();
    private final ZooKeeperAttributesDto zooKeeperAttributesDto;
    private final ZooKeeperConnectorFactory zooKeeperConnectorFactory;
    private final SiembolFileSystemFactory fileSystemFactory;
    private final StormMetricsRegistrarFactory metricsFactory;

    private OutputCollector collector;
    private ZooKeeperConnector zooKeeperConnector;
    private SiembolMetricsRegistrar metricsRegistrar;

    MemoryTableEnrichmentBolt(StormEnrichmentAttributesDto attributes,
                              ZooKeeperConnectorFactory zooKeeperConnectorFactory,
                              SiembolFileSystemFactory fileSystemFactory,
                              StormMetricsRegistrarFactory metricsFactory) {
        this.zooKeeperAttributesDto = attributes.getEnrichingTablesAttributes();
        this.zooKeeperConnectorFactory = zooKeeperConnectorFactory;
        this.fileSystemFactory = fileSystemFactory;
        this.metricsFactory = metricsFactory;
    }

    public MemoryTableEnrichmentBolt(StormEnrichmentAttributesDto attributes) {
        this(attributes,
                new ZooKeeperConnectorFactoryImpl(),
                SupportedFileSystem.fromUri(attributes.getEnrichingTablesUri()),
                new StormMetricsRegistrarFactoryImpl());
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.collector = outputCollector;

        try {
            LOG.info(TABLES_INIT_START);
            zooKeeperConnector = zooKeeperConnectorFactory.createZookeeperConnector(zooKeeperAttributesDto);
            metricsRegistrar = metricsFactory.createSiembolMetricsRegistrar(topologyContext);

            updateTables();
            if (enrichmentTables.isEmpty()) {
                LOG.warn(TABLES_UPDATE_EMPTY_TABLES);
            }

            zooKeeperConnector.addCacheListener(this::updateTables);
            LOG.info(TABLES_INIT_COMPLETED);
        } catch (Exception e) {
            String msg = String.format(INIT_EXCEPTION_MSG_FORMAT, ExceptionUtils.getStackTrace(e));
            LOG.error(msg);
            throw new IllegalStateException(msg);
        }
    }

    private void updateTables() {
        try {
            LOG.info(TABLES_UPDATES_START);

            String tablesUpdateStr = zooKeeperConnector.getData();
            LOG.info(String.format(TABLES_UPDATE_MESSAGE_FORMAT, tablesUpdateStr));
            EnrichmentTablesUpdateDto enrichmentTablesUpdateDto = TABLES_UPDATE_READER.readValue(tablesUpdateStr);
            try (SiembolFileSystem fs = fileSystemFactory.create()) {
                for (EnrichmentTableDto table : enrichmentTablesUpdateDto.getEnrichmentTables()) {
                    var currentTablePair = enrichmentTables.get(table.getName());
                    if (currentTablePair != null && currentTablePair.getLeft().equals(table.getPath())) {
                        //NOTE: we already have a table loaded from the same path
                        continue;
                    }

                    LOG.info(TABLE_INIT_START, table.getName(), table.getPath());
                    try (InputStream is = fs.openInputStream(table.getPath())) {
                        var memoryTable = EnrichmentMemoryTable.fromJsonStream(is);
                        enrichmentTables.put(table.getName(), ImmutablePair.of(table.getPath(), memoryTable));
                        LOG.info(TABLE_INIT_COMPLETED, table.getName());
                        metricsRegistrar
                                .registerCounter(SiembolMetrics.ENRICHMENT_TABLE_UPDATED.getMetricName(table.getName()))
                                .increment();
                    } catch (Exception e) {
                        LOG.error(TABLE_UPDATE_EXCEPTION_FORMAT,
                                table.getName(),
                                table.getPath(),
                                ExceptionUtils.getStackTrace(e));
                        metricsRegistrar.registerCounter(
                                SiembolMetrics.ENRICHMENT_TABLE_UPDATE_ERROR.getMetricName(table.getName()))
                                .increment();
                    }
                }
            }
            LOG.info(TABLES_UPDATES_COMPLETED);
        } catch (Exception e) {
            LOG.error(TABLES_UPDATE_EXCEPTION_FORMAT, ExceptionUtils.getStackTrace(e));
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void execute(Tuple tuple) {
        String event = tuple.getStringByField(EnrichmentTuples.EVENT.toString());

        Object commandsObj = tuple.getValueByField(EnrichmentTuples.COMMANDS.toString());
        if (!(commandsObj instanceof EnrichmentCommands)) {
            LOG.error(INVALID_TYPE_IN_TUPLES);
            throw new IllegalArgumentException(INVALID_TYPE_IN_TUPLES);
        }
        EnrichmentCommands commands = (EnrichmentCommands)commandsObj;

        Object exceptionsObj = tuple.getValueByField(EnrichmentTuples.EXCEPTIONS.toString());
        if (!(exceptionsObj instanceof EnrichmentExceptions)) {
            LOG.error(INVALID_TYPE_IN_TUPLES);
            throw new IllegalArgumentException(INVALID_TYPE_IN_TUPLES);
        }
        EnrichmentExceptions exceptions = (EnrichmentExceptions)exceptionsObj;

        EnrichmentPairs enrichments = new EnrichmentPairs();
        SiembolMetricsCounters counters = new SiembolMetricsCounters();

        for (EnrichmentCommand command : commands) {
            var tablePair = enrichmentTables.get(command.getTableName());
            if (tablePair == null) {
                continue;
            }

            Optional<List<Pair<String, String>>> result = tablePair.getRight().getValues(command);
            if (result.isPresent()) {
                enrichments.addAll(result.get());
                counters.add(SiembolMetrics.ENRICHMENT_RULE_APPLIED.getMetricName(command.getRuleName()));
                counters.add(SiembolMetrics.ENRICHMENT_TABLE_APPLIED.getMetricName(command.getTableName()));
            }

        }
        collector.emit(tuple, new Values(event, enrichments, exceptions, counters));
        collector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields(EnrichmentTuples.EVENT.toString(),
                EnrichmentTuples.ENRICHMENTS.toString(),
                EnrichmentTuples.EXCEPTIONS.toString(),
                EnrichmentTuples.COUNTERS.toString()));
    }
}
