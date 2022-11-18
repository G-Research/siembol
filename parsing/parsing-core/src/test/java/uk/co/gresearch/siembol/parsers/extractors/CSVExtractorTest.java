package uk.co.gresearch.siembol.parsers.extractors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

public class CSVExtractorTest {
    private final String name = "test_name";
    private final String field = "test_field";
    private List<ColumnNames> columnNamesList;
    private EnumSet<ParserExtractor.ParserExtractorFlags> extractorFlags;

    private final String simpleNoQuotes = """
      a,bb,ccc,,ee
     """;

    private final String simpleEmptyLastColumn = """
      a,bb,ccc,,ee,
     """;

    private final String stringDelimiterEmptyLastColumn = """
      a||bb||ccc||||ee||
     """;

    private final String simpleChangedDelimiter = """
      a;bb;ccc;;ee
     """;

    private final String simpleQuotes = """
      a,"b,,,b",cc""c,"","ee
     """;

    private final String noQuotesStringDelimiter = """
      a||bb||ccc||||ee
     """;

    @Before
    public void setUp() {
        extractorFlags =
                EnumSet.noneOf(ParserExtractor.ParserExtractorFlags.class);
        columnNamesList = new ArrayList<>();
    }

    @Test
    public void testGoodSimpleNoQuotes() {
        extractorFlags.add(
                ParserExtractor.ParserExtractorFlags.SHOULD_REMOVE_FIELD);
        columnNamesList.add(new ColumnNames(
                Arrays.asList("c1", "c2", "c3", "c4", "c5", "c6")));
        CSVExtractor extractor = CSVExtractor.builder()
                .columnNames(columnNamesList)
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Assert.assertEquals(name, extractor.getName());
        Assert.assertEquals(field, extractor.getField());
        Assert.assertTrue(extractor.shouldRemoveField());
        Assert.assertFalse(extractor.shouldOverwriteFields());

        Map<String, Object> out = extractor.extract(simpleEmptyLastColumn.trim());
        Assert.assertEquals(6, out.size());
        Assert.assertEquals("a", out.get("c1"));
        Assert.assertEquals("bb", out.get("c2"));
        Assert.assertEquals("ccc", out.get("c3"));
        Assert.assertEquals("", out.get("c4"));
        Assert.assertEquals("ee", out.get("c5"));
        Assert.assertEquals("", out.get("c6"));
    }

    @Test
    public void testGoodSimpleNoQuotesEmptyColumnENd() {
        extractorFlags.add(
                ParserExtractor.ParserExtractorFlags.SHOULD_REMOVE_FIELD);
        columnNamesList.add(new ColumnNames(
                Arrays.asList("c1", "c2", "c3", "c4", "c5")));
        CSVExtractor extractor = CSVExtractor.builder()
                .columnNames(columnNamesList)
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Assert.assertEquals(name, extractor.getName());
        Assert.assertEquals(field, extractor.getField());
        Assert.assertTrue(extractor.shouldRemoveField());
        Assert.assertFalse(extractor.shouldOverwriteFields());

        Map<String, Object> out = extractor.extract(simpleNoQuotes.trim());
        Assert.assertEquals(5, out.size());
        Assert.assertEquals("a", out.get("c1"));
        Assert.assertEquals("bb", out.get("c2"));
        Assert.assertEquals("ccc", out.get("c3"));
        Assert.assertEquals("", out.get("c4"));
        Assert.assertEquals("ee", out.get("c5"));
    }

    @Test
    public void testGoodWordDelimiterNoQuotes() {
        columnNamesList.add(new ColumnNames(
                Arrays.asList("c1", "c2", "c3", "c4", "c5")));
        CSVExtractor extractor = CSVExtractor.builder()
                .columnNames(columnNamesList)
                .wordDelimiter("||")
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Map<String, Object> out = extractor.extract(noQuotesStringDelimiter.trim());
        Assert.assertEquals(5, out.size());
        Assert.assertEquals("a", out.get("c1"));
        Assert.assertEquals("bb", out.get("c2"));
        Assert.assertEquals("ccc", out.get("c3"));
        Assert.assertEquals("", out.get("c4"));
        Assert.assertEquals("ee", out.get("c5"));
    }

