package uk.co.gresearch.siembol.parsers.extractors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.EnumSet;
import java.util.Map;


public class KeyValueExtractorTest {
    private final String name = "test_name";
    private final String field = "test_field";
    private EnumSet<ParserExtractor.ParserExtractorFlags> extractorFlags;
    private EnumSet<KeyValueExtractor.KeyValueExtractorFlags> keyValueFlags;

    private final String simpleNoQuotas = """
      Level=1 Category=UNKNOWN Type=abc
     """;

    private final String simpleQuotes = """
      Threat=Evil Level='A' Category="UN  =KNOWN"
     """;

    private final String nonStandardDelimiters = """
      Threat|Evil,Level|'A',Category|"UN,|KNOWN"
     """;
    

    private final String nonStandardDelimitersEscaping = """
      Threat|Evil,Level|'\\'A',Category|"UN,|KN\\"OWN"
     """;

    @Before
    public void setUp() {
        extractorFlags =
                EnumSet.noneOf(ParserExtractor.ParserExtractorFlags.class);
        keyValueFlags =
                EnumSet.noneOf(KeyValueExtractor.KeyValueExtractorFlags.class);
    }

    @Test
    public void testGoodSimpleNoQuotes() {
        extractorFlags.add(
                ParserExtractor.ParserExtractorFlags.SHOULD_REMOVE_FIELD);
        KeyValueExtractor extractor = KeyValueExtractor.builder()
                .keyValueExtractorFlags(keyValueFlags)
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Assert.assertEquals(name, extractor.getName());
        Assert.assertEquals(field, extractor.getField());
        Assert.assertTrue(extractor.shouldRemoveField());
        Assert.assertFalse(extractor.shouldOverwriteFields());

        Map<String, Object> out = extractor.extract(simpleNoQuotas.trim());
        Assert.assertEquals(3, out.size());
        Assert.assertEquals("1", out.get("Level"));
        Assert.assertEquals("UNKNOWN", out.get("Category"));
        Assert.assertEquals("abc", out.get("Type"));
    }

    @Test
    public void testGoodSimpleQuotesRemove() {
        extractorFlags.add(
                ParserExtractor.ParserExtractorFlags.SHOULD_OVERWRITE_FIELDS);
        keyValueFlags.add(KeyValueExtractor.KeyValueExtractorFlags.QUOTE_VALUE_HANDLING);
        extractorFlags.add(KeyValueExtractor.ParserExtractorFlags.REMOVE_QUOTES);

        KeyValueExtractor extractor = KeyValueExtractor.builder()
                .keyValueExtractorFlags(keyValueFlags)
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Assert.assertEquals(name, extractor.getName());
        Assert.assertEquals(field, extractor.getField());
        Assert.assertFalse(extractor.shouldRemoveField());
        Assert.assertTrue(extractor.shouldOverwriteFields());

        Map<String, Object> out = extractor.extract(simpleQuotes.trim());
        Assert.assertEquals(3, out.size());
        Assert.assertEquals("Evil", out.get("Threat"));
        Assert.assertEquals("UN  =KNOWN", out.get("Category"));
        Assert.assertEquals("A", out.get("Level"));
    }

    @Test
    public void testGoodSimpleQuotesLeave() {
        extractorFlags.add(
                ParserExtractor.ParserExtractorFlags.SHOULD_OVERWRITE_FIELDS);
        keyValueFlags.add(KeyValueExtractor.KeyValueExtractorFlags.QUOTE_VALUE_HANDLING);

        KeyValueExtractor extractor = KeyValueExtractor.builder()
                .keyValueExtractorFlags(keyValueFlags)
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Assert.assertEquals(name, extractor.getName());
        Assert.assertEquals(field, extractor.getField());
        Assert.assertFalse(extractor.shouldRemoveField());
        Assert.assertTrue(extractor.shouldOverwriteFields());

        Map<String, Object> out = extractor.extract(simpleQuotes.trim());
        Assert.assertEquals(3, out.size());
        Assert.assertEquals("Evil", out.get("Threat"));
        Assert.assertEquals("\"UN  =KNOWN\"", out.get("Category"));
        Assert.assertEquals("'A'", out.get("Level"));
    }
    @Test
    public void testGoodNonStandardsDelimiter() {
        extractorFlags.add(
                ParserExtractor.ParserExtractorFlags.SHOULD_OVERWRITE_FIELDS);
        keyValueFlags.add(KeyValueExtractor.KeyValueExtractorFlags.QUOTE_VALUE_HANDLING);

        KeyValueExtractor extractor = KeyValueExtractor.builder()
                .keyValueExtractorFlags(keyValueFlags)
                .wordDelimiter(',')
                .keyValueDelimiter('|')
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Assert.assertEquals(name, extractor.getName());
        Assert.assertEquals(field, extractor.getField());
        Assert.assertFalse(extractor.shouldRemoveField());
        Assert.assertTrue(extractor.shouldOverwriteFields());

        Map<String, Object> out = extractor.extract(nonStandardDelimiters.trim());
        Assert.assertEquals(3, out.size());
        Assert.assertEquals("Evil", out.get("Threat"));
        Assert.assertEquals("\"UN,|KNOWN\"", out.get("Category"));
        Assert.assertEquals("'A'", out.get("Level"));
    }

