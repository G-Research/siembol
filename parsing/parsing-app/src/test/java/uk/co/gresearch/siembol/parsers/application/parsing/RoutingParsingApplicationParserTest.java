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

public class RoutingParsingApplicationParserTest {
    private final String metadata = """
     {
         "a": "string",
         "b": 1,
         "c": true
     }
     """;

    private SerializableSiembolParser routerParser;
    private SerializableSiembolParser defaultParser;
    private SerializableSiembolParser routedParser1;
    private SerializableSiembolParser routedParser2;
    private final String routingConditionField = "test_field";
    private final String routingMessageField = "original_string";

    private RoutingParsingApplicationParser appParser;
    private Map<String, Object> message1;
    private Map<String, Object> message2;
    private List<Map<String, Object>> parsed;
    private final String errorTopic = "error";
    private final String outputTopic = "output";
    private final byte[] input = "test".getBytes();
    private ParserResult routerParserResult;
    private ParserResult routedParserResult1;
    private ParserResult routedParserResult2;
    TimeProvider timeProvider;
    long currentTime = 1L;

    @Before
    public void setUp() {
        timeProvider = Mockito.mock(TimeProvider.class);
        when(timeProvider.getCurrentTimeInMs()).thenReturn(currentTime);

        routerParser = Mockito.mock(SerializableSiembolParser.class);
        when(routerParser.getSourceType()).thenReturn("router-parser");

        defaultParser = Mockito.mock(SerializableSiembolParser.class);
        when(defaultParser.getSourceType()).thenReturn("default-parser");

        routedParser1 = Mockito.mock(SerializableSiembolParser.class);
        when(routedParser1.getSourceType()).thenReturn("routed-parser1");

        routedParser2 = Mockito.mock(SerializableSiembolParser.class);
        when(routedParser2.getSourceType()).thenReturn("routed-parser2");

        message1 = new HashMap<>();
        message1.put("test_field", "a");
        message1.put("original_string", "dummy");
        message1.put("timestamp", 1);

        message2 = new HashMap<>();
        message2.put("test_field", "b");
        message2.put("original_string", "dummy");
        message2.put("timestamp", 2);

        parsed = new ArrayList<>();
        parsed.add(message1);

        routerParserResult = new ParserResult();
        routerParserResult.setParsedMessages(parsed);

        Map<String, Object> messageRoutedParser = new HashMap<>();
        messageRoutedParser.put("output_field", "routed");
        messageRoutedParser.put("original_string", "test");
        messageRoutedParser.put("timestamp", 3);

        routedParserResult1 = new ParserResult();
        routedParserResult1.setParsedMessages(Arrays.asList(messageRoutedParser));

        routedParserResult2 = new ParserResult();
        routedParserResult2.setParsedMessages(Arrays.asList(new HashMap<>(messageRoutedParser)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingArguments() {
        appParser = RoutingParsingApplicationParser.builder()
                .errorTopic(errorTopic)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void wrongMaxFieldSize() {
        appParser =  RoutingParsingApplicationParser.builder()
                .routerParser(routerParser)
                .defaultParser(outputTopic, defaultParser)
                .routingConditionField(routingConditionField)
                .routingMessageField(routingMessageField)
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(timeProvider)
                .maxNumFields(1)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingArguments2() {
        appParser =  RoutingParsingApplicationParser.builder()
                .routerParser(routerParser)
                .defaultParser(outputTopic, defaultParser)
                .routingConditionField(routingConditionField)
                .routingMessageField(routingMessageField)
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(null)
                .build();
    }


    @Test
    public void testParseOneMessageDefault() {
        appParser = RoutingParsingApplicationParser.builder()
                .routerParser(routerParser)
                .defaultParser(outputTopic, defaultParser)
                .routingConditionField(routingConditionField)
                .routingMessageField(routingMessageField)
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(timeProvider)
                .build();

        when(routerParser.parseToResult(metadata, input)).thenReturn(routerParserResult);
        when(defaultParser.parseToResult(metadata, "dummy".getBytes())).thenReturn(routedParserResult1);

        List<ParsingApplicationResult> result = appParser.parse( metadata, input);
        verify(timeProvider, times(1)).getCurrentTimeInMs();
        verify(routerParser, times(1)).parseToResult(metadata, input);
        verify(defaultParser, times(1)).parseToResult(metadata, "dummy".getBytes());
        Assert.assertEquals(outputTopic, result.get(0).getTopic());
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(1, result.get(0).getMessages().size());
        Assert.assertTrue(result.get(0).getMessages().get(0).contains(SiembolMessageFields.PARSING_TIME + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("output_field" + "\":\"routed"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains(SiembolMessageFields.PARSING_TIME + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("original_string" + "\":\"test"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("timestamp" + "\":3"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains(
                SiembolMessageFields.SENSOR_TYPE + "\":\"default-parser\""));
    }

    @Test
    public void testParseOneMessageFiltered() {
        appParser = RoutingParsingApplicationParser.builder()
                .routerParser(routerParser)
                .defaultParser(outputTopic, defaultParser)
                .routingConditionField(routingConditionField)
                .routingMessageField(routingMessageField)
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(timeProvider)
                .build();

        routerParserResult.setParsedMessages(new ArrayList<>());
        when(routerParser.parseToResult(metadata, input)).thenReturn(routerParserResult);


        List<ParsingApplicationResult> result = appParser.parse( metadata, input);
        verify(timeProvider, times(1)).getCurrentTimeInMs();
        verify(routerParser, times(1)).parseToResult(metadata, input);

        Assert.assertEquals(1, result.size());
        Assert.assertTrue(result.get(0).getResultFlags().contains(ParsingApplicationResult.ResultFlag.FILTERED));
    }

    @Test
    public void routerParseTwMessagesError() {
        appParser = RoutingParsingApplicationParser.builder()
                .routerParser(routerParser)
                .defaultParser(outputTopic, defaultParser)
                .routingConditionField(routingConditionField)
                .routingMessageField(routingMessageField)
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(timeProvider)
                .build();

        parsed.add(message2);
        when(routerParser.parseToResult(metadata, input)).thenReturn(routerParserResult);

        List<ParsingApplicationResult> result = appParser.parse( metadata, input);
        verify(timeProvider, times(1)).getCurrentTimeInMs();
        verify(routerParser, times(1)).parseToResult(metadata, input);

        Assert.assertEquals(1, result.size());
        Assert.assertTrue(result.get(0).getResultFlags().contains(ParsingApplicationResult.ResultFlag.ERROR));
    }

    @Test
    public void testParseOneMessageGuidDefault() {
        appParser = RoutingParsingApplicationParser.builder()
                .routerParser(routerParser)
                .defaultParser(outputTopic, defaultParser)
                .routingConditionField(routingConditionField)
                .routingMessageField(routingMessageField)
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(timeProvider)
                .addGuidToMessages(true)
                .build();

        when(routerParser.parseToResult(metadata, input)).thenReturn(routerParserResult);
        when(defaultParser.parseToResult(metadata, "dummy".getBytes())).thenReturn(routedParserResult1);

        List<ParsingApplicationResult> result = appParser.parse( metadata, input);
        verify(timeProvider, times(1)).getCurrentTimeInMs();
        verify(routerParser, times(1)).parseToResult(metadata, input);
        verify(defaultParser, times(1)).parseToResult(metadata, "dummy".getBytes());
        Assert.assertEquals(outputTopic, result.get(0).getTopic());
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(1, result.get(0).getMessages().size());
        Assert.assertTrue(result.get(0).getMessages().get(0).contains(SiembolMessageFields.PARSING_TIME + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("output_field" + "\":\"routed"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains(SiembolMessageFields.PARSING_TIME + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("original_string" + "\":\"test"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("timestamp" + "\":3"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("guid" + "\":"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains(
                SiembolMessageFields.SENSOR_TYPE + "\":\"default-parser\""));
    }

    @Test
    public void testParseOneMessageRouterException() {
        appParser = RoutingParsingApplicationParser.builder()
                .routerParser(routerParser)
                .defaultParser(outputTopic, defaultParser)
                .routingConditionField(routingConditionField)
                .routingMessageField(routingMessageField)
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(timeProvider)
                .build();

        routerParserResult.getParsedMessages().clear();
        routerParserResult.setException(new IllegalStateException("test_exception"));
        when(routerParser.parseToResult(metadata, input)).thenReturn(routerParserResult);

        List<ParsingApplicationResult> result = appParser.parse( metadata, input);
        verify(timeProvider, times(1)).getCurrentTimeInMs();
        verify(routerParser, times(1)).parseToResult(metadata, input);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(1, result.get(0).getMessages().size());

        Assert.assertEquals(errorTopic, result.get(0).getTopic());
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("test_exception"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("\"failed_sensor_type\":\"router-parser\""));
    }

    @Test
    public void testParseOneMessageMissingRoutingMessageField() {
        appParser = RoutingParsingApplicationParser.builder()
                .routerParser(routerParser)
                .defaultParser(outputTopic, defaultParser)
                .routingConditionField(routingConditionField)
                .routingMessageField(routingMessageField)
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(timeProvider)
                .build();

        routerParserResult.getParsedMessages().get(0).remove(routingMessageField);
        when(routerParser.parseToResult(metadata, input)).thenReturn(routerParserResult);

        List<ParsingApplicationResult> result = appParser.parse( metadata, input);
        verify(routerParser, times(1)).parseToResult(metadata, input);

        Assert.assertEquals(errorTopic, result.get(0).getTopic());
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(1, result.get(0).getMessages().size());
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("Missing routing fields"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("\"failed_sensor_type\":\"router-parser\""));
    }

    @Test
    public void testParseOneMessageDefaultWithRoutedParsers() {
        appParser = RoutingParsingApplicationParser.builder()
                .routerParser(routerParser)
                .defaultParser(outputTopic, defaultParser)
                .routingConditionField(routingConditionField)
                .routingMessageField(routingMessageField)
                .addParser("dummy1", routedParser1, "c")
                .addParser("dummy2", routedParser2, "d")
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(timeProvider)
                .build();

        when(routerParser.parseToResult(metadata, input)).thenReturn(routerParserResult);
        when(defaultParser.parseToResult(metadata, "dummy".getBytes())).thenReturn(routedParserResult1);

        List<ParsingApplicationResult> result = appParser.parse( metadata, input);
        verify(timeProvider, times(1)).getCurrentTimeInMs();
        verify(routerParser, times(1)).parseToResult(metadata, input);
        verify(defaultParser, times(1)).parseToResult(metadata, "dummy".getBytes());
        Assert.assertEquals(outputTopic, result.get(0).getTopic());
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(1, result.get(0).getMessages().size());
        Assert.assertTrue(result.get(0).getMessages().get(0).contains(SiembolMessageFields.PARSING_TIME + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("output_field" + "\":\"routed"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains(SiembolMessageFields.PARSING_TIME + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("original_string" + "\":\"test"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("timestamp" + "\":3"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains(
                SiembolMessageFields.SENSOR_TYPE + "\":\"default-parser\""));
    }

    @Test
    public void testParseMessagesWithRoutedParsers() {
        appParser = RoutingParsingApplicationParser.builder()
                .routerParser(routerParser)
                .defaultParser(outputTopic, defaultParser)
                .routingConditionField(routingConditionField)
                .routingMessageField(routingMessageField)
                .addParser("dummy1", routedParser1, "a")
                .addParser("dummy2", routedParser2, "b")
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(timeProvider)
                .build();

        when(routerParser.parseToResult(metadata, input)).thenReturn(routerParserResult);
        when(routedParser1.parseToResult(metadata, "dummy".getBytes())).thenReturn(routedParserResult1);
        when(routedParser2.parseToResult(metadata, "dummy".getBytes())).thenReturn(routedParserResult2);

        List<ParsingApplicationResult> result = appParser.parse( metadata, input);
        verify(timeProvider, times(1)).getCurrentTimeInMs();
        verify(routedParser1, times(1)).parseToResult(metadata, "dummy".getBytes());

        Assert.assertEquals(1, result.size());
        Assert.assertEquals("dummy1", result.get(0).getTopic());
        Assert.assertTrue(result.get(0).getMessages().get(0).contains(
                SiembolMessageFields.SENSOR_TYPE + "\":\"routed-parser1\""));

    }

    @Test
    public void testParseMessagesWithRoutedParsersMergedFields() {
        appParser = RoutingParsingApplicationParser.builder()
                .routerParser(routerParser)
                .defaultParser(outputTopic, defaultParser)
                .routingConditionField(routingConditionField)
                .routingMessageField(routingMessageField)
                .addParser("dummy1", routedParser1, "a")
                .addParser("dummy2", routedParser2, "b")
                .mergedFields(List.of("timestamp"))
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(timeProvider)
                .build();

        when(routerParser.parseToResult(metadata, input)).thenReturn(routerParserResult);
        when(routedParser1.parseToResult(metadata, "dummy".getBytes())).thenReturn(routedParserResult1);

        List<ParsingApplicationResult> result = appParser.parse( metadata, input);
        verify(timeProvider, times(1)).getCurrentTimeInMs();
        verify(routedParser1, times(1)).parseToResult(metadata, "dummy".getBytes());

        Assert.assertEquals(1, result.size());
        Assert.assertEquals("dummy1", result.get(0).getTopic());
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("timestamp" + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains(
                SiembolMessageFields.SENSOR_TYPE + "\":\"routed-parser1\""));
    }

    @Test
    public void testParseMessagesWithRoutedParsersMergedFieldsGuid() {
        appParser = RoutingParsingApplicationParser.builder()
                .routerParser(routerParser)
                .defaultParser(outputTopic, defaultParser)
                .routingConditionField(routingConditionField)
                .routingMessageField(routingMessageField)
                .addParser("dummy1", routedParser1, "a")
                .addParser("dummy2", routedParser2, "b")
                .mergedFields(List.of("timestamp"))
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(timeProvider)
                .addGuidToMessages(true)
                .build();

        when(routerParser.parseToResult(metadata, input)).thenReturn(routerParserResult);
        when(routedParser1.parseToResult(metadata, "dummy".getBytes())).thenReturn(routedParserResult1);

        List<ParsingApplicationResult> result = appParser.parse( metadata, input);
        verify(timeProvider, times(1)).getCurrentTimeInMs();
        verify(routedParser1, times(1)).parseToResult(metadata, "dummy".getBytes());

        Assert.assertEquals(1, result.size());
        Assert.assertEquals("dummy1", result.get(0).getTopic());
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("timestamp" + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains(
                SiembolMessageFields.SENSOR_TYPE + "\":\"routed-parser1\""));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("guid" + "\":"));
    }

    @Test
    public void testParseMessagesWithRoutedParsersMergedFieldsWithException() {
        appParser = RoutingParsingApplicationParser.builder()
                .routerParser(routerParser)
                .defaultParser(outputTopic, defaultParser)
                .routingConditionField(routingConditionField)
                .routingMessageField(routingMessageField)
                .addParser("dummy1", routedParser1, "a")
                .addParser("dummy2", routedParser2, "b")
                .mergedFields(List.of("timestamp"))
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(timeProvider)
                .build();

        routedParserResult1.setException(new IllegalStateException("test_exception"));
        when(routerParser.parseToResult(metadata, input)).thenReturn(routerParserResult);
        when(routedParser1.parseToResult(metadata, "dummy".getBytes())).thenReturn(routedParserResult1);

        List<ParsingApplicationResult> result = appParser.parse( metadata, input);
        verify(timeProvider, times(1)).getCurrentTimeInMs();
        verify(routedParser1, times(1)).parseToResult(metadata, "dummy".getBytes());

        Assert.assertEquals(1, result.size());
        Assert.assertEquals("error", result.get(0).getTopic());
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("\"failed_sensor_type\":\"routed-parser1\""));
    }
}
