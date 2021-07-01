package uk.co.gresearch.siembol.alerts.storm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
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
import uk.co.gresearch.siembol.common.model.ZooKeeperAttributesDto;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnector;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnectorFactory;
import uk.co.gresearch.siembol.alerts.common.EvaluationResult;
import uk.co.gresearch.siembol.alerts.common.AlertingEngine;
import uk.co.gresearch.siembol.alerts.common.AlertingResult;
import uk.co.gresearch.siembol.alerts.compiler.AlertingRulesCompiler;
import uk.co.gresearch.siembol.alerts.storm.model.AlertMessage;
import uk.co.gresearch.siembol.alerts.storm.model.AlertMessages;
import uk.co.gresearch.siembol.alerts.storm.model.ExceptionMessages;
import uk.co.gresearch.siembol.common.model.AlertingStormAttributesDto;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnectorFactoryImpl;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class AlertingEngineBolt extends BaseRichBolt {
    private static final long serialVersionUID = 1L;
    private static final String EXCEPTION_MSG_FORMAT = "Alerting Engine exception: %s during evaluating event: %s";
    private static final String INIT_EXCEPTION_MSG_FORMAT = "Alerting Engine exception: %s during initialising alerts engine";
    private static final String UPDATE_EXCEPTION_LOG = "Exception during alerts rules update: {}";
    private static final String ENGINE_INIT_MESSAGE = "Alerting Engine exception: Engine initialisation error";
    private static final String ENGINE_INIT_START = "Alerting Engine initialisation start";
    private static final String ENGINE_INIT_COMPLETED = "Alerting Engine initialisation completed";
    private static final String ENGINE_UPDATE_START = "Alerting Engine update start";
    private static final String ENGINE_UPDATE_COMPLETED = "Alerting Engine update completed";
    private static final String ENGINE_UPDATE_TRY_MSG_FORMAT = "Alerting Engine is trying to update the rules: %s";
    private static final String ACK_NO_MATCH_LOG = "Ack-ing event {}, since no further processing required";
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final ObjectWriter JSON_WRITER = new ObjectMapper()
            .writerFor(new TypeReference<Map<String, Object>>() { });

    protected static final String COMPILER_EXCEPTION_MSG_FORMAT = "Exception during alerting rules compilation: %s";
    protected final AtomicReference<AlertingEngine> AlertingEngine = new AtomicReference<>();

    private OutputCollector collector;
    private ZooKeeperConnector zooKeeperConnector;
    private final ZooKeeperConnectorFactory zooKeeperConnectorFactory;
    private final ZooKeeperAttributesDto zookeperAttributes;

    AlertingEngineBolt(AlertingStormAttributesDto attributes, ZooKeeperConnectorFactory zooKeeperConnectorFactory) {
        this.zookeperAttributes = attributes.getZookeperAttributes();
        this.zooKeeperConnectorFactory = zooKeeperConnectorFactory;
    }

    AlertingEngineBolt(AlertingStormAttributesDto attributes) {
        this(attributes, new ZooKeeperConnectorFactoryImpl());
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.collector = outputCollector;
        try {
            LOG.info(ENGINE_INIT_START);
            zooKeeperConnector = zooKeeperConnectorFactory.createZookeeperConnector(zookeperAttributes);

            updateRules();
            if (AlertingEngine.get() == null) {
                throw new IllegalStateException(ENGINE_INIT_MESSAGE);
            }

            zooKeeperConnector.addCacheListener(this::updateRules);
            LOG.info(ENGINE_INIT_COMPLETED);
        } catch (Exception e) {
            String msg = String.format(INIT_EXCEPTION_MSG_FORMAT, ExceptionUtils.getStackTrace(e));
            LOG.error(msg);
            throw new IllegalStateException(msg);
        }
    }

    private void updateRules() {
        try {
            LOG.info(ENGINE_UPDATE_START);

            String rules = zooKeeperConnector.getData();
            LOG.info(String.format(ENGINE_UPDATE_TRY_MSG_FORMAT, rules));

            AlertingEngine engine = getAlertingEngine(rules);
            AlertingEngine.set(engine);

            LOG.info(ENGINE_UPDATE_COMPLETED);
        } catch (Exception e) {
            LOG.error(UPDATE_EXCEPTION_LOG, ExceptionUtils.getStackTrace(e));
            return;
        }
    }

    protected AlertingEngine getAlertingEngine(String rules) {
        try {
            AlertingResult engineResult =  AlertingRulesCompiler.createAlertingRulesCompiler().compile(rules);
            if (engineResult.getStatusCode() != AlertingResult.StatusCode.OK) {
                String errorMsg = String.format(COMPILER_EXCEPTION_MSG_FORMAT,
                        engineResult.getAttributes().getException());
                LOG.error(errorMsg);
                throw new IllegalStateException(errorMsg);
            }
            return engineResult.getAttributes().getEngine();
        } catch (Exception e) {
            String errorMsg = String.format(COMPILER_EXCEPTION_MSG_FORMAT,
                    ExceptionUtils.getStackTrace(e));
            LOG.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }
    }

    @Override
    public void execute(Tuple tuple) {
        AlertingEngine currentEngine = AlertingEngine.get();
        String event = tuple.getStringByField(TupleFieldNames.EVENT.toString());
        AlertingResult ret = currentEngine.evaluate(event);

        if (ret.getStatusCode() == AlertingResult.StatusCode.OK
                && ret.getAttributes().getEvaluationResult() == EvaluationResult.NO_MATCH
                && ret.getAttributes().getExceptionEvents() == null) {
            LOG.debug(ACK_NO_MATCH_LOG, event);
            collector.ack(tuple);
            return;
        }

        AlertMessages matches = new AlertMessages();
        ExceptionMessages exceptions = new ExceptionMessages();

        if (ret.getStatusCode() != AlertingResult.StatusCode.OK) {
            exceptions.add(String.format(EXCEPTION_MSG_FORMAT,
                    ret.getAttributes().getException(),
                    event));
        }

        if (ret.getAttributes().getExceptionEvents() != null) {
            ret.getAttributes().getExceptionEvents()
                    .forEach( x -> {
                        try {
                            exceptions.add(JSON_WRITER.writeValueAsString(x));
                        } catch (JsonProcessingException e) {
                            exceptions.add(ExceptionUtils.getStackTrace(e));
                        }
                    });
        }

        if (ret.getAttributes().getOutputEvents() != null) {
            ret.getAttributes().getOutputEvents()
                    .forEach( x -> {
                        try {
                            matches.add(new AlertMessage(currentEngine.getAlertingEngineType(),
                                    x,
                                    JSON_WRITER.writeValueAsString(x)));
                        } catch (Exception e) {
                            exceptions.add(ExceptionUtils.getStackTrace(e));
                        }
                    });
        }

        collector.emit(tuple, new Values(matches, exceptions));
        collector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields(TupleFieldNames.ALERTING_MATCHES.toString(),
                TupleFieldNames.ALERTING_EXCEPTIONS.toString()));
    }

}