    @Test
    public void testGoodNonStandardsDelimiterEscaping() {
        extractorFlags.add(
                ParserExtractor.ParserExtractorFlags.SHOULD_OVERWRITE_FIELDS);
        keyValueFlags.add(KeyValueExtractor.KeyValueExtractorFlags.QUOTE_VALUE_HANDLING);
        keyValueFlags.add(KeyValueExtractor.KeyValueExtractorFlags.ESCAPING_HANDLING);

        KeyValueExtractor extractor = KeyValueExtractor.builder()
                .keyValueExtractorFlags(keyValueFlags)
                .wordDelimiter(',')
                .keyValueDelimiter('|')
                .escapedChar('\\')
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Assert.assertEquals(name, extractor.getName());
        Assert.assertEquals(field, extractor.getField());
        Assert.assertFalse(extractor.shouldRemoveField());
        Assert.assertTrue(extractor.shouldOverwriteFields());

        Map<String, Object> out = extractor.extract(nonStandardDelimitersEscaping.trim());
        Assert.assertEquals(3, out.size());
        Assert.assertEquals("Evil", out.get("Threat"));
        Assert.assertEquals("\"UN,|KN\\\"OWN\"", out.get("Category"));
        Assert.assertEquals("'\\'A'", out.get("Level"));
    }

    @Test
    public void testGoodNonStandardsDelimiterEscapingNextKey() {
        extractorFlags.add(
                ParserExtractor.ParserExtractorFlags.SHOULD_OVERWRITE_FIELDS);
        keyValueFlags.add(KeyValueExtractor.KeyValueExtractorFlags.QUOTE_VALUE_HANDLING);
        keyValueFlags.add(KeyValueExtractor.KeyValueExtractorFlags.ESCAPING_HANDLING);
        keyValueFlags.add(KeyValueExtractor.KeyValueExtractorFlags.NEXT_KEY_STRATEGY);

        KeyValueExtractor extractor = KeyValueExtractor.builder()
                .keyValueExtractorFlags(keyValueFlags)
                .wordDelimiter(',')
                .keyValueDelimiter('|')
                .escapedChar('\\')
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Assert.assertEquals(name, extractor.getName());
        Assert.assertEquals(field, extractor.getField());
        Assert.assertFalse(extractor.shouldRemoveField());
        Assert.assertTrue(extractor.shouldOverwriteFields());

        Map<String, Object> out = extractor.extract(nonStandardDelimitersEscaping.trim());
        Assert.assertEquals(3, out.size());
        Assert.assertEquals("Evil", out.get("Threat"));
        Assert.assertEquals("\"UN,|KN\\\"OWN\"", out.get("Category"));
        Assert.assertEquals("'\\'A'", out.get("Level"));
    }

    @Test(expected = IllegalStateException.class)
    public void testWrongEmptyKey() {
        extractorFlags.add(
                ParserExtractor.ParserExtractorFlags.SHOULD_OVERWRITE_FIELDS);
        extractorFlags.add(
                ParserExtractor.ParserExtractorFlags.THROWN_EXCEPTION_ON_ERROR);

        keyValueFlags.add(KeyValueExtractor.KeyValueExtractorFlags.QUOTE_VALUE_HANDLING);

        KeyValueExtractor extractor = KeyValueExtractor.builder()
                .keyValueExtractorFlags(keyValueFlags)
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Assert.assertEquals(name, extractor.getName());
        Assert.assertEquals(field, extractor.getField());

        Map<String, Object> out = extractor.extract("=abc");
        Assert.assertNull(out);
    }

