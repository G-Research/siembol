package uk.co.gresearch.siembol.response.application.ruleservice;

import com.github.charithe.kafka.EphemeralKafkaBroker;
import com.github.charithe.kafka.KafkaJunitRule;
import org.adrianwalker.multilinestring.Multiline;
import org.junit.*;
import org.mockito.Mockito;
import org.springframework.boot.actuate.health.Status;
import uk.co.gresearch.siembol.response.application.rest.RespondingConfigProperties;
import uk.co.gresearch.siembol.response.common.RespondingResult;
import uk.co.gresearch.siembol.response.common.RespondingResultAttributes;
import uk.co.gresearch.siembol.response.common.ResponseAlert;
import uk.co.gresearch.siembol.response.common.ResponseEvaluationResult;
import uk.co.gresearch.siembol.response.engine.RulesEngine;

import java.util.List;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class KafkaStreamRuleServiceTest {
    /**
     *{
     *  "source_type" : "secret",
     *  "is_alert" : "TruE",
     *  "dummy_field_int" : 1,
     *  "dummy_field_boolean" : false
     *}
     **/
    @Multiline
    private static String alertStr;

    @ClassRule
    public static KafkaJunitRule kafkaRule = new KafkaJunitRule(EphemeralKafkaBroker.create());

    private KafkaStreamRulesService streamService;
    private RulesProvider rulesProvider;
    private RulesEngine rulesEngine;
    private String inputTopic = "input";
    private String errorTopic = "error";
    private RespondingResultAttributes resultAttributes;
    private ResponseAlert responseAlert;

    @Before
    public void setUp() {
        responseAlert = new ResponseAlert();
        resultAttributes = new RespondingResultAttributes();
        resultAttributes.setAlert(responseAlert);

        rulesProvider = Mockito.mock(RulesProvider.class);
        rulesEngine = Mockito.mock(RulesEngine.class);

        when(rulesProvider.getEngine()).thenReturn(rulesEngine);
        RespondingConfigProperties properties = new RespondingConfigProperties();
        properties.setInputTopic(inputTopic);
        properties.setErrorTopic(errorTopic);
        properties.setStreamConfig(new HashMap<>());
        String bootstrapServer = String.format("127.0.0.1:%d", kafkaRule.helper().kafkaPort());
        properties.getStreamConfig().put("application.id", "siembol-response");
        properties.getStreamConfig().put("bootstrap.servers", bootstrapServer);
        properties.getStreamConfig().put("security.protocol", "PLAINTEXT");

        kafkaRule.waitForStartup();
        streamService = new KafkaStreamRulesService(rulesProvider, properties);
    }

    @After
    public void tearDown() {
        streamService.close();
    }

    @Test
    public void testMatchEngineTest() throws Exception {
        when(rulesEngine.evaluate(any()))
                .thenReturn(RespondingResult.fromEvaluationResult(ResponseEvaluationResult.MATCH, responseAlert));
        kafkaRule.helper().produceStrings(inputTopic, alertStr.trim());;
        Assert.assertEquals(Status.UP, streamService.checkHealth().toFuture().get().getStatus());
    }

    @Test
    public void testNoMatchEngineTest() throws Exception {
        resultAttributes.setMessage("no rule matched");
        when(rulesEngine.evaluate(any()))
                .thenReturn(RespondingResult.fromEvaluationResult(ResponseEvaluationResult.NO_MATCH, responseAlert));
        kafkaRule.helper().produceStrings(inputTopic, alertStr.trim());

        List<String> outputEvent = kafkaRule.helper().consumeStrings(errorTopic, 1)
                .get(15, TimeUnit.SECONDS);
        Assert.assertNotNull(outputEvent);
        Assert.assertEquals(1, outputEvent.size());
        Assert.assertEquals(Status.UP, streamService.checkHealth().toFuture().get().getStatus());
    }

    @Test
    public void testErrorMatchEngineTest() throws Exception {
        when(rulesEngine.evaluate(any()))
                .thenReturn(RespondingResult.fromException(new IllegalStateException("tmp")));
        kafkaRule.helper().produceStrings(inputTopic, alertStr.trim());

        List<String> outputEvent = kafkaRule.helper().consumeStrings(errorTopic, 1)
                .get(10, TimeUnit.SECONDS);
        Assert.assertNotNull(outputEvent);
        Assert.assertEquals(1, outputEvent.size());
        Assert.assertEquals(Status.UP, streamService.checkHealth().toFuture().get().getStatus());
    }

    @Test
    public void testExceptionEngineTest() throws Exception {
        when(rulesEngine.evaluate(any())).thenThrow(new IllegalStateException());
        kafkaRule.helper().produceStrings(inputTopic, alertStr.trim());

        List<String> outputEvent = kafkaRule.helper().consumeStrings(errorTopic, 1)
                .get(15, TimeUnit.SECONDS);
        Assert.assertNotNull(outputEvent);
        Assert.assertEquals(1, outputEvent.size());
        Assert.assertEquals(Status.UP, streamService.checkHealth().toFuture().get().getStatus());
    }
}
