package uk.co.gresearch.siembol.parsers.extractors;

import org.junit.Test;

import java.util.EnumSet;
import java.util.Map;

public class JsonPathExtractorTest {
    private final String name = "test_name";
    private final String field = "test_field";
    private final EnumSet<ParserExtractor.ParserExtractorFlags> extractorFlags =
            EnumSet.of(ParserExtractor.ParserExtractorFlags.SHOULD_REMOVE_FIELD);


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

    @Test
    public void testGoodNested() {
        var extractor = JsonPathExtractor.builder()
                .addQuery("moby_dick_price", "$..books[?(@.title =~ /moby dick/i)].price")
                .addQuery("authors", "$.products.books[*].author")
                .addQuery("table_available", "$.products.table.available")
                .addQuery("titles", "$.products.books[*].title")
                .addQuery("books_count", "$..books.length()")
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Map<String, Object> out = extractor.extract(productsJson);
    }
}
