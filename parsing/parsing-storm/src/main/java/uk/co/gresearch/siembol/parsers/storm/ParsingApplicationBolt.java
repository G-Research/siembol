package uk.co.gresearch.siembol.parsers.storm;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.common.constants.SiembolConstants;
import uk.co.gresearch.siembol.common.metrics.SiembolMetrics;
import uk.co.gresearch.siembol.common.metrics.SiembolMetricsRegistrar;
import uk.co.gresearch.siembol.common.metrics.storm.StormMetricsRegistrarFactory;
import uk.co.gresearch.siembol.common.metrics.storm.StormMetricsRegistrarFactoryImpl;
import uk.co.gresearch.siembol.common.model.StormParsingApplicationAttributesDto;
import uk.co.gresearch.siembol.common.storm.KafkaWriterMessage;
import uk.co.gresearch.siembol.common.storm.KafkaWriterMessages;
import uk.co.gresearch.siembol.common.model.ZooKeeperAttributesDto;
import uk.co.gresearch.siembol.common.storm.SiembolMetricsCounters;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnector;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnectorFactory;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnectorFactoryImpl;
import uk.co.gresearch.siembol.parsers.application.factory.ParsingApplicationFactory;
import uk.co.gresearch.siembol.parsers.application.factory.ParsingApplicationFactoryAttributes;
import uk.co.gresearch.siembol.parsers.application.factory.ParsingApplicationFactoryImpl;
import uk.co.gresearch.siembol.parsers.application.factory.ParsingApplicationFactoryResult;
import uk.co.gresearch.siembol.parsers.application.parsing.ParsingApplicationParser;
import uk.co.gresearch.siembol.parsers.application.parsing.ParsingApplicationResult;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
/**
 * An object for integration of a parsing application into a storm bolt
 *
 * <p>This class extends a Storm BaseRichBolt class to implement a Storm bolt, that
 *  parses logs using a parsing application parser initialised from the parser configurations cached in the ZooKeeper,
 *  watches for the parser configurations update in ZooKeeper and
 *  updates the parsers without needing to restart the topology or the bolt,
 *  emits parsed messages and exceptions after parsing.
 *
 * @author Marian Novotny
 * @see ParsingApplicationParser
 * @see ZooKeeperConnector
 *
 */
public class ParsingApplicationBolt extends BaseRichBolt {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String PARSERCONFIG_UPDATE_TRY_MSG_FORMAT = "Trying to update parsing app: %s, " +
            "by parser configs: %s, ";
    private static final String INIT_EXCEPTION_MSG_FORMAT = "Parsing application exception: %s during initialising";
    private static final String FACTORY_EXCEPTION_MSG_FORMAT = "Exception during creation of parsing application: %s";
    private static final String UPDATE_EXCEPTION_LOG = "Exception during parserconfig update: {}";
    private static final String ERROR_INIT_MESSAGE = "Parsing application exception: Parsing app initialisation error";
    private static final String INIT_START = "Parsing application initialisation start";
    private static final String INIT_COMPLETED = "Parsing application initialisation completed";
    private static final String PARSERS_UPDATE_START = "Parser config update start";
    private static final String PARSERS_UPDATE_COMPLETED = "Parser config update completed";
    private static final String INVALID_TYPE_IN_TUPLE = "Invalid type in tuple";

    private final AtomicReference<ParsingApplicationParser> parsingApplicationParser = new AtomicReference<>();
    private final ZooKeeperAttributesDto zooKeeperAttributes;
    private final String parsingAppSpecification;


    private OutputCollector collector;
    private ZooKeeperConnector zooKeeperConnector;
    private SiembolMetricsRegistrar metricsRegistrar;
    private final ZooKeeperConnectorFactory zooKeeperConnectorFactory;
    private final StormMetricsRegistrarFactory metricsFactory;

    ParsingApplicationBolt(StormParsingApplicationAttributesDto attributes,
                           ParsingApplicationFactoryAttributes parsingAttributes,
                           ZooKeeperConnectorFactory zooKeeperConnectorFactory,
                           StormMetricsRegistrarFactory metricsFactory) {
        this.zooKeeperAttributes = attributes.getZookeeperAttributes();
        this.parsingAppSpecification = parsingAttributes.getApplicationParserSpecification();
        this.zooKeeperConnectorFactory = zooKeeperConnectorFactory;
        this.metricsFactory = metricsFactory;
    }

    public ParsingApplicationBolt(StormParsingApplicationAttributesDto attributes,
                                  ParsingApplicationFactoryAttributes parsingAttributes) {
        this(attributes,
                parsingAttributes,
                new ZooKeeperConnectorFactoryImpl(),
                new StormMetricsRegistrarFactoryImpl());
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.collector = outputCollector;
        try {
            LOG.info(INIT_START);
            zooKeeperConnector = zooKeeperConnectorFactory.createZookeeperConnector(zooKeeperAttributes);
            metricsRegistrar = metricsFactory.createSiembolMetricsRegistrar(topologyContext);

            updateParsers();
            if (parsingApplicationParser.get() == null) {
                throw new IllegalStateException(ERROR_INIT_MESSAGE);
            }

            zooKeeperConnector.addCacheListener(this::updateParsers);
            LOG.info(INIT_COMPLETED);
        } catch (Exception e) {
            String msg = String.format(INIT_EXCEPTION_MSG_FORMAT, ExceptionUtils.getStackTrace(e));
            LOG.error(msg);
            throw new IllegalStateException(msg);
        }
    }

