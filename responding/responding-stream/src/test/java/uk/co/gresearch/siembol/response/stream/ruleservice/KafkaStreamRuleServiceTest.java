package uk.co.gresearch.siembol.response.stream.ruleservice;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.adrianwalker.multilinestring.Multiline;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.*;
import org.mockito.Mockito;
import uk.co.gresearch.siembol.common.error.ErrorMessage;
import uk.co.gresearch.siembol.common.error.ErrorType;
import uk.co.gresearch.siembol.response.stream.rest.application.ResponseConfigurationProperties;
import uk.co.gresearch.siembol.response.common.RespondingResult;
import uk.co.gresearch.siembol.response.common.RespondingResultAttributes;
import uk.co.gresearch.siembol.response.common.ResponseAlert;
import uk.co.gresearch.siembol.response.common.ResponseEvaluationResult;
import uk.co.gresearch.siembol.response.engine.RulesEngine;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    private static final ObjectReader ERROR_READER = new ObjectMapper()
            .readerFor(ErrorMessage.class);

    private KafkaStreamRulesService streamService;
    private RulesProvider rulesProvider;
    private RulesEngine rulesEngine;
    private String inputTopic = "input";
    private String errorTopic = "error";
    private RespondingResultAttributes resultAttributes;
    private ResponseAlert responseAlert;
    private KafkaStreams kafkaStreams;
    private TestingDriverKafkaStreamsFactory streamsFactory;
    private TopologyTestDriver testDriver;
    private TestInputTopic<String, String>  testInputTopic;
    private TestOutputTopic<String, String>  testErrorTopic;

    @Before
    public void setUp() {
        responseAlert = new ResponseAlert();
        resultAttributes = new RespondingResultAttributes();
        resultAttributes.setAlert(responseAlert);

        rulesProvider = Mockito.mock(RulesProvider.class);
        rulesEngine = Mockito.mock(RulesEngine.class);
        kafkaStreams = Mockito.mock(KafkaStreams.class);
        streamsFactory = new TestingDriverKafkaStreamsFactory(kafkaStreams);

        when(rulesProvider.getEngine()).thenReturn(rulesEngine);
        ResponseConfigurationProperties properties = new ResponseConfigurationProperties();
        properties.setInputTopic(inputTopic);
        properties.setErrorTopic(errorTopic);
        properties.setStreamConfig(new HashMap<>());
        properties.getStreamConfig().put("application.id", "siembol-response-" + UUID.randomUUID().toString());
        streamService = new KafkaStreamRulesService(rulesProvider, properties, streamsFactory);
        testDriver = streamsFactory.getTestDriver();
        testInputTopic = testDriver.createInputTopic(inputTopic, Serdes.String().serializer(),
                Serdes.String().serializer());
        testErrorTopic = testDriver.createOutputTopic(errorTopic, Serdes.String().deserializer(),
                Serdes.String().deserializer());
    }

    @After
    public void tearDown() throws IOException {
        streamsFactory.close();
    }

    @Test
    public void testMatchEngineTest() {
        when(rulesEngine.evaluate(any(ResponseAlert.class)))
                .thenReturn(RespondingResult.fromEvaluationResult(ResponseEvaluationResult.MATCH, responseAlert));
        testInputTopic.pipeInput(alertStr);
        Assert.assertTrue(testErrorTopic.isEmpty());
    }

    @Test
    public void testNoMatchEngineTest() throws JsonProcessingException {
        resultAttributes.setMessage("no rule matched");
        when(rulesEngine.evaluate(any(ResponseAlert.class)))
                .thenReturn(RespondingResult.fromEvaluationResult(ResponseEvaluationResult.NO_MATCH, responseAlert));
        testInputTopic.pipeInput(alertStr);
        Assert.assertFalse(testErrorTopic.isEmpty());
        verify(rulesEngine, times(1)).evaluate(any());
        String errorMessageStr = testErrorTopic.readValue();
        ErrorMessage errorMessage = ERROR_READER.readValue(errorMessageStr);
        Assert.assertEquals(ErrorType.RESPONSE_ERROR, errorMessage.getErrorType());
        Assert.assertEquals(alertStr, errorMessage.getRawMessage());
    }

    @Test
    public void testErrorMatchEngineTest() throws JsonProcessingException {
        when(rulesEngine.evaluate(any(ResponseAlert.class)))
                .thenReturn(RespondingResult.fromException(new IllegalStateException("tmp")));
        testInputTopic.pipeInput(alertStr);
        Assert.assertFalse(testErrorTopic.isEmpty());
        verify(rulesEngine, times(1)).evaluate(any());
        String errorMessageStr = testErrorTopic.readValue();
        ErrorMessage errorMessage = ERROR_READER.readValue(errorMessageStr);
        Assert.assertEquals(ErrorType.RESPONSE_ERROR, errorMessage.getErrorType());
        Assert.assertEquals(alertStr, errorMessage.getRawMessage());
    }

    @Test
    public void testExceptionEngineTest() throws JsonProcessingException {
        when(rulesEngine.evaluate(any(ResponseAlert.class))).thenThrow(new IllegalStateException());
        testInputTopic.pipeInput(alertStr);
        Assert.assertFalse(testErrorTopic.isEmpty());
        verify(rulesEngine, times(1)).evaluate(any());
        String errorMessageStr = testErrorTopic.readValue();
        ErrorMessage errorMessage = ERROR_READER.readValue(errorMessageStr);
        Assert.assertEquals(ErrorType.RESPONSE_ERROR, errorMessage.getErrorType());
        Assert.assertEquals(alertStr, errorMessage.getRawMessage());
    }
}
