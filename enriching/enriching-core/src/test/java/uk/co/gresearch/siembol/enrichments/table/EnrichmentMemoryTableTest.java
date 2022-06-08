package uk.co.gresearch.siembol.enrichments.table;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import uk.co.gresearch.siembol.enrichments.common.EnrichmentCommand;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class EnrichmentMemoryTableTest {

    private final String simpleEmptyFields = """    
            {
              "1.2.3.1" : {},
              "1.2.3.2" : {},
              "1.2.3.3" : {},
              "1.2.3.4" : {},
              "1.2.3.5" : {}
            }
            """;

    private final String unsupportedFieldType = """
            {
              "1.2.3.1" : {"is_ioc" : 1}
            }
            """;

    private final String simpleOneField = """
            {
              "1.2.3.1" : { "is_malicious" : "true" },
              "1.2.3.2" : { "is_malicious" : "true"},
              "1.2.3.3" : {"is_malicious" : "false"},
              "1.2.3.4" : {"is_malicious" : "true"},
              "1.2.3.5" : {"is_malicious" : "true"}
            }
            """;

    private final String simpleMixedFields = """
            {
              "1.2.3.1" : { "is_malicious" : "true", "is_ioc" : "false" },
              "1.2.3.2" : {},
              "1.2.3.3" : {"is_malicious" : "true", "is_ioc" : "false", "is_alert" : "true"},
              "1.2.3.4" : {},
              "1.2.3.5" : {"is_malicious" : "true"}
            }
            """;

    private EnrichmentMemoryTable table;

    @Test
    public void simpleTableNoFields() throws IOException {
        try (InputStream is = new ByteArrayInputStream(simpleEmptyFields.getBytes())) {
            table = EnrichmentMemoryTable.fromJsonStream(is);
        }
        Assert.assertTrue(table.containsKey("1.2.3.1"));
        Assert.assertFalse(table.containsKey("1.2.3.8"));
        Optional<List<Pair<String, String>>> values = table.getValues("1.2.3.1", Collections.emptyList());
        Assert.assertTrue(values.isPresent());
        Assert.assertTrue(values.get().isEmpty());

        values = table.getValues("1.2.3.1", Arrays.asList("a", "b", "c"));
        Assert.assertTrue(values.isPresent());
        Assert.assertTrue(values.get().isEmpty());

        values = table.getValues("1.2.3.8", Collections.emptyList());
        Assert.assertFalse(values.isPresent());
    }

    @Test
    public void simpleTableOneField() throws IOException {
        try (InputStream is = new ByteArrayInputStream(simpleOneField.getBytes())) {
            table = EnrichmentMemoryTable.fromJsonStream(is);
        }

        Assert.assertTrue(table.containsKey("1.2.3.1"));
        Assert.assertFalse(table.containsKey("1.2.3.8"));
        Optional<List<Pair<String, String>>> values = table.getValues("1.2.3.1", Collections.emptyList());
        Assert.assertTrue(values.isPresent());
        Assert.assertTrue(values.get().isEmpty());

        values = table.getValues("1.2.3.1", Arrays.asList("is_malicious", "b", "c"));
        Assert.assertTrue(values.isPresent());
        Assert.assertEquals(1, values.get().size());
        Assert.assertEquals("is_malicious", values.get().get(0).getKey());
        Assert.assertEquals("true", values.get().get(0).getValue());

        values = table.getValues("1.2.3.8", Collections.emptyList());
        Assert.assertFalse(values.isPresent());
    }

    @Test
    public void simpleTableMixedFields() throws IOException {
        try (InputStream is = new ByteArrayInputStream(simpleMixedFields.getBytes())) {
            table = EnrichmentMemoryTable.fromJsonStream(is);
        }
        Assert.assertTrue(table.containsKey("1.2.3.1"));
        Assert.assertFalse(table.containsKey("1.2.3.8"));
        Optional<List<Pair<String, String>>> values = table.getValues("1.2.3.1", Collections.emptyList());
        Assert.assertTrue(values.isPresent());
        Assert.assertTrue(values.get().isEmpty());

        values = table.getValues("1.2.3.1", Arrays.asList("is_malicious", "b", "c"));
        Assert.assertTrue(values.isPresent());
        Assert.assertEquals(1, values.get().size());
        Assert.assertEquals("is_malicious", values.get().get(0).getKey());
        Assert.assertEquals("true", values.get().get(0).getValue());

        values = table.getValues("1.2.3.8", Collections.emptyList());
        Assert.assertFalse(values.isPresent());
    }

    @Test
    public void simpleTableMixedFieldsCommand() throws IOException {
        try (InputStream is = new ByteArrayInputStream(simpleMixedFields.getBytes())) {
            table = EnrichmentMemoryTable.fromJsonStream(is);
        }
        EnrichmentCommand command = new EnrichmentCommand();
        command.setTags(new ArrayList<>(List.of(Pair.of("tag_key", "tag_value"))));
        command.setEnrichmentFields(new ArrayList<>(List.of(
                Pair.of("is_malicious", "siembol:is_malicious"))));
        command.setKey("1.2.3.1");
        Optional<List<Pair<String, String>>> values = table.getValues(command);
        Assert.assertTrue(values.isPresent());
        Assert.assertEquals(2, values.get().size());
        Assert.assertEquals("tag_key", values.get().get(0).getKey());
        Assert.assertEquals("tag_value", values.get().get(0).getValue());
        Assert.assertEquals("siembol:is_malicious", values.get().get(1).getKey());
        Assert.assertEquals("true", values.get().get(1).getValue());
    }

    @Test
    public void simpleTableMixedFieldsCommandMissing() throws IOException {
        try (InputStream is = new ByteArrayInputStream(simpleMixedFields.getBytes())) {
            table = EnrichmentMemoryTable.fromJsonStream(is);
        }
        EnrichmentCommand command = new EnrichmentCommand();
        command.setTags(new ArrayList<>(List.of(Pair.of("tag_key", "tag_value"))));
        command.setEnrichmentFields(new ArrayList<>(List.of(
                Pair.of("is_malicious", "siembol:is_malicious"))));
        command.setKey("1.2.3.15");
        Optional<List<Pair<String, String>>> values = table.getValues(command);
        Assert.assertFalse(values.isPresent());
    }

    @Test(expected = com.fasterxml.jackson.core.JsonParseException.class)
    public void invalidJson() throws IOException {
        try (InputStream is = new ByteArrayInputStream("INVALID".getBytes())) {
            table = EnrichmentMemoryTable.fromJsonStream(is);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidJsonTable() throws IOException {
        try (InputStream is = new ByteArrayInputStream("[]".getBytes())) {
            table = EnrichmentMemoryTable.fromJsonStream(is);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void unsupportedFieldType() throws IOException {
        try (InputStream is = new ByteArrayInputStream(unsupportedFieldType.getBytes())) {
            table = EnrichmentMemoryTable.fromJsonStream(is);
        }
    }

    @Test(expected = com.fasterxml.jackson.core.io.JsonEOFException.class)
    public void invalidJsonTable2() throws IOException {
        String trimString = simpleOneField.trim();
        try (InputStream is = new ByteArrayInputStream(trimString
                .substring(0, trimString.length() - 1).getBytes())) {
            EnrichmentMemoryTable.fromJsonStream(is);
        }
    }

    @Test
    public void tableMetadata() throws IOException {
        try (InputStream is = new ByteArrayInputStream(simpleMixedFields.getBytes())) {
            table = EnrichmentMemoryTable.fromJsonStream(is);
        }
        Assert.assertTrue(table.containsKey(TableMetadata.TABLE_METADATA_KEY));
        Optional<List<Pair<String, String>>> values = table.getValues(TableMetadata.TABLE_METADATA_KEY,
                List.of(TableMetadata.LAST_UPDATE_FIELD_NAME,
                        TableMetadata.SIZE_FIELD_NAME,
                        TableMetadata.NUMBER_OF_FIELDS_FIELD_NAME,
                        TableMetadata.NUMBER_OF_ROWS_FIELD_NAME,
                        TableMetadata.NUMBER_OF_VALUES_FIELD_NAME));

        Assert.assertTrue(values.isPresent());
        Assert.assertEquals(5, values.get().size());

        var valuesMap = values.get().stream()
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
        Assert.assertEquals(5, valuesMap.size());
        Assert.assertNotNull(valuesMap.get(TableMetadata.LAST_UPDATE_FIELD_NAME));
        Assert.assertEquals("5", valuesMap.get(TableMetadata.NUMBER_OF_ROWS_FIELD_NAME));
        Assert.assertEquals("3", valuesMap.get(TableMetadata.NUMBER_OF_FIELDS_FIELD_NAME));
        Assert.assertEquals("6", valuesMap.get(TableMetadata.NUMBER_OF_VALUES_FIELD_NAME));
        Assert.assertEquals("225", valuesMap.get(TableMetadata.SIZE_FIELD_NAME));
    }
}
