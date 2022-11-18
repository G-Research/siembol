package uk.co.gresearch.siembol.parsers.extractors;

import org.junit.Assert;
import org.junit.Test;
import java.util.EnumSet;
import java.util.Map;

public class JsonExtractorTest {
    private final String name = "test_name";
    private final String field = "test_field";
    private final EnumSet<ParserExtractor.ParserExtractorFlags> extractorFlags =
            EnumSet.of(ParserExtractor.ParserExtractorFlags.SHOULD_REMOVE_FIELD);


    private final String simpleJson = """
      {"key1":"bbb", "key2":2, "key3": true, "key4": {"nested1": { "neste21" : 1, "nested22" : true, "nested23" : {}, "nested24": []} }} {"ignored": "hopefully"}
     """;

    private final String simpleArrayJson = """
       {"key1":"bbb", "key2": {"nested1": [{"order" : 1}, {"order" : 2}]}} {"ignored": "hopefully"}
     """;

    @Test
    public void testGoodNested() {
        JsonExtractor extractor = JsonExtractor.builder()
                .nestedSeparator(":")
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Map<String, Object> out = extractor.extract(simpleJson.trim());
        Assert.assertEquals(5, out.size());
        Assert.assertEquals("bbb", out.get("key1"));
        Assert.assertEquals(2, out.get("key2"));
        Assert.assertEquals(true, out.get("key3"));
        Assert.assertEquals(1, out.get("key4:nested1:neste21"));
        Assert.assertEquals(true, out.get("key4:nested1:nested22"));

        Assert.assertEquals(name, extractor.getName());
        Assert.assertEquals(field, extractor.getField());
        Assert.assertTrue(extractor.shouldRemoveField());
        Assert.assertFalse(extractor.shouldOverwriteFields());
    }

    @Test
    public void testGoodNestedStartingPathArrow() {
        JsonExtractor extractor = JsonExtractor.builder()
                .nestedSeparator("=>")
                .pathPrefix("begin")
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();


        Map<String, Object> out = extractor.extract(simpleJson.trim());
        Assert.assertEquals(5, out.size());
        Assert.assertEquals("bbb", out.get("begin=>key1"));
        Assert.assertEquals(2, out.get("begin=>key2"));
        Assert.assertEquals(true, out.get("begin=>key3"));
        Assert.assertEquals(1, out.get("begin=>key4=>nested1=>neste21"));
        Assert.assertEquals(true, out.get("begin=>key4=>nested1=>nested22"));

        Assert.assertEquals(name, extractor.getName());
        Assert.assertEquals(field, extractor.getField());
        Assert.assertTrue(extractor.shouldRemoveField());
        Assert.assertFalse(extractor.shouldOverwriteFields());
    }

    @Test
    public void testGoodNestedArray() {
        JsonExtractor extractor = JsonExtractor.builder()
                .nestedSeparator(":")
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();


        Map<String, Object> out = extractor.extract(simpleArrayJson.trim());
        Assert.assertEquals(3, out.size());
        Assert.assertEquals("bbb", out.get("key1"));
        Assert.assertEquals(1, out.get("key2:nested1:0:order"));
        Assert.assertEquals(2, out.get("key2:nested1:1:order"));
    }
}
