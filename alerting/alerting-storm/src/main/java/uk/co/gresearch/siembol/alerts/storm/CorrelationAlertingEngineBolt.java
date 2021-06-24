package uk.co.gresearch.siembol.alerts.storm;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnectorFactory;
import uk.co.gresearch.siembol.alerts.common.AlertingEngine;
import uk.co.gresearch.siembol.alerts.common.AlertingResult;
import uk.co.gresearch.siembol.alerts.compiler.AlertingCorrelationRulesCompiler;
import uk.co.gresearch.siembol.common.model.AlertingStormAttributesDto;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import static org.apache.storm.utils.TupleUtils.isTick;
import static org.apache.storm.utils.TupleUtils.putTickFrequencyIntoComponentConfig;

public class CorrelationAlertingEngineBolt extends AlertingEngineBolt {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final int cleanIntervalSec;

    public CorrelationAlertingEngineBolt(AlertingStormAttributesDto attributes,
                                         ZooKeeperConnectorFactory zooKeeperConnectorFactory) {
        super(attributes, zooKeeperConnectorFactory);
        cleanIntervalSec = attributes.getAlertingEngineCleanIntervalSec();
    }

    protected AlertingEngine getAlertingEngine(String rules) {
        try {
            AlertingResult engineResult =  AlertingCorrelationRulesCompiler
                    .createAlertingCorrelationRulesCompiler()
                    .compile(rules);
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
