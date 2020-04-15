package uk.co.gresearch.siembol.parsers.storm;

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
import uk.co.gresearch.siembol.common.storm.KafkaBatchWriterMessage;
import uk.co.gresearch.siembol.common.storm.KafkaBatchWriterMessages;
import uk.co.gresearch.siembol.common.zookeper.ZookeperAttributes;
import uk.co.gresearch.siembol.common.zookeper.ZookeperConnector;
import uk.co.gresearch.siembol.common.zookeper.ZookeperConnectorFactory;
import uk.co.gresearch.siembol.parsers.application.factory.ParsingApplicationFactory;
import uk.co.gresearch.siembol.parsers.application.factory.ParsingApplicationFactoryAttributes;
import uk.co.gresearch.siembol.parsers.application.factory.ParsingApplicationFactoryImpl;
import uk.co.gresearch.siembol.parsers.application.factory.ParsingApplicationFactoryResult;
import uk.co.gresearch.siembol.parsers.application.parsing.ParsingApplicationParser;
import uk.co.gresearch.siembol.parsers.application.parsing.ParsingApplicationResult;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class ParsingApplicationBolt extends BaseRichBolt {
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
    private final ZookeperAttributes zookeperAttributes;
    private final String parsingAppSpecification;

    private OutputCollector collector;
    private ZookeperConnector zookeperConnector;
    private final ZookeperConnectorFactory zookeperConnectorFactory;

    ParsingApplicationBolt(StormParsingApplicationAttributes attributes,
                           ParsingApplicationFactoryAttributes parsingAttributes,
                           ZookeperConnectorFactory zookeperConnectorFactory) throws Exception {
        this.zookeperAttributes = attributes.getZookeperAttributes();
        this.parsingAppSpecification = parsingAttributes.getApplicationParserSpecification();
        this.zookeperConnectorFactory = zookeperConnectorFactory;
    }

    public ParsingApplicationBolt(StormParsingApplicationAttributes attributes,
                                  ParsingApplicationFactoryAttributes parsingAttributes) throws Exception {
        this(attributes, parsingAttributes, new ZookeperConnectorFactory() {});
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.collector = outputCollector;
        try {
            LOG.info(INIT_START);
            zookeperConnector = zookeperConnectorFactory.createZookeperConnector(zookeperAttributes);

            updateParsers();
            if (parsingApplicationParser.get() == null) {
                throw new IllegalStateException(ERROR_INIT_MESSAGE);
            }
            zookeperConnector.addCacheListener(this::updateParsers);
            LOG.info(INIT_COMPLETED);
        } catch (Exception e) {
            String msg = String.format(INIT_EXCEPTION_MSG_FORMAT, ExceptionUtils.getStackTrace(e));
            LOG.error(msg);
            throw new IllegalStateException(msg);
        }
    }

    private void updateParsers() {
        try {
            ParsingApplicationFactory factory =  new ParsingApplicationFactoryImpl();

            LOG.info(PARSERS_UPDATE_START);
            String parserConfigs = zookeperConnector.getData();
            LOG.info(String.format(PARSERCONFIG_UPDATE_TRY_MSG_FORMAT, parsingAppSpecification, parserConfigs));
            ParsingApplicationFactoryResult result = factory.create(parsingAppSpecification, parserConfigs);
            if (result.getStatusCode() != ParsingApplicationFactoryResult.StatusCode.OK) {
                String errorMsg = String.format(FACTORY_EXCEPTION_MSG_FORMAT,
                        result.getAttributes().getMessage());
                LOG.error(errorMsg);
                throw new IllegalStateException(errorMsg);
            }

            parsingApplicationParser.set(result.getAttributes().getApplicationParser());

            LOG.info(PARSERS_UPDATE_COMPLETED);
        } catch (Exception e) {
            LOG.error(UPDATE_EXCEPTION_LOG, ExceptionUtils.getStackTrace(e));
            return;
        }
    }

    @Override
    public void execute(Tuple tuple) {
        ParsingApplicationParser currentParser = parsingApplicationParser.get();

        String metadata = tuple.getStringByField(ParsingApplicationTuples.METADATA.toString());
        Object logObj = tuple.getValueByField(ParsingApplicationTuples.LOG.toString());
        if (!(logObj instanceof byte[])) {
            throw new IllegalArgumentException(INVALID_TYPE_IN_TUPLE);
        }

        byte[] log = (byte[])logObj;
        ArrayList<ParsingApplicationResult> results = currentParser.parse(metadata, log);
        if (!results.isEmpty()) {
            KafkaBatchWriterMessages kafkaBatchWriterMessages = new KafkaBatchWriterMessages();
            results.forEach(x -> x.getMessages().forEach(y ->
                    kafkaBatchWriterMessages.add(new KafkaBatchWriterMessage(x.getTopic(), y))));
            collector.emit(tuple, new Values(kafkaBatchWriterMessages));
        }

        collector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields(ParsingApplicationTuples.PARSING_MESSAGES.toString()));
    }
}
