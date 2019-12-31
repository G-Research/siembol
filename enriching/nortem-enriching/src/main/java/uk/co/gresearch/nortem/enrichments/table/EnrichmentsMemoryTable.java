package uk.co.gresearch.nortem.enrichments.table;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class EnrichmentsMemoryTable implements EnrichmentsTable {
    private static final String INVALID_JSON_TABLE_OBJECT = "Json table should be a json object";
    private static final String INVALID_JSON_TABLE_FIELD_MSG = "Invalid json table field: %s key: %s, " +
            "only strings fields are supported ";

    private final HashMap<String, ArrayList<Pair<String, String>>> table;

    public EnrichmentsMemoryTable(HashMap<String, ArrayList<Pair<String, String>>> table) {
        this.table = table;
    }

    @Override
    public boolean containsKey(String key) {
        return table.containsKey(key.toLowerCase());
    }

    @Override
    public Optional<List<Pair<String, String>>> getValues(String key, List<String> field) {
        List<Pair<String, String>> values = table.get(key.toLowerCase());
        if (values == null) {
            return Optional.empty();
        }

        return Optional.of(values.stream().filter(x -> field.contains(x.getKey())).collect(Collectors.toList()));
    }

    public static EnrichmentsMemoryTable fromJsonStream(InputStream is) throws IOException {
        HashMap<String, ArrayList<Pair<String, String>>> table = new HashMap<>();
        JsonFactory factory = new JsonFactory();

        try(JsonParser parser = factory.createParser(is)) {
            if (parser.nextToken() != JsonToken.START_OBJECT) {
                throw new IllegalArgumentException(INVALID_JSON_TABLE_OBJECT);
            }

            while (parser.nextToken() != null
                    && parser.currentToken() != JsonToken.END_OBJECT) {
                ArrayList<Pair<String, String>> fields = new ArrayList<>();

                String key = parser.getCurrentName().toLowerCase();
                parser.nextToken();
                parser.isExpectedStartObjectToken();

                while (parser.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = parser.getCurrentName();
                    if (parser.nextToken() != JsonToken.VALUE_STRING) {
                        throw new IllegalArgumentException(String.format(INVALID_JSON_TABLE_FIELD_MSG, fieldName, key));
                    }
                    fields.add(Pair.of(fieldName, parser.getText()));
                }
                table.put(key, fields.isEmpty() ? new ArrayList<>() : fields);
            }

            if (parser.currentToken() != JsonToken.END_OBJECT) {
                throw new IllegalArgumentException(INVALID_JSON_TABLE_OBJECT);
            }
        }

        return new EnrichmentsMemoryTable(table);
    }
}
