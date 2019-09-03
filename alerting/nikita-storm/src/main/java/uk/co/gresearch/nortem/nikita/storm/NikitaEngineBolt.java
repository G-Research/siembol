package uk.co.gresearch.nortem.nikita.storm;

import repackaged.com.fasterxml.jackson.core.JsonProcessingException;
import repackaged.com.fasterxml.jackson.core.type.TypeReference;
import repackaged.com.fasterxml.jackson.databind.ObjectMapper;
import repackaged.com.fasterxml.jackson.databind.ObjectWriter;
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
import uk.co.gresearch.nortem.nikita.common.EvaluationResult;
import uk.co.gresearch.nortem.nikita.common.NikitaEngine;
import uk.co.gresearch.nortem.nikita.common.NikitaResult;
import uk.co.gresearch.nortem.nikita.compiler.NikitaRulesCompiler;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class NikitaEngineBolt extends BaseRichBolt {
    private static final String EXCEPTION_MSG_FORMAT = "Nikita Engine exception: %s during evaluating event: %s";
    private static final String INIT_EXCEPTION_MSG_FORMAT = "Nikita Engine exception: %s during initialising nikita engine";
    protected static final String COMPILER_EXCEPTION_MSG_FORMAT = "Exception during nikita rules compilation: %s";
    private static final String UPDATE_EXCEPTION_LOG = "Exception during nikita rules update: {}";
    private static final String ENGINE_INIT_MESSAGE = "Nikita Engine exception: Engine initialisation error";
    private static final String ENGINE_INIT_START = "Nikita Engine initialisation start";
    private static final String ENGINE_INIT_COMPLETED = "Nikita Engine initialisation completed";
    private static final String ENGINE_UPDATE_START = "Nikita Engine update start";
    private static final String ENGINE_UPDATE_COMPLETED = "Nikita Engine update completed";
    private static final String ENGINE_UPDATE_TRY_MSG_FORMAT = "Nikita is trying to update the rules: %s";
    private static final String ACK_NO_MATCH_LOG = "Ack-ing event {}, since no further processing required";
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final ObjectWriter JSON_WRITER = new ObjectMapper()
            .writerFor(new TypeReference<Map<String, Object>>() { });

    protected final AtomicReference<NikitaEngine> nikitaEngine = new AtomicReference<>();
    private OutputCollector collector;
    private ZookeperConnector zookeperConnector;
    private final NikitaStormAttributes attributes;

    public NikitaEngineBolt(NikitaStormAttributes attributes) {
        this.attributes = attributes;
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.collector = outputCollector;
        try {
            LOG.info(ENGINE_INIT_START);
            zookeperConnector = new ZookeperConnector.Builder()
                    .zkServer(attributes.getZkUrl())
                    .path(attributes.getZkPathNikitaRules())
                    .baseSleepTimeMs(attributes.getZkBaseSleepMs())
                    .maxRetries(attributes.getZkMaxRetries())
                    .build();

            updateRules();
            if (nikitaEngine.get() == null) {
                throw new IllegalStateException(ENGINE_INIT_MESSAGE);
            }

            zookeperConnector.addCacheListener(this::updateRules);
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

            String rules = zookeperConnector.getData();
            LOG.info(String.format(ENGINE_UPDATE_TRY_MSG_FORMAT, rules));

            NikitaEngine engine = getNikitaEngine(rules);
            nikitaEngine.set(engine);

            LOG.info(ENGINE_UPDATE_COMPLETED);
        } catch (Exception e) {
            LOG.error(UPDATE_EXCEPTION_LOG, ExceptionUtils.getStackTrace(e));
            return;
        }
    }

    protected NikitaEngine getNikitaEngine(String rules) {
        try {
            NikitaResult engineResult =  NikitaRulesCompiler.createNikitaRulesCompiler().compile(rules);
            if (engineResult.getStatusCode() != NikitaResult.StatusCode.OK) {
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
        NikitaEngine currentEngine = nikitaEngine.get();
        String event = tuple.getStringByField(TupleFieldNames.EVENT.toString());
        NikitaResult ret = currentEngine.evaluate(event);

        if (ret.getStatusCode() == NikitaResult.StatusCode.OK
                && ret.getAttributes().getEvaluationResult() == EvaluationResult.NO_MATCH
                && ret.getAttributes().getExceptionEvents() == null) {
            LOG.debug(ACK_NO_MATCH_LOG, event);
            collector.ack(tuple);
            return;
        }

        ArrayList<NikitaAlert> matches = new ArrayList<>();
        ArrayList<String> exceptions = new ArrayList<>();

        if (ret.getStatusCode() != NikitaResult.StatusCode.OK) {
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
                            matches.add(new NikitaAlert(x, JSON_WRITER.writeValueAsString(x)));
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
        declarer.declare(new Fields(TupleFieldNames.NIKITA_MATCHES.toString(),
                TupleFieldNames.NIKITA_EXCEPTIONS.toString()));
    }
}
