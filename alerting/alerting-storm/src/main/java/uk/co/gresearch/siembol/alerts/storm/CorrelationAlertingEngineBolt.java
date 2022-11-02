package uk.co.gresearch.siembol.alerts.storm;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.gresearch.siembol.common.metrics.storm.StormMetricsRegistrarFactory;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperCompositeConnectorFactory;
import uk.co.gresearch.siembol.alerts.common.AlertingEngine;
import uk.co.gresearch.siembol.alerts.common.AlertingResult;
import uk.co.gresearch.siembol.alerts.compiler.AlertingCorrelationRulesCompiler;
import uk.co.gresearch.siembol.common.model.AlertingStormAttributesDto;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnector;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import static org.apache.storm.utils.TupleUtils.isTick;
import static org.apache.storm.utils.TupleUtils.putTickFrequencyIntoComponentConfig;
/**
 * An object for integration of a correlation alerting engine into a storm bolt
 *
 * <p>This class extends a Storm AlertingBolt class to implement a Storm bolt, that
 *  evaluates events using an correlation engine initialised form the rules cached in the ZooKeeper,
 *  watches for the rules update in ZooKeeper and updates the rules without need to restart the topology or the bolt,
 *  emits alerts and exceptions after matching.
 *  It cleans regularly internal state of counters by calling clean method of the alerting engine.
 *
 * @author Marian Novotny
 * @see AlertingEngine
 * @see ZooKeeperConnector
 *
 */
public class CorrelationAlertingEngineBolt extends AlertingEngineBolt {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final int cleanIntervalSec;

    public CorrelationAlertingEngineBolt(AlertingStormAttributesDto attributes,
                                         ZooKeeperCompositeConnectorFactory zooKeeperConnectorFactory,
                                         StormMetricsRegistrarFactory metricsFactory) {
        super(attributes, zooKeeperConnectorFactory, metricsFactory);
        cleanIntervalSec = attributes.getAlertingEngineCleanIntervalSec();
    }

    @Override
    protected AlertingEngine getAlertingEngine(List<String> rulesList) {
        try {
            AlertingResult engineResult =  AlertingCorrelationRulesCompiler
                    .createAlertingCorrelationRulesCompiler()
                    .compile(rulesList);
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
    public Map<String, Object> getComponentConfiguration() {
        return putTickFrequencyIntoComponentConfig(null, cleanIntervalSec);
    }

    @Override
    public void execute(Tuple tuple) {
        if (isTick(tuple)) {
            AlertingEngine currentEngine = AlertingEngine.get();
            currentEngine.clean();
            return;
        }

        super.execute(tuple);
    }
}
