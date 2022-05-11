package uk.co.gresearch.siembol.parsers.extractors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.EnumSet;
import java.util.Map;

public class JsonPathExtractorTest {
    private final String name = "test_name";
    private final String field = "test_field";
    private EnumSet<ParserExtractor.ParserExtractorFlags> extractorFlags;
    private EnumSet<JsonPathExtractor.JsonPathExtractorFlags> jsonPathExtractorFlags;

    private final String productsJson = """
                    {
                      "products": {
                        "books": [
                          {
                            "category": "reference",
                            "author": "Nigel Rees",
                            "title": "Sayings of the Century",
                            "available" : true,
                            "price": 5
                          },
                          {
                            "category": "fiction",
                            "author": "William Gibson",
                            "title": "Neuromancer",
                            "available" : false,
                            "price": 7
                          },
                          {
                            "category": "fiction",
                            "author": "Herman Melville",
                            "title": "Moby Dick",
                            "available" : false,
                            "price": 11
                          },
                          {
                            "category": "fiction",
                            "author": "J. R. R. Tolkien",
                            "title": "The Lord of the Rings",
                            "available" : false,
                            "price": 20.5
                          }
                        ],
                        "table": {
                          "color": "white",
                          "price": 15,
                          "available" : true
                        }
                      },
                      "version" : 1
                    }
            """;

    @Before
    public void setUp() {
        extractorFlags = EnumSet.of(ParserExtractor.ParserExtractorFlags.SHOULD_REMOVE_FIELD);
        jsonPathExtractorFlags = EnumSet.noneOf(JsonPathExtractor.JsonPathExtractorFlags.class);
    }
    @Test
    public void extractingOk() {
        var extractor = JsonPathExtractor.builder()
                .addQuery("moby_dick_price", "$..books[?(@.title =~ /moby dick/i)].price")
                .addQuery("moby_dick_category", "$..books[?(@.title =~ /moby dick/i)].category")
                .addQuery("authors", "$.products.books[*].author")
                .addQuery("dummy", "dummy")
                .addQuery("table_available", "$.products.table.available")
                .addQuery("titles", "$.products.books[*].title")
                .addQuery("books_count", "$..books.length()")
                .jsonPathExtractorFlags(jsonPathExtractorFlags)
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Map<String, Object> out = extractor.extract(productsJson);
        Assert.assertEquals(6, out.size());
        Assert.assertEquals(true, out.get("table_available"));
        Assert.assertEquals(4, out.get("books_count"));
        Assert.assertEquals("Sayings of the Century,Neuromancer,Moby Dick,The Lord of the Rings",
                out.get("titles"));
        Assert.assertEquals(11, out.get("moby_dick_price"));
        Assert.assertEquals("fiction", out.get("moby_dick_category"));
        Assert.assertEquals("Nigel Rees,William Gibson,Herman Melville,J. R. R. Tolkien", out.get("authors"));
        Assert.assertNotNull(out);
    }

    @Test
    public void extractingNoResultOk() {
        var extractor = JsonPathExtractor.builder()
                .addQuery("dummy", "dummy")
                .jsonPathExtractorFlags(jsonPathExtractorFlags)
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Map<String, Object> out = extractor.extract(productsJson);
        Assert.assertTrue(out.isEmpty());
    }

    @Test
    public void extractingNoResultJsonObjectOk() {
        var extractor = JsonPathExtractor.builder()
                .addQuery("first_book", "$.products.books[0]")
                .jsonPathExtractorFlags(jsonPathExtractorFlags)
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Map<String, Object> out = extractor.extract(productsJson);
        Assert.assertTrue(out.isEmpty());
    }

    @Test(expected = IllegalStateException.class)
    public void extractingNoResultJsonObjectThrowException() {
        jsonPathExtractorFlags.add(JsonPathExtractor.JsonPathExtractorFlags.AT_LEAST_ONE_QUERY_RESULT);
        extractorFlags.add(ParserExtractor.ParserExtractorFlags.THROWN_EXCEPTION_ON_ERROR);
        var extractor = JsonPathExtractor.builder()
                .addQuery("first_book", "$.products.books[0]")
                .jsonPathExtractorFlags(jsonPathExtractorFlags)
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        extractor.extract(productsJson);
    }

    @Test
    public void extractingNoResultOk2() {
        jsonPathExtractorFlags.add(JsonPathExtractor.JsonPathExtractorFlags.AT_LEAST_ONE_QUERY_RESULT);
        var extractor = JsonPathExtractor.builder()
                .addQuery("dummy", "dummy")
                .jsonPathExtractorFlags(jsonPathExtractorFlags)
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Map<String, Object> out = extractor.extract(productsJson);
        Assert.assertTrue(out.isEmpty());
    }

    @Test(expected = IllegalStateException.class)
    public void extractingNoResultException() {
        jsonPathExtractorFlags.add(JsonPathExtractor.JsonPathExtractorFlags.AT_LEAST_ONE_QUERY_RESULT);
        extractorFlags.add(ParserExtractor.ParserExtractorFlags.THROWN_EXCEPTION_ON_ERROR);
        var extractor = JsonPathExtractor.builder()
                .addQuery("dummy", "dummy")
                .jsonPathExtractorFlags(jsonPathExtractorFlags)
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        extractor.extract(productsJson);
    }
    @Test
    public void extractingInvalidJsonNoException() {
        var extractor = JsonPathExtractor.builder()
                .addQuery("moby_dick_price", "$..books[?(@.title =~ /moby dick/i)].price")
                .jsonPathExtractorFlags(jsonPathExtractorFlags)
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Map<String, Object> out = extractor.extract("INVALID");
        Assert.assertTrue(out.isEmpty());
    }

    @Test(expected = IllegalStateException.class)
    public void extractingThrowException() {
        extractorFlags.add(ParserExtractor.ParserExtractorFlags.THROWN_EXCEPTION_ON_ERROR);
        var extractor = JsonPathExtractor.builder()
                .addQuery("moby_dick_price", "$..books[?(@.title =~ /moby dick/i)].price")
                .jsonPathExtractorFlags(jsonPathExtractorFlags)
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        extractor.extract("INVALID");
    }

    @Test(expected = IllegalArgumentException.class)
    public void builderEmptyQueries() {
        JsonPathExtractor.builder()
                .jsonPathExtractorFlags(jsonPathExtractorFlags)
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void builderEmptyQueryOutputField() {
        JsonPathExtractor.builder()
                .addQuery("", "$..books[?(@.title =~ /moby dick/i)].price")
                .jsonPathExtractorFlags(jsonPathExtractorFlags)
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void builderEmptyQuery() {
        JsonPathExtractor.builder()
                .addQuery("a", "")
                .jsonPathExtractorFlags(jsonPathExtractorFlags)
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void builderNullQueryOutputField() {
        JsonPathExtractor.builder()
                .addQuery(null, "$..books[?(@.title =~ /moby dick/i)].price")
                .jsonPathExtractorFlags(jsonPathExtractorFlags)
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();
    }
}