    private void updateParsers() {
        try {
            ParsingApplicationFactory factory = new ParsingApplicationFactoryImpl();

            LOG.info(PARSERS_UPDATE_START);
            String parserConfigs = zooKeeperConnector.getData();
            LOG.info(String.format(PARSERCONFIG_UPDATE_TRY_MSG_FORMAT,
                    parsingAppSpecification,
                    StringUtils.left(parserConfigs, SiembolConstants.MAX_SIZE_CONFIG_UPDATE_LOG)));
            ParsingApplicationFactoryResult result = factory.create(parsingAppSpecification, parserConfigs);
            if (result.getStatusCode() != ParsingApplicationFactoryResult.StatusCode.OK) {
                String errorMsg = String.format(FACTORY_EXCEPTION_MSG_FORMAT,
                        result.getAttributes().getMessage());
                LOG.error(errorMsg);
                throw new IllegalStateException(errorMsg);
            }

            metricsRegistrar.registerCounter(SiembolMetrics.PARSING_CONFIGS_UPDATE.getMetricName()).increment();
            parsingApplicationParser.set(result.getAttributes().getApplicationParser());
            LOG.info(PARSERS_UPDATE_COMPLETED);
        } catch (Exception e) {
            LOG.error(UPDATE_EXCEPTION_LOG, ExceptionUtils.getStackTrace(e));
            metricsRegistrar.registerCounter(SiembolMetrics.PARSING_CONFIGS_ERROR_UPDATE.getMetricName()).increment();
        }
    }

    @Override
    public void execute(Tuple tuple) {
        ParsingApplicationParser currentParser = parsingApplicationParser.get();

        String source = tuple.getStringByField(ParsingApplicationTuples.SOURCE.toString());
        String metadata = tuple.getStringByField(ParsingApplicationTuples.METADATA.toString());
        Object logObj = tuple.getValueByField(ParsingApplicationTuples.LOG.toString());
        if (!(logObj instanceof byte[])) {
            throw new IllegalArgumentException(INVALID_TYPE_IN_TUPLE);
        }

        byte[] log = (byte[]) logObj;
        ArrayList<ParsingApplicationResult> results = currentParser.parse(source, metadata, log);

        var kafkaWriterMessages = new KafkaWriterMessages();
        var counters = new SiembolMetricsCounters();
        for (var result : results) {
            if (result.getResultFlags().contains(ParsingApplicationResult.ResultFlag.FILTERED)) {
                metricsRegistrar.registerCounter(
                                SiembolMetrics.PARSING_SOURCE_TYPE_FILTERED_MESSAGES.getMetricName(result.getSourceType()))
                        .increment();
                metricsRegistrar.registerCounter(SiembolMetrics.PARSING_APP_FILTERED_MESSAGES.getMetricName())
                        .increment();
            } else {
                result.getMessages().forEach(x -> {
                    kafkaWriterMessages.add(new KafkaWriterMessage(result.getTopic(), x));
                    addCounters(counters, result.getResultFlags(), result.getSourceType());
                });
            }
        }

        if (!kafkaWriterMessages.isEmpty()) {
            collector.emit(tuple, new Values(kafkaWriterMessages, counters));
        }

        collector.ack(tuple);
    }

    private void addCounters(SiembolMetricsCounters counters,
                             EnumSet<ParsingApplicationResult.ResultFlag> resultFlags,
                             String sourceType) {
        if (resultFlags.contains(ParsingApplicationResult.ResultFlag.PARSED)) {
            counters.add(SiembolMetrics.PARSING_SOURCE_TYPE_PARSED_MESSAGES
                    .getMetricName(sourceType));
            counters.add(SiembolMetrics.PARSING_APP_PARSED_MESSAGES.getMetricName());
        }

        if (resultFlags.contains(ParsingApplicationResult.ResultFlag.ERROR)) {
            counters.add(SiembolMetrics.PARSING_APP_ERROR_MESSAGES.getMetricName());
        }

        if (resultFlags.contains(ParsingApplicationResult.ResultFlag.REMOVED_FIELDS)) {
            counters.add(SiembolMetrics.PARSING_SOURCE_TYPE_REMOVED_FIELDS_MESSAGES.getMetricName(sourceType));
        }

        if (resultFlags.contains(ParsingApplicationResult.ResultFlag.TRUNCATED_FIELDS)) {
            counters.add(SiembolMetrics.PARSING_SOURCE_TYPE_TRUNCATED_FIELDS_MESSAGES
                    .getMetricName(sourceType));
        }

        if (resultFlags.contains(ParsingApplicationResult.ResultFlag.TRUNCATED_ORIGINAL_STRING)) {
            counters.add(SiembolMetrics.PARSING_SOURCE_TYPE_TRUNCATED_ORIGINAL_STRING_MESSAGES
                    .getMetricName(sourceType));
        }

        if (resultFlags.contains(ParsingApplicationResult.ResultFlag.ORIGINAL_MESSAGE)) {
            counters.add(SiembolMetrics.PARSING_SOURCE_TYPE_SENT_ORIGINAL_STRING_MESSAGES
                    .getMetricName(sourceType));
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields(ParsingApplicationTuples.PARSING_MESSAGES.toString(),
                ParsingApplicationTuples.COUNTERS.toString()));
    }
}
