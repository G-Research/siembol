package uk.co.gresearch.nortem.parsers.storm;

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
import uk.co.gresearch.nortem.common.utils.ZookeperConnector;
import uk.co.gresearch.nortem.parsers.application.factory.ParsingApplicationFactory;
import uk.co.gresearch.nortem.parsers.application.factory.ParsingApplicationFactoryAttributes;
import uk.co.gresearch.nortem.parsers.application.factory.ParsingApplicationFactoryImpl;
import uk.co.gresearch.nortem.parsers.application.factory.ParsingApplicationFactoryResult;
import uk.co.gresearch.nortem.parsers.application.parsing.ParsingApplicationParser;
import uk.co.gresearch.nortem.parsers.application.parsing.ParsingApplicationResult;


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

    private final AtomicReference<ParsingApplicationParser> parsingApplicationParser = new AtomicReference<>();
    private OutputCollector collector;
    private ZookeperConnector zookeperConnector;
    private final StormParsingApplicationAttributes stormAttributes;
    private final String parsingAppSpecification;

    public ParsingApplicationBolt(StormParsingApplicationAttributes attributes,
                                  ParsingApplicationFactoryAttributes parsingAttributes) throws Exception {
        this.stormAttributes = attributes;
        this.parsingAppSpecification = parsingAttributes.getApplicationParserSpecification();
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.collector = outputCollector;
        try {
            LOG.info(INIT_START);

            zookeperConnector = new ZookeperConnector.Builder()
                    .zkServer(stormAttributes.getZkUrl())
                    .path(stormAttributes.getZkPathParserConfigs())
                    .baseSleepTimeMs(stormAttributes.getZkBaseSleepMs())
                    .maxRetries(stormAttributes.getZkMaxRetries())
                    .build();

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
        byte[] log = (byte[])tuple.getValueByField(ParsingApplicationTuples.LOG.toString());

        ArrayList<ParsingApplicationResult> results = currentParser.parse(log, metadata);
        if (!results.isEmpty()) {
            collector.emit(tuple, new Values(results));
        }

        collector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields(ParsingApplicationTuples.PARSING_RESULTS.toString()));
    }
}
