package uk.co.gresearch.siembol.parsers.application.parsing;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
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
    private static final ObjectReader JSON_READER = new ObjectMapper()
            .readerFor(new TypeReference<Map<String, Object>>() { });
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

    private final String originalStringTopic = "original";
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
    public void missingArguments() {
        appParser = SingleApplicationParser.builder()
                .errorTopic(errorTopic)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void missingArguments2() throws Exception {
        appParser = SingleApplicationParser.builder()
                .parser(outputTopic, siembolParser)
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(null)
                .build();
    }

    @Test
    public void parseTwoMessages() throws Exception {
        appParser = SingleApplicationParser.builder()
                .parser(outputTopic, siembolParser)
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(timeProvider)
                .build();

        when(siembolParser.parseToResult(metadata, input)).thenReturn(parserResult);
        List<ParsingApplicationResult> result = appParser.parse(metadata, input);
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
    public void parseOneMessage() throws Exception {
        appParser = SingleApplicationParser.builder()
                .parser(outputTopic, siembolParser)
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(timeProvider)
                .build();

        parserResult.getParsedMessages().remove(1);
        when(siembolParser.parseToResult(metadata, input)).thenReturn(parserResult);
        List<ParsingApplicationResult> result = appParser.parse(metadata, input);
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
    public void parseOneMessageWithGuid() throws Exception {
        appParser = SingleApplicationParser.builder()
                .parser(outputTopic, siembolParser)
                .addGuidToMessages(true)
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(timeProvider)
                .build();

        parserResult.getParsedMessages().remove(1);
        when(siembolParser.parseToResult(metadata, input)).thenReturn(parserResult);
        List<ParsingApplicationResult> result = appParser.parse(metadata, input);
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
    public void parseOneMessageFiltered() throws Exception {
        appParser = SingleApplicationParser.builder()
                .parser(outputTopic, siembolParser)
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(timeProvider)
                .build();

        parserResult.getParsedMessages().replaceAll(x -> new HashMap<>());
        when(siembolParser.parseToResult(metadata, input)).thenReturn(parserResult);
        List<ParsingApplicationResult> result = appParser.parse(metadata, input);
        Assert.assertEquals(1, result.size());
        Assert.assertNull(result.get(0).getMessages());
    }

    @Test
    public void parseWithError() throws Exception {
        appParser = SingleApplicationParser.builder()
                .parser(outputTopic, siembolParser)
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(timeProvider)
                .build();

        parserResult.setParsedMessages(null);
        parserResult.setException(new IllegalStateException("test_exception"));
        when(siembolParser.parseToResult(metadata, input)).thenReturn(parserResult);
        List<ParsingApplicationResult> result = appParser.parse(metadata, input);
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
    public void parseWithRuntimeExceptiom() throws Exception {
        appParser = SingleApplicationParser.builder()
                .parser(outputTopic, siembolParser)
                .name("test-app")
                .errorTopic(errorTopic)
                .timeProvider(timeProvider)
                .build();

        when(siembolParser.parseToResult(metadata, input)).thenThrow(new RuntimeException("runtime_exception"));
        List<ParsingApplicationResult> result = appParser.parse(metadata, input);
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
    public void parseFiltered() throws Exception {
        appParser = SingleApplicationParser.builder()
                .parser(outputTopic, siembolParser)
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(timeProvider)
                .build();

        parserResult.setParsedMessages(new ArrayList<>());
        when(siembolParser.parseToResult(metadata, input)).thenReturn(parserResult);
        List<ParsingApplicationResult> result = appParser.parse(metadata, input);
        verify(timeProvider, times(1)).getCurrentTimeInMs();
        verify(siembolParser, times(1)).parseToResult(metadata, input);
        Assert.assertEquals(1, result.size());
        Assert.assertNull(result.get(0).getMessages());

    }

    @Test
    public void parseTwoMessagesWithMetadata() throws Exception {
        appParser = SingleApplicationParser.builder()
                .parser(outputTopic, siembolParser)
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(timeProvider)
                .parseMetadata(true)
                .metadataPrefix("test_metadata:")
                .build();

        when(siembolParser.parseToResult(metadata, input)).thenReturn(parserResult);
        List<ParsingApplicationResult> result = appParser.parse(metadata, input);
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
    public void parseTwoMessagesWithGuidAndMetadata() throws Exception {
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
        List<ParsingApplicationResult> result = appParser.parse(metadata, input);
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
    public void parseTwoMessagesWithNoPrefixMetadata() throws Exception {
        appParser = SingleApplicationParser.builder()
                .parser(outputTopic, siembolParser)
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(timeProvider)
                .parseMetadata(true)
                .build();

        when(siembolParser.parseToResult(metadata, input)).thenReturn(parserResult);
        List<ParsingApplicationResult> result = appParser.parse(metadata, input);
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

    @Test
    public void parseOneMessageWithLargeFields() throws Exception {
        appParser = SingleApplicationParser.builder()
                .parser(outputTopic, siembolParser)
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(timeProvider)
                .maxFieldSize(10)
                .build();

        parserResult.getParsedMessages().remove(1);
        message1.put("large_field", "123456789ABC");


        when(siembolParser.parseToResult(metadata, input)).thenReturn(parserResult);
        List<ParsingApplicationResult> result = appParser.parse(metadata, input);
        verify(timeProvider, times(1)).getCurrentTimeInMs();
        verify(siembolParser, times(1)).parseToResult(metadata, input);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(1, result.get(0).getMessages().size());
        Assert.assertEquals(outputTopic, result.get(0).getTopic());
        Assert.assertTrue(result.get(0).getResultFlags().contains(ParsingApplicationResult.ResultFlag.PARSED));
        Assert.assertTrue(result.get(0).getResultFlags().contains(ParsingApplicationResult.ResultFlag.TRUNCATED_FIELDS));
        Assert.assertFalse(
                result.get(0).getResultFlags().contains(ParsingApplicationResult.ResultFlag.TRUNCATED_ORIGINAL_STRING));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains(SiembolMessageFields.PARSING_TIME + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("test_field" + "\":\"a"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("large_field" + "\":\"123456789A\""));

        Assert.assertTrue(result.get(0).getMessages().get(0).contains("timestamp" + "\":1"));

        Assert.assertFalse(result.get(0).getMessages().get(0).contains("guid" + "\":"));

        Assert.assertTrue(result.get(0).getMessages().get(0).contains(
                SiembolMessageFields.SENSOR_TYPE + "\":\"test_type\""));
    }

    @Test
    public void parseOneMessageWithLargeOriginalString() throws Exception {
        appParser = SingleApplicationParser.builder()
                .parser(outputTopic, siembolParser)
                .name("test")
                .errorTopic(errorTopic)
                .originalStringTopic(originalStringTopic)
                .timeProvider(timeProvider)
                .maxFieldSize(10)
                .build();

        parserResult.getParsedMessages().remove(1);
        message1.put("original_string", "123456789ABC");


        when(siembolParser.parseToResult(metadata, input)).thenReturn(parserResult);
        List<ParsingApplicationResult> result = appParser.parse(metadata, input);
        verify(timeProvider, times(1)).getCurrentTimeInMs();
        verify(siembolParser, times(1)).parseToResult(metadata, input);
        Assert.assertEquals(2, result.size());
        Assert.assertEquals(1, result.get(0).getMessages().size());
        Assert.assertEquals(outputTopic, result.get(0).getTopic());
        Assert.assertTrue(result.get(0).getResultFlags().contains(ParsingApplicationResult.ResultFlag.PARSED));
        Assert.assertTrue(result.get(0).getResultFlags().contains(ParsingApplicationResult.ResultFlag.TRUNCATED_FIELDS));
        Assert.assertTrue(result.get(0).getResultFlags().contains(
                ParsingApplicationResult.ResultFlag.TRUNCATED_ORIGINAL_STRING));
        Assert.assertTrue(
                result.get(0).getResultFlags().contains(ParsingApplicationResult.ResultFlag.TRUNCATED_ORIGINAL_STRING));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains(SiembolMessageFields.PARSING_TIME + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("test_field" + "\":\"a"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("original_string" + "\":\"123456789A\""));

        Assert.assertTrue(result.get(0).getMessages().get(0).contains("timestamp" + "\":1"));

        Assert.assertFalse(result.get(0).getMessages().get(0).contains("guid" + "\":"));

        Assert.assertTrue(result.get(0).getMessages().get(0).contains(
                SiembolMessageFields.SENSOR_TYPE + "\":\"test_type\""));

        Assert.assertEquals(1, result.get(1).getMessages().size());
        Assert.assertEquals(originalStringTopic, result.get(1).getTopic());
        Assert.assertTrue(result.get(1).getResultFlags().contains(ParsingApplicationResult.ResultFlag.ORIGINAL_MESSAGE));
        Assert.assertFalse(result.get(1).getResultFlags().contains(ParsingApplicationResult.ResultFlag.PARSED));
        Assert.assertEquals(1, result.get(0).getMessages().size());
        Assert.assertEquals(new String(input), result.get(1).getMessages().get(0));
    }

    @Test
    public void parseOneMessageWithLargeNumberOfFields() throws Exception {
        appParser = SingleApplicationParser.builder()
                .parser(outputTopic, siembolParser)
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(timeProvider)
                .maxFieldSize(100)
                .maxNumFields(20)
                .build();

        parserResult.getParsedMessages().remove(1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            sb.append("a");
            message1.put(sb.toString(), "dummy");
        }


        when(siembolParser.parseToResult(metadata, input)).thenReturn(parserResult);
        List<ParsingApplicationResult> result = appParser.parse(metadata, input);
        verify(timeProvider, times(1)).getCurrentTimeInMs();
        verify(siembolParser, times(1)).parseToResult(metadata, input);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(1, result.get(0).getMessages().size());
        Assert.assertEquals(outputTopic, result.get(0).getTopic());

        Map<String, Object> parsed = JSON_READER.readValue(result.get(0).getMessages().get(0));
        Assert.assertEquals(20, parsed.size());
        Assert.assertTrue(parsed.containsKey(SiembolMessageFields.PARSING_TIME.getName()));
        Assert.assertTrue(parsed.containsKey(SiembolMessageFields.TIMESTAMP.getName()));
        Assert.assertTrue(result.get(0).getResultFlags().contains(ParsingApplicationResult.ResultFlag.PARSED));
        Assert.assertTrue(result.get(0).getResultFlags().contains(ParsingApplicationResult.ResultFlag.REMOVED_FIELDS));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains(SiembolMessageFields.PARSING_TIME + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("test_field" + "\":\"a"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("timestamp" + "\":1"));
        Assert.assertFalse(result.get(0).getMessages().get(0).contains("aaaaaaaaaaaaaaaaa"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains(
                SiembolMessageFields.SENSOR_TYPE + "\":\"test_type\""));
    }
}
