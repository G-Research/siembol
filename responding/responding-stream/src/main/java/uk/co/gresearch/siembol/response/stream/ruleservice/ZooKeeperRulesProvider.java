package uk.co.gresearch.siembol.response.stream.ruleservice;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.common.constants.SiembolConstants;
import uk.co.gresearch.siembol.common.metrics.SiembolCounter;
import uk.co.gresearch.siembol.common.metrics.SiembolMetrics;
import uk.co.gresearch.siembol.common.metrics.SiembolMetricsRegistrar;
import uk.co.gresearch.siembol.common.model.ZooKeeperAttributesDto;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnector;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnectorFactory;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnectorFactoryImpl;
import uk.co.gresearch.siembol.response.common.RespondingResult;
import uk.co.gresearch.siembol.response.compiler.RespondingCompiler;
import uk.co.gresearch.siembol.response.engine.ResponseEngine;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.atomic.AtomicReference;

public class ZooKeeperRulesProvider implements RulesProvider {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String UPDATE_TRY_MSG_FORMAT = "Trying to update response rules {}";
    private static final String COMPILE_RULES_ERROR_MSG_FORMAT =
            "Compilation of response rules has failed with error message: {}";
    private static final String UPDATE_EXCEPTION_LOG = "Exception during response engine update: {}";
    private static final String ERROR_INIT_MESSAGE = "Response exception: Response rules initialisation error";
    private static final String INIT_START = "Response application initialisation start";
    private static final String INIT_COMPLETED = "Response application initialisation completed";
    private static final String PARSERS_UPDATE_START = "Response rules update start";
    private static final String PARSERS_UPDATE_COMPLETED = "Response rules update completed";

    private final AtomicReference<ResponseEngine> currentEngine = new AtomicReference<>();
    private final ZooKeeperConnector zooKeeperConnector;
    private final RespondingCompiler respondingCompiler;
    private final SiembolCounter updateCounter;
    private final SiembolCounter updateErrorCounter;


    public ZooKeeperRulesProvider(ZooKeeperAttributesDto zookeperAttributes,
                                  RespondingCompiler respondingCompiler,
                                  SiembolMetricsRegistrar metricsRegistrar) throws Exception {
        this(new ZooKeeperConnectorFactoryImpl(), zookeperAttributes, respondingCompiler, metricsRegistrar);
    }

    ZooKeeperRulesProvider(ZooKeeperConnectorFactory factory,
                           ZooKeeperAttributesDto zookeperAttributes,
                           RespondingCompiler respondingCompiler,
                           SiembolMetricsRegistrar metricsRegistrar) throws Exception {
        LOG.info(INIT_START);

        this.updateCounter = metricsRegistrar.registerCounter(SiembolMetrics.RESPONSE_RULES_UPDATE.getMetricName());
        this.updateErrorCounter = metricsRegistrar.registerCounter(SiembolMetrics.RESPONSE_RULES_ERROR_UPDATE.getMetricName());
        this.respondingCompiler = respondingCompiler;
        zooKeeperConnector = factory.createZookeeperConnector(zookeperAttributes);

        updateRules();
        if (currentEngine.get() == null) {
            throw new IllegalStateException(ERROR_INIT_MESSAGE);
        }
        zooKeeperConnector.addCacheListener(this::updateRules);
        LOG.info(INIT_COMPLETED);
    }

    private void updateRules() {
        try {
            LOG.info(PARSERS_UPDATE_START);
            String jsonRules = zooKeeperConnector.getData();
            LOG.info(UPDATE_TRY_MSG_FORMAT, StringUtils.left(jsonRules, SiembolConstants.MAX_SIZE_CONFIG_UPDATE_LOG));
            RespondingResult result = respondingCompiler.compile(jsonRules);
            if (result.getStatusCode() != RespondingResult.StatusCode.OK) {
                updateErrorCounter.increment();
                LOG.error(COMPILE_RULES_ERROR_MSG_FORMAT, result.getAttributes().getMessage());
                return;
            }

            currentEngine.set(result.getAttributes().getResponseEngine());
            updateCounter.increment();
            LOG.info(PARSERS_UPDATE_COMPLETED);
        } catch (Exception e) {
            updateErrorCounter.increment();
            LOG.error(UPDATE_EXCEPTION_LOG, ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public ResponseEngine getEngine() {
        return currentEngine.get();
    }
}
