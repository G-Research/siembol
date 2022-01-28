package uk.co.gresearch.siembol.parsers.application.parsing;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.gresearch.siembol.common.utils.TimeProvider;
import uk.co.gresearch.siembol.common.constants.SiembolMessageFields;
import uk.co.gresearch.siembol.parsers.common.ParserResult;
import uk.co.gresearch.siembol.parsers.common.SerializableSiembolParser;

import java.util.*;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SingleApplicationParserTest {
    private final String metadata = """
     {
    
         "a": "string",
         "b": 1,
         "c": true
     }
     """;

    private SerializableSiembolParser siembolParser;
    private final String sourceType = "test_type";
    private SingleApplicationParser appParser;
    private Map<String, Object> message1;
    private Map<String, Object> message2;
    private List<Map<String, Object>> parsed;
    private final String errorTopic = "error";
    private final String outputTopic = "output";
    private final byte[] input = "test".getBytes();
    private ParserResult parserResult;
    TimeProvider timeProvider;
    long currentTime = 1L;


    @Before
    public void setUp() {
        timeProvider = Mockito.mock(TimeProvider.class);
        when(timeProvider.getCurrentTimeInMs()).thenReturn(currentTime);
        siembolParser = Mockito.mock(SerializableSiembolParser.class);
        when(siembolParser.getSourceType()).thenReturn(sourceType);

        message1 = new HashMap<>();
        message1.put("test_field", "a");
        message1.put("timestamp", 1);

        message2 = new HashMap<>();
        message2.put("test_field", "b");
        message2.put("timestamp", 2);

        parsed = new ArrayList<>();
        parsed.add(message1);
        parsed.add(message2);
        parserResult = new ParserResult();
        parserResult.setParsedMessages(parsed);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingArguments() {
        appParser = SingleApplicationParser.builder()
                .errorTopic(errorTopic)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingArguments2() throws Exception {
        appParser = SingleApplicationParser.builder()
                .parser(outputTopic, siembolParser)
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(null)
                .build();
    }

    @Test
    public void testParseTwoMessages() throws Exception {
        appParser = SingleApplicationParser.builder()
                .parser(outputTopic, siembolParser)
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(timeProvider)
                .build();

        when(siembolParser.parseToResult(metadata, input)).thenReturn(parserResult);
        List<ParsingApplicationResult> result = appParser.parse( metadata, input);
        verify(timeProvider, times(1)).getCurrentTimeInMs();
        verify(siembolParser, times(1)).parseToResult(metadata, input);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(2, result.get(0).getMessages().size());
        Assert.assertEquals(outputTopic, result.get(0).getTopic());
        Assert.assertTrue(result.get(0).getMessages().get(0).contains(SiembolMessageFields.PARSING_TIME + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("test_field" + "\":\"a"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("timestamp" + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains(
                SiembolMessageFields.SENSOR_TYPE + "\":\"test_type\""));
        Assert.assertTrue(result.get(0).getMessages().get(1).contains(SiembolMessageFields.PARSING_TIME + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(1).contains("test_field" + "\":\"b"));
        Assert.assertTrue(result.get(0).getMessages().get(1).contains("timestamp" + "\":2"));
        Assert.assertTrue(result.get(0).getMessages().get(1).contains(
                SiembolMessageFields.SENSOR_TYPE + "\":\"test_type\""));
    }

    @Test
    public void testParseOneMessages() throws Exception {
        appParser = SingleApplicationParser.builder()
                .parser(outputTopic, siembolParser)
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(timeProvider)
                .build();

        parserResult.getParsedMessages().remove(1);
        when(siembolParser.parseToResult(metadata, input)).thenReturn(parserResult);
        List<ParsingApplicationResult> result = appParser.parse( metadata, input);
        verify(timeProvider, times(1)).getCurrentTimeInMs();
        verify(siembolParser, times(1)).parseToResult(metadata, input);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(1, result.get(0).getMessages().size());
        Assert.assertEquals(outputTopic, result.get(0).getTopic());
        Assert.assertTrue(result.get(0).getMessages().get(0).contains(SiembolMessageFields.PARSING_TIME + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("test_field" + "\":\"a"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("timestamp" + "\":1"));
        Assert.assertFalse(result.get(0).getMessages().get(0).contains("guid" + "\":"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains(
                SiembolMessageFields.SENSOR_TYPE + "\":\"test_type\""));
    }

    @Test
    public void testParseOneMessageGuid() throws Exception {
        appParser = SingleApplicationParser.builder()
                .parser(outputTopic, siembolParser)
                .addGuidToMessages(true)
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(timeProvider)
                .build();

        parserResult.getParsedMessages().remove(1);
        when(siembolParser.parseToResult(metadata, input)).thenReturn(parserResult);
        List<ParsingApplicationResult> result = appParser.parse( metadata, input);
        verify(timeProvider, times(1)).getCurrentTimeInMs();
        verify(siembolParser, times(1)).parseToResult(metadata, input);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(1, result.get(0).getMessages().size());
        Assert.assertEquals(outputTopic, result.get(0).getTopic());
        Assert.assertTrue(result.get(0).getMessages().get(0).contains(SiembolMessageFields.PARSING_TIME + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("test_field" + "\":\"a"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("timestamp" + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("guid" + "\":"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains(
                SiembolMessageFields.SENSOR_TYPE + "\":\"test_type\""));
    }

    @Test
    public void testParseOneMessageFiltered() throws Exception {
        appParser = SingleApplicationParser.builder()
                .parser(outputTopic, siembolParser)
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(timeProvider)
                .build();

        parserResult.getParsedMessages().replaceAll(x-> new HashMap<>());
        when(siembolParser.parseToResult(metadata, input)).thenReturn(parserResult);
        List<ParsingApplicationResult> result = appParser.parse( metadata, input);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testExceptionParsing() throws Exception {
        appParser = SingleApplicationParser.builder()
                .parser(outputTopic, siembolParser)
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(timeProvider)
                .build();

        parserResult.setParsedMessages(null);
        parserResult.setException(new IllegalStateException("test_exception"));
        when(siembolParser.parseToResult(metadata, input)).thenReturn(parserResult);
        List<ParsingApplicationResult> result = appParser.parse( metadata, input);
        verify(timeProvider, times(1)).getCurrentTimeInMs();
        verify(siembolParser, times(1)).parseToResult(metadata, input);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(1, result.get(0).getMessages().size());

        Assert.assertEquals(errorTopic, result.get(0).getTopic());
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("test_exception"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("\"failed_sensor_type\":\"test_type\""));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("\"raw_message\":\"test\""));
    }

    @Test
    public void testRuntimeExceptionParsing() throws Exception {
        appParser = SingleApplicationParser.builder()
                .parser(outputTopic, siembolParser)
                .name("test-app")
                .errorTopic(errorTopic)
                .timeProvider(timeProvider)
                .build();

        when(siembolParser.parseToResult(metadata, input)).thenThrow(new RuntimeException("runtime_exception"));
        List<ParsingApplicationResult> result = appParser.parse( metadata, input);
        verify(timeProvider, times(1)).getCurrentTimeInMs();
        verify(siembolParser, times(1)).parseToResult(metadata, input);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(1, result.get(0).getMessages().size());

        Assert.assertEquals(errorTopic, result.get(0).getTopic());
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("runtime_exception"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("\"failed_sensor_type\":\"test-app\""));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("\"raw_message\":\"test\""));
    }

    @Test
    public void testParsingFiltered() throws Exception {
        appParser = SingleApplicationParser.builder()
                .parser(outputTopic, siembolParser)
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(timeProvider)
                .build();

        parserResult.setParsedMessages(new ArrayList<>());
        when(siembolParser.parseToResult(metadata, input)).thenReturn(parserResult);
        List<ParsingApplicationResult> result = appParser.parse( metadata, input);
        verify(timeProvider, times(1)).getCurrentTimeInMs();
        verify(siembolParser, times(1)).parseToResult(metadata, input);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void testParseTwoMessagesMetadata() throws Exception {
        appParser = SingleApplicationParser.builder()
                .parser(outputTopic, siembolParser)
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(timeProvider)
                .parseMetadata(true)
                .metadataPrefix("test_metadata:")
                .build();

        when(siembolParser.parseToResult(metadata, input)).thenReturn(parserResult);
        List<ParsingApplicationResult> result = appParser.parse( metadata, input);
        verify(timeProvider, times(1)).getCurrentTimeInMs();
        verify(siembolParser, times(1)).parseToResult(metadata, input);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(2, result.get(0).getMessages().size());
        Assert.assertEquals(outputTopic, result.get(0).getTopic());
        Assert.assertTrue(result.get(0).getMessages().get(0).contains(SiembolMessageFields.PARSING_TIME + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("test_field" + "\":\"a"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("test_metadata:a" + "\":\"string\""));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("test_metadata:b" + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("test_metadata:c" + "\":true"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("timestamp" + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains(
                SiembolMessageFields.SENSOR_TYPE + "\":\"test_type\""));
        Assert.assertTrue(result.get(0).getMessages().get(1).contains(SiembolMessageFields.PARSING_TIME + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(1).contains("test_field" + "\":\"b"));
        Assert.assertTrue(result.get(0).getMessages().get(1).contains("timestamp" + "\":2"));
        Assert.assertTrue(result.get(0).getMessages().get(1).contains("test_metadata:a" + "\":\"string\""));
        Assert.assertTrue(result.get(0).getMessages().get(1).contains("test_metadata:b" + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(1).contains("test_metadata:c" + "\":true"));
        Assert.assertTrue(result.get(0).getMessages().get(1).contains(
                SiembolMessageFields.SENSOR_TYPE + "\":\"test_type\""));
    }

    @Test
    public void testParseTwoMessagesGuidMetadata() throws Exception {
        appParser = SingleApplicationParser.builder()
                .parser(outputTopic, siembolParser)
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(timeProvider)
                .parseMetadata(true)
                .metadataPrefix("test_metadata:")
                .addGuidToMessages(true)
                .build();

        when(siembolParser.parseToResult(metadata, input)).thenReturn(parserResult);
        List<ParsingApplicationResult> result = appParser.parse( metadata, input);
        verify(timeProvider, times(1)).getCurrentTimeInMs();
        verify(siembolParser, times(1)).parseToResult(metadata, input);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(2, result.get(0).getMessages().size());
        Assert.assertEquals(outputTopic, result.get(0).getTopic());
        Assert.assertTrue(result.get(0).getMessages().get(0).contains(SiembolMessageFields.PARSING_TIME + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("test_field" + "\":\"a"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("test_metadata:a" + "\":\"string\""));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("test_metadata:b" + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("test_metadata:c" + "\":true"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("timestamp" + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("guid" + "\":"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains(
                SiembolMessageFields.SENSOR_TYPE + "\":\"test_type\""));
        Assert.assertTrue(result.get(0).getMessages().get(1).contains(SiembolMessageFields.PARSING_TIME + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(1).contains("test_field" + "\":\"b"));
        Assert.assertTrue(result.get(0).getMessages().get(1).contains("timestamp" + "\":2"));
        Assert.assertTrue(result.get(0).getMessages().get(1).contains("test_metadata:a" + "\":\"string\""));
        Assert.assertTrue(result.get(0).getMessages().get(1).contains("test_metadata:b" + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(1).contains("test_metadata:c" + "\":true"));
        Assert.assertTrue(result.get(0).getMessages().get(1).contains(
                SiembolMessageFields.SENSOR_TYPE + "\":\"test_type\""));
        Assert.assertTrue(result.get(0).getMessages().get(1).contains("guid" + "\":"));
    }

    @Test
    public void testParseTwoMessagesMetadataNoPrefix() throws Exception {
        appParser = SingleApplicationParser.builder()
                .parser(outputTopic, siembolParser)
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(timeProvider)
                .parseMetadata(true)
                .build();

        when(siembolParser.parseToResult(metadata, input)).thenReturn(parserResult);
        List<ParsingApplicationResult> result = appParser.parse( metadata, input);
        verify(timeProvider, times(1)).getCurrentTimeInMs();
        verify(siembolParser, times(1)).parseToResult(metadata, input);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(2, result.get(0).getMessages().size());
        Assert.assertEquals(outputTopic, result.get(0).getTopic());
        Assert.assertTrue(result.get(0).getMessages().get(0).contains(SiembolMessageFields.PARSING_TIME + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("test_field" + "\":\"a"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("a" + "\":\"string\""));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("b" + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("c" + "\":true"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("timestamp" + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains(
                SiembolMessageFields.SENSOR_TYPE + "\":\"test_type\""));
        Assert.assertTrue(result.get(0).getMessages().get(1).contains(SiembolMessageFields.PARSING_TIME + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(1).contains("test_field" + "\":\"b"));
        Assert.assertTrue(result.get(0).getMessages().get(1).contains("timestamp" + "\":2"));
        Assert.assertTrue(result.get(0).getMessages().get(1).contains("a" + "\":\"string\""));
        Assert.assertTrue(result.get(0).getMessages().get(1).contains("b" + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(1).contains("c" + "\":true"));
        Assert.assertTrue(result.get(0).getMessages().get(1).contains(
                SiembolMessageFields.SENSOR_TYPE + "\":\"test_type\""));
    }
}