    @Test
    public void testGoodWordDelimiterEndEmptyLastColumn() {
        columnNamesList.add(new ColumnNames(
                Arrays.asList("c1", "c2", "c3", "c4", "c5", "c6")));
        CSVExtractor extractor = CSVExtractor.builder()
                .columnNames(columnNamesList)
                .wordDelimiter("||")
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Map<String, Object> out = extractor.extract(stringDelimiterEmptyLastColumn.trim());
        Assert.assertEquals(6, out.size());
        Assert.assertEquals("a", out.get("c1"));
        Assert.assertEquals("bb", out.get("c2"));
        Assert.assertEquals("ccc", out.get("c3"));
        Assert.assertEquals("", out.get("c4"));
        Assert.assertEquals("ee", out.get("c5"));
        Assert.assertEquals("", out.get("c6"));
    }

    @Test
    public void testGoodSimpleNoQuotesSkipEmpty() {
        extractorFlags.add(
                ParserExtractor.ParserExtractorFlags.SHOULD_REMOVE_FIELD);
        extractorFlags.add(
                ParserExtractor.ParserExtractorFlags.SKIP_EMPTY_VALUES);

        columnNamesList.add(new ColumnNames(
                Arrays.asList("c1", "c2", "c3", "c4", "c5")));
        CSVExtractor extractor = CSVExtractor.builder()
                .columnNames(columnNamesList)
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Map<String, Object> out = extractor.extract(simpleNoQuotes.trim());
        Assert.assertEquals(4, out.size());
        Assert.assertEquals("a", out.get("c1"));
        Assert.assertEquals("bb", out.get("c2"));
        Assert.assertEquals("ccc", out.get("c3"));
        Assert.assertEquals("ee", out.get("c5"));
    }

    @Test
    public void testGoodSimpleNoQuotesSkipColumn() {
        extractorFlags.add(
                ParserExtractor.ParserExtractorFlags.SHOULD_REMOVE_FIELD);


        columnNamesList.add(new ColumnNames(
                Arrays.asList("c1", "c2", "SKIP", "SKIP", "SKIP")));
        CSVExtractor extractor = CSVExtractor.builder()
                .columnNames(columnNamesList)
                .skippingColumnName("SKIP")
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Map<String, Object> out = extractor.extract(simpleNoQuotes.trim());
        Assert.assertEquals(2, out.size());
        Assert.assertEquals("a", out.get("c1"));
        Assert.assertEquals("bb", out.get("c2"));
    }

    @Test
    public void testGoodSimpleNoQuotesUnknownColumns() {
        extractorFlags.add(
                ParserExtractor.ParserExtractorFlags.SHOULD_REMOVE_FIELD);
        columnNamesList.add(new ColumnNames(
                Arrays.asList("c1", "c2", "c3")));
        CSVExtractor extractor = CSVExtractor.builder()
                .columnNames(columnNamesList)
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Map<String, Object> out = extractor.extract(simpleNoQuotes.trim());
        Assert.assertEquals(5, out.size());
        Assert.assertEquals("a", out.get("unknown_test_name_1"));
        Assert.assertEquals("bb", out.get("unknown_test_name_2"));
        Assert.assertEquals("ccc", out.get("unknown_test_name_3"));
        Assert.assertEquals("", out.get("unknown_test_name_4"));
        Assert.assertEquals("ee", out.get("unknown_test_name_5"));
    }

