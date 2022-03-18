package uk.co.gresearch.siembol.response.stream.ruleservice;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import uk.co.gresearch.siembol.common.metrics.SiembolMetrics;
import uk.co.gresearch.siembol.common.metrics.SiembolMetricsRegistrar;
import uk.co.gresearch.siembol.common.metrics.test.SiembolMetricsTestRegistrar;
import uk.co.gresearch.siembol.common.model.ZooKeeperAttributesDto;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnector;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnectorFactory;
import uk.co.gresearch.siembol.response.common.*;
import uk.co.gresearch.siembol.response.compiler.RespondingCompiler;
import uk.co.gresearch.siembol.response.compiler.RespondingCompilerImpl;
import uk.co.gresearch.siembol.response.engine.ResponseEngine;
import uk.co.gresearch.siembol.response.model.ProvidedEvaluatorsProperties;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static uk.co.gresearch.siembol.response.common.RespondingResult.StatusCode.OK;
import static uk.co.gresearch.siembol.response.common.ResponseEvaluationResult.MATCH;

public class ZooKeeperRulesProviderTest {
    private final String testingRules = """
            {
              "rules_version": 111,
              "rules": [
                {
                  "rule_name": "default_rule",
                  "rule_author": "john",
                  "rule_version": 1,
                  "rule_description": "default rule",
                  "evaluators": [
                    {
                      "evaluator_type": "fixed_result",
                      "evaluator_attributes": {
                        "evaluation_result": "match"
                      }
                    }
                  ]
                }
              ]
            }
               """;

    private RespondingCompiler compiler;
    private SiembolMetricsTestRegistrar metricsTestRegistrar;
    private SiembolMetricsRegistrar cachedMetricsRegistrar;
    private ZooKeeperConnectorFactory zooKeeperConnectorFactory;
    private ZooKeeperConnector rulesZooKeeperConnector;
    private ArgumentCaptor<Runnable> zooKeeperCallback;
    private ZooKeeperRulesProvider rulesProvider;
    private ZooKeeperAttributesDto zooKeeperAttributes;

    @Before
    public void setUp() throws Exception {
        metricsTestRegistrar = new SiembolMetricsTestRegistrar();
        cachedMetricsRegistrar = metricsTestRegistrar.cachedRegistrar();
        List<RespondingEvaluatorFactory> evaluatorFactories = new ArrayList<>(
                ProvidedEvaluators.getRespondingEvaluatorFactories(new ProvidedEvaluatorsProperties())
                        .getAttributes()
                        .getRespondingEvaluatorFactories());

        compiler = new RespondingCompilerImpl.Builder()
                .addRespondingEvaluatorFactories(evaluatorFactories)
                .metricsRegistrar(cachedMetricsRegistrar)
                .build();


        zooKeeperAttributes = new ZooKeeperAttributesDto();
        zooKeeperConnectorFactory = Mockito.mock(ZooKeeperConnectorFactory.class, withSettings().serializable());

        rulesZooKeeperConnector = Mockito.mock(ZooKeeperConnector.class, withSettings().serializable());
        when(zooKeeperConnectorFactory.createZookeeperConnector(zooKeeperAttributes))
                .thenReturn(rulesZooKeeperConnector);
        when(rulesZooKeeperConnector.getData()).thenReturn(testingRules);
        zooKeeperCallback = ArgumentCaptor.forClass(Runnable.class);
        doNothing().when(rulesZooKeeperConnector).addCacheListener(zooKeeperCallback.capture());

        rulesProvider = new ZooKeeperRulesProvider(zooKeeperConnectorFactory,
                zooKeeperAttributes,
                compiler,
                cachedMetricsRegistrar);
        Assert.assertEquals(1,
                metricsTestRegistrar.getCounterValue(SiembolMetrics.RESPONSE_RULES_UPDATE.getMetricName()));

    }

    @Test
    public void testMetadataEngineOk() {
        ResponseEngine engine = rulesProvider.getEngine();
        Assert.assertEquals(OK, engine.getRulesMetadata().getStatusCode());
        Assert.assertNotNull(engine.getRulesMetadata().getAttributes().getCompiledTime());
        Assert.assertEquals(111L, engine.getRulesMetadata().getAttributes().getRulesVersion().intValue());
        Assert.assertEquals(1L, engine.getRulesMetadata().getAttributes().getNumberOfRules().intValue());
        Assert.assertEquals(testingRules, engine.getRulesMetadata().getAttributes().getJsonRules());
    }

    @Test(expected = java.lang.IllegalStateException.class)
    public void testInvalidRulesInit() throws Exception {
        when(rulesZooKeeperConnector.getData()).thenReturn("INVALID");
        rulesProvider = new ZooKeeperRulesProvider(zooKeeperConnectorFactory,
                zooKeeperAttributes,
                compiler,
                cachedMetricsRegistrar);
    }

    @Test
    public void testEngineEvaluate() {
        ResponseAlert alert = new ResponseAlert();
        RespondingResult result = rulesProvider.getEngine().evaluate(alert);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertEquals(MATCH, result.getAttributes().getResult());
    }

    @Test
    public void updateOk() {
        zooKeeperCallback.getValue().run();
        Assert.assertEquals(2,
                metricsTestRegistrar.getCounterValue(SiembolMetrics.RESPONSE_RULES_UPDATE.getMetricName()));

        zooKeeperCallback.getValue().run();
        Assert.assertEquals(3,
                metricsTestRegistrar.getCounterValue(SiembolMetrics.RESPONSE_RULES_UPDATE.getMetricName()));
        verify(rulesZooKeeperConnector, times(3)).getData();

    }

    @Test
    public void updateError() {
        when(rulesZooKeeperConnector.getData()).thenReturn("INVALID");
        zooKeeperCallback.getValue().run();
        Assert.assertEquals(1,
                metricsTestRegistrar.getCounterValue(SiembolMetrics.RESPONSE_RULES_UPDATE.getMetricName()));
        Assert.assertEquals(1,
                metricsTestRegistrar.getCounterValue(SiembolMetrics.RESPONSE_RULES_UPDATE_ERROR.getMetricName()));
    }
}
