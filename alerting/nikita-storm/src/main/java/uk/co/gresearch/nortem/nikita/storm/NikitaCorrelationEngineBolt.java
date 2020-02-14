package uk.co.gresearch.nortem.nikita.storm;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.gresearch.nortem.common.zookeper.ZookeperConnectorFactory;
import uk.co.gresearch.nortem.nikita.common.NikitaEngine;
import uk.co.gresearch.nortem.nikita.common.NikitaResult;
import uk.co.gresearch.nortem.nikita.compiler.NikitaCorrelationRulesCompiler;
import uk.co.gresearch.nortem.nikita.storm.model.NikitaStormAttributes;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import static org.apache.storm.utils.TupleUtils.isTick;
import static org.apache.storm.utils.TupleUtils.putTickFrequencyIntoComponentConfig;

public class NikitaCorrelationEngineBolt extends NikitaEngineBolt {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final int cleanIntervalSec;

    public NikitaCorrelationEngineBolt(NikitaStormAttributes attributes,
                                       ZookeperConnectorFactory zookeperConnectorFactory) {
        super(attributes, zookeperConnectorFactory);
        cleanIntervalSec = attributes.getNikitaEngineCleanIntervalSec();
    }

    protected NikitaEngine getNikitaEngine(String rules) {
        try {
            NikitaResult engineResult =  NikitaCorrelationRulesCompiler
                    .createNikitaCorrelationRulesCompiler()
                    .compile(rules);
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
    public Map<String, Object> getComponentConfiguration() {
        return putTickFrequencyIntoComponentConfig(null, cleanIntervalSec);
    }

    @Override
    public void execute(Tuple tuple) {
        if (isTick(tuple)) {
            NikitaEngine currentEngine = nikitaEngine.get();
            currentEngine.clean();
            return;
        }

        super.execute(tuple);
    }
}