    @Test(expected = IllegalStateException.class)
    public void testGoodSimpleNoQuotesExceptionUnknownColumns() {
        extractorFlags.add(
                ParserExtractor.ParserExtractorFlags.SHOULD_REMOVE_FIELD);
        extractorFlags.add(
                ParserExtractor.ParserExtractorFlags.THROWN_EXCEPTION_ON_ERROR);
        columnNamesList.add(new ColumnNames(
                Arrays.asList("c1", "c2", "c3")));
        CSVExtractor extractor = CSVExtractor.builder()
                .columnNames(columnNamesList)
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Map<String, Object> out = extractor.extract(simpleNoQuotes.trim());
        Assert.assertNull(out);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongColumns() {
        CSVExtractor extractor = CSVExtractor.builder()
                .columnNames(null)
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Map<String, Object> out = extractor.extract(simpleNoQuotes.trim());
        Assert.assertNull(out);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongColumns2() {
        CSVExtractor extractor = CSVExtractor.builder()
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Map<String, Object> out = extractor.extract(simpleNoQuotes.trim());
        Assert.assertNull(out);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongColumns3() {
        columnNamesList.add(new ColumnNames(
                Arrays.asList("c1", "c1")));
        CSVExtractor extractor = CSVExtractor.builder()
                .columnNames(columnNamesList)
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Map<String, Object> out = extractor.extract(simpleNoQuotes.trim());
        Assert.assertNull(out);
    }

    @Test
    public void testGoodSimpleChangedDelimiter() {
        extractorFlags.add(
                ParserExtractor.ParserExtractorFlags.SHOULD_REMOVE_FIELD);
        columnNamesList.add(new ColumnNames(
                Arrays.asList("c1", "c2", "c3", "c4", "c5")));
        CSVExtractor extractor = CSVExtractor.builder()
                .columnNames(columnNamesList)
                .wordDelimiter(";")
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Assert.assertEquals(name, extractor.getName());
        Assert.assertEquals(field, extractor.getField());
        Assert.assertTrue(extractor.shouldRemoveField());
        Assert.assertFalse(extractor.shouldOverwriteFields());

        Map<String, Object> out = extractor.extract(simpleChangedDelimiter.trim());
        Assert.assertEquals(5, out.size());
        Assert.assertEquals("a", out.get("c1"));
        Assert.assertEquals("bb", out.get("c2"));
        Assert.assertEquals("ccc", out.get("c3"));
        Assert.assertEquals("", out.get("c4"));
        Assert.assertEquals("ee", out.get("c5"));
    }

    @Test
    public void testRemoveQuotes() {
        extractorFlags.add(
                ParserExtractor.ParserExtractorFlags.SHOULD_REMOVE_FIELD);
        extractorFlags.add(
                ParserExtractor.ParserExtractorFlags.REMOVE_QUOTES);

        columnNamesList.add(new ColumnNames(
                Arrays.asList("c1", "c2", "c3", "c4", "c5")));
        CSVExtractor extractor = CSVExtractor.builder()
                .columnNames(columnNamesList)
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Map<String, Object> out = extractor.extract(simpleQuotes.trim());
        Assert.assertEquals(5, out.size());
        Assert.assertEquals("a", out.get("c1"));
        Assert.assertEquals("b,,,b", out.get("c2"));
        Assert.assertEquals("cc\"\"c", out.get("c3"));
        Assert.assertEquals("", out.get("c4"));
        Assert.assertEquals("\"ee", out.get("c5"));
    }

    @Test
    public void testLeaveQuotes() {
        extractorFlags.add(
                ParserExtractor.ParserExtractorFlags.SHOULD_REMOVE_FIELD);
        columnNamesList.add(new ColumnNames(
                Arrays.asList("c1", "c2", "c3", "c4", "c5")));
        CSVExtractor extractor = CSVExtractor.builder()
                .columnNames(columnNamesList)
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Map<String, Object> out = extractor.extract(simpleQuotes.trim());
        Assert.assertEquals(5, out.size());
        Assert.assertEquals("a", out.get("c1"));
        Assert.assertEquals("\"b,,,b\"", out.get("c2"));
        Assert.assertEquals("cc\"\"c", out.get("c3"));
        Assert.assertEquals("\"\"", out.get("c4"));
        Assert.assertEquals("\"ee", out.get("c5"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongFilter() {
        columnNamesList.add(new ColumnNames(
                Arrays.asList("c1", "c1"),
                new  AbstractMap.SimpleEntry<> (2, "a")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongFilter2() {
        columnNamesList.add(new ColumnNames(
                Arrays.asList("c1", "c1"),
                new  AbstractMap.SimpleEntry<> (1, null)));
    }
}