    @Test
    public void testGoodEmptyValue() {
        extractorFlags.add(
                ParserExtractor.ParserExtractorFlags.SHOULD_OVERWRITE_FIELDS);
        keyValueFlags.add(KeyValueExtractor.KeyValueExtractorFlags.QUOTE_VALUE_HANDLING);

        KeyValueExtractor extractor = KeyValueExtractor.builder()
                .keyValueExtractorFlags(keyValueFlags)
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Assert.assertEquals(name, extractor.getName());
        Assert.assertEquals(field, extractor.getField());

        Map<String, Object> out = extractor.extract("a=");
        Assert.assertEquals(1, out.size());
        Assert.assertEquals("", out.get("a"));
    }

    @Test
    public void testGoodEmptyValueQuota() {
        extractorFlags.add(
                ParserExtractor.ParserExtractorFlags.SHOULD_OVERWRITE_FIELDS);
        keyValueFlags.add(KeyValueExtractor.KeyValueExtractorFlags.QUOTE_VALUE_HANDLING);
        extractorFlags.add(KeyValueExtractor.ParserExtractorFlags.REMOVE_QUOTES);

        KeyValueExtractor extractor = KeyValueExtractor.builder()
                .keyValueExtractorFlags(keyValueFlags)
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Assert.assertEquals(name, extractor.getName());
        Assert.assertEquals(field, extractor.getField());

        Map<String, Object> out = extractor.extract("a=\"\"");
        Assert.assertEquals(1, out.size());
        Assert.assertEquals("", out.get("a"));
    }

    @Test
    public void testDuplicateValuesRename() {
        extractorFlags.add(
                ParserExtractor.ParserExtractorFlags.SHOULD_OVERWRITE_FIELDS);
        keyValueFlags.add(KeyValueExtractor.KeyValueExtractorFlags.RENAME_DUPLICATE_KEYS);

        KeyValueExtractor extractor = KeyValueExtractor.builder()
                .keyValueExtractorFlags(keyValueFlags)
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Map<String, Object> out = extractor.extract("a=1 a=2 b=3 b=4 a=5");
        Assert.assertEquals(5, out.size());
        Assert.assertEquals("1", out.get("a"));
        Assert.assertEquals("2", out.get("duplicate_a_1"));
        Assert.assertEquals("5", out.get("duplicate_a_2"));
        Assert.assertEquals("3", out.get("b"));
        Assert.assertEquals("4", out.get("duplicate_b_1"));

    }

    @Test
    public void testDuplicateValueRename() {
        extractorFlags.add(
                ParserExtractor.ParserExtractorFlags.SHOULD_OVERWRITE_FIELDS);
        keyValueFlags.add(KeyValueExtractor.KeyValueExtractorFlags.RENAME_DUPLICATE_KEYS);

        KeyValueExtractor extractor = KeyValueExtractor.builder()
                .keyValueExtractorFlags(keyValueFlags)
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Map<String, Object> out = extractor.extract("a=1 a=2");
        Assert.assertEquals(2, out.size());
        Assert.assertEquals("1", out.get("a"));
        Assert.assertEquals("2", out.get("duplicate_a_1"));
    }

    @Test
    public void testDuplicateValueOverwrite() {
        extractorFlags.add(
                ParserExtractor.ParserExtractorFlags.SHOULD_OVERWRITE_FIELDS);

        KeyValueExtractor extractor = KeyValueExtractor.builder()
                .keyValueExtractorFlags(keyValueFlags)
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Map<String, Object> out = extractor.extract("a=1 a=2 a=3 b=1 b=2");
        Assert.assertEquals(2, out.size());
        Assert.assertEquals("3", out.get("a"));
        Assert.assertEquals("2", out.get("b"));
    }

    @Test
    public void testWrongQuotesFaultTolerant() {
        keyValueFlags.add(KeyValueExtractor.KeyValueExtractorFlags.QUOTE_VALUE_HANDLING);

        KeyValueExtractor extractor = KeyValueExtractor.builder()
                .keyValueExtractorFlags(keyValueFlags)
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Map<String, Object> out = extractor.extract("a=\"dhdhd0ehe");
        Assert.assertEquals("\"dhdhd0ehe", out.get("a"));
    }
}
