package uk.co.gresearch.siembol.parsers.application.parsing;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.gresearch.siembol.common.constants.SiembolMessageFields;
import uk.co.gresearch.siembol.common.utils.TimeProvider;
import uk.co.gresearch.siembol.parsers.common.ParserResult;
import uk.co.gresearch.siembol.parsers.common.SerializableSiembolParser;

import java.util.*;

import static org.mockito.Mockito.*;

public class SourceRoutingApplicationParserTest {
    private final String metadata = """
     {
         "a": "string",
         "b": 1,
         "c": true
     }
     """;

    private SerializableSiembolParser defaultParser;
    private SerializableSiembolParser routedParser1;
    private SerializableSiembolParser routedParser2;

    private SourceRoutingApplicationParser appParser;
    private final String errorTopic = "error";
    private final String outputTopic = "output";
    private final byte[] input = "test".getBytes();
    private ParserResult defaultParserResult;
    private ParserResult routedParserResult1;
    private ParserResult routedParserResult2;
    TimeProvider timeProvider;
    long currentTime = 1L;

    @Before
    public void setUp() {
        timeProvider = Mockito.mock(TimeProvider.class);
        when(timeProvider.getCurrentTimeInMs()).thenReturn(currentTime);

        defaultParser = Mockito.mock(SerializableSiembolParser.class);
        when(defaultParser.getSourceType()).thenReturn("default-parser");

        routedParser1 = Mockito.mock(SerializableSiembolParser.class);
        when(routedParser1.getSourceType()).thenReturn("routed-parser1");

        routedParser2 = Mockito.mock(SerializableSiembolParser.class);
        when(routedParser2.getSourceType()).thenReturn("routed-parser2");

        Map<String, Object> messageRoutedParser = new HashMap<>();
        messageRoutedParser.put("output_field", "routed");
        messageRoutedParser.put("original_string", "test");
        messageRoutedParser.put("timestamp", 3);

        routedParserResult1 = new ParserResult();
        routedParserResult1.setParsedMessages(Arrays.asList(messageRoutedParser));

        routedParserResult2 = new ParserResult();
        routedParserResult2.setParsedMessages(Arrays.asList(new HashMap<>(messageRoutedParser)));

        Map<String, Object> messageDefaultParser = new HashMap<>();
        messageDefaultParser.put("output_field", "default");
        messageDefaultParser.put("original_string", "test");
        messageDefaultParser.put("timestamp", 3);

        defaultParserResult = new ParserResult();
        defaultParserResult.setParsedMessages(Arrays.asList(new HashMap<>(messageDefaultParser)));

    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingArguments() {
        SourceRoutingApplicationParser.builder()
                .errorTopic(errorTopic)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingArguments2() {
        SourceRoutingApplicationParser.builder()
                .defaultParser(outputTopic, defaultParser)
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(null)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDuplicatesSources() {
        SourceRoutingApplicationParser.builder()
                .defaultParser(outputTopic, defaultParser)
                .addParser("a", "out", routedParser1)
                .addParser("a", "out", routedParser2)
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(timeProvider)
                .build();
    }

    @Test
    public void testParseOneMessageDefault() {
        appParser = SourceRoutingApplicationParser.builder()
                .defaultParser(outputTopic, defaultParser)
                .addParser("a", "out", routedParser1)
                .addParser("b", "out", routedParser2)
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(timeProvider)
                .build();

        when(defaultParser.parseToResult(metadata, "dummy".getBytes())).thenReturn(defaultParserResult);

        List<ParsingApplicationResult> result = appParser.parse("c", metadata, "dummy".getBytes());
        verify(timeProvider, times(1)).getCurrentTimeInMs();
        verify(defaultParser, times(1)).parseToResult(metadata, "dummy".getBytes());
        Assert.assertEquals(outputTopic, result.get(0).getTopic());
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(1, result.get(0).getMessages().size());
        Assert.assertTrue(result.get(0).getMessages().get(0).contains(SiembolMessageFields.PARSING_TIME + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("output_field" + "\":\"default"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains(SiembolMessageFields.PARSING_TIME + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("original_string" + "\":\"test"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("timestamp" + "\":3"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains(
                SiembolMessageFields.SENSOR_TYPE + "\":\"default-parser\""));
    }

    @Test
    public void testParseOneMessageRouted() {
        appParser = SourceRoutingApplicationParser.builder()
                .defaultParser(outputTopic, defaultParser)
                .addParser("a", "out", routedParser1)
                .addParser("b", "out", routedParser2)
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(timeProvider)
                .build();

        when(routedParser1.parseToResult(metadata, "dummy".getBytes())).thenReturn(routedParserResult1);

        List<ParsingApplicationResult> result = appParser.parse("a", metadata, "dummy".getBytes());
        verify(timeProvider, times(1)).getCurrentTimeInMs();
        verify(routedParser1, times(1)).parseToResult(metadata, "dummy".getBytes());
        Assert.assertEquals("out", result.get(0).getTopic());
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(1, result.get(0).getMessages().size());
        Assert.assertTrue(result.get(0).getMessages().get(0).contains(SiembolMessageFields.PARSING_TIME + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("output_field" + "\":\"routed"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains(SiembolMessageFields.PARSING_TIME + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("original_string" + "\":\"test"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("timestamp" + "\":3"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains(
                SiembolMessageFields.SENSOR_TYPE + "\":\"routed-parser1\""));
    }

    @Test
    public void testParseOneMessageRouted2() {
        appParser = SourceRoutingApplicationParser.builder()
                .defaultParser(outputTopic, defaultParser)
                .addParser("a", "out", routedParser1)
                .addParser("b", "out", routedParser2)
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(timeProvider)
                .build();

        when(routedParser2.parseToResult(metadata, "dummy".getBytes())).thenReturn(routedParserResult1);

        List<ParsingApplicationResult> result = appParser.parse("b", metadata, "dummy".getBytes());
        verify(timeProvider, times(1)).getCurrentTimeInMs();
        verify(routedParser2, times(1)).parseToResult(metadata, "dummy".getBytes());
        Assert.assertEquals("out", result.get(0).getTopic());
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(1, result.get(0).getMessages().size());
        Assert.assertTrue(result.get(0).getMessages().get(0).contains(SiembolMessageFields.PARSING_TIME + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("output_field" + "\":\"routed"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains(SiembolMessageFields.PARSING_TIME + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("original_string" + "\":\"test"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("timestamp" + "\":3"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains(
                SiembolMessageFields.SENSOR_TYPE + "\":\"routed-parser2\""));
    }

    @Test
    public void testParseOneMessageFiltered() {
        appParser = SourceRoutingApplicationParser.builder()
                .defaultParser(outputTopic, defaultParser)
                .addParser("a", "out", routedParser1)
                .addParser("b", "out", routedParser2)
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(timeProvider)
                .build();

        routedParserResult1.setParsedMessages(new ArrayList<>());
        when(routedParser1.parseToResult(metadata, input)).thenReturn(routedParserResult1);


        List<ParsingApplicationResult> result = appParser.parse("a", metadata, input);
        verify(timeProvider, times(1)).getCurrentTimeInMs();
        verify(routedParser1, times(1)).parseToResult(metadata, input);

        Assert.assertFalse(result.isEmpty());
        Assert.assertEquals(1, result.size());
        Assert.assertNull(result.get(0).getMessages());
    }

    @Test
    public void testParseOneMessageException() {
        appParser = SourceRoutingApplicationParser.builder()
                .defaultParser(outputTopic, defaultParser)
                .addParser("a", "out", routedParser1)
                .addParser("b", "out", routedParser2)
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(timeProvider)
                .build();

        routedParserResult1.setException(new IllegalStateException("test_exception"));
        when(routedParser1.parseToResult(metadata, input)).thenReturn(routedParserResult1);

        List<ParsingApplicationResult> result = appParser.parse("a", metadata, input);
        verify(timeProvider, times(1)).getCurrentTimeInMs();
        verify(routedParser1, times(1)).parseToResult(metadata, input);

        Assert.assertEquals(1, result.size());
        Assert.assertEquals("error", result.get(0).getTopic());
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("\"failed_sensor_type\":\"routed-parser1\""));
    }
}
