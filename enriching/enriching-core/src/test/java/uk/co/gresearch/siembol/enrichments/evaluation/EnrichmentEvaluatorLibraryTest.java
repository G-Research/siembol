package uk.co.gresearch.siembol.enrichments.evaluation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.gresearch.siembol.enrichments.common.EnrichmentCommand;
import uk.co.gresearch.siembol.enrichments.table.EnrichmentTable;

import java.io.IOException;
import java.util.*;

import static org.mockito.Mockito.when;

public class EnrichmentEvaluatorLibraryTest {
    private static final ObjectReader JSON_MAP_READER =
            new ObjectMapper().readerFor(new TypeReference<Map<String, Object>>() {});
    /**
     * {
     *   "timestamp" : 1,
     *   "dummy_bool" : true,
     *   "dummy_str" : "test",
     *   "a" : "conflict"
     * }
     **/
    @Multiline
    public static String simpleEvent;

    private List<Pair<String, String>> enrichments;
    private List<EnrichmentCommand> commands;
    private EnrichmentCommand command;
    private EnrichmentTable table;
    private Map<String, EnrichmentTable> tables;

    @Before
    public void setUp() throws IOException {
        enrichments = new ArrayList<>();
        command = new EnrichmentCommand();
        enrichments.add(Pair.of("a", "b"));
        enrichments.add(Pair.of("c", "d"));
        table = Mockito.mock(EnrichmentTable.class);
        Optional<List<Pair<String, String>>> tableResult = Optional.of(enrichments);
        when(table.getValues(command)).thenReturn(tableResult);
        tables = new HashMap<>();
        tables.put("test_table", table);
        commands = new ArrayList<>();
        commands.add(command);
    }

    @Test
    public void testMergeOK() throws IOException {
        String enriched = EnrichmentEvaluatorLibrary.mergeEnrichments(simpleEvent, enrichments, Optional.empty());
        Assert.assertNotNull(enriched);
        Map<String, Object> map = JSON_MAP_READER.readValue(enriched);
        Assert.assertEquals(5, map.size());
        Assert.assertEquals(1, map.get("timestamp"));
        Assert.assertEquals(true, map.get("dummy_bool"));
        Assert.assertEquals("test", map.get("dummy_str"));
        Assert.assertEquals("b", map.get("a"));
        Assert.assertEquals("d", map.get("c"));
    }

    @Test
    public void testMergeWithTomestampOK() throws IOException {
        String enriched = EnrichmentEvaluatorLibrary.mergeEnrichments(simpleEvent,
                enrichments,
                Optional.of("enrichment_timestamp"));
        Assert.assertNotNull(enriched);
        Map<String, Object> map = JSON_MAP_READER.readValue(enriched);
        Assert.assertEquals(6, map.size());
        Assert.assertEquals(1, map.get("timestamp"));
        Assert.assertEquals(true, map.get("dummy_bool"));
        Assert.assertEquals("test", map.get("dummy_str"));
        Assert.assertEquals("b", map.get("a"));
        Assert.assertEquals("d", map.get("c"));
        Assert.assertTrue(map.get("enrichment_timestamp") instanceof Number);
    }

    @Test
    public void testMergeOKEmptyEvent() throws IOException {
        String enriched = EnrichmentEvaluatorLibrary.mergeEnrichments("{}", enrichments, Optional.empty());
        Assert.assertNotNull(enriched);
        Map<String, Object> map = JSON_MAP_READER.readValue(enriched);
        Assert.assertEquals(2, map.size());
        Assert.assertEquals("b", map.get("a"));
        Assert.assertEquals("d", map.get("c"));
    }

    @Test
    public void testMergeOKNoConflict() throws IOException {
        enrichments.remove(0);
        String enriched = EnrichmentEvaluatorLibrary.mergeEnrichments(simpleEvent, enrichments, Optional.empty());
        Assert.assertNotNull(enriched);
        Map<String, Object> map = JSON_MAP_READER.readValue(enriched);
        Assert.assertEquals(5, map.size());
        Assert.assertEquals(1, map.get("timestamp"));
        Assert.assertEquals(true, map.get("dummy_bool"));
        Assert.assertEquals("test", map.get("dummy_str"));
        Assert.assertEquals("conflict", map.get("a"));
        Assert.assertEquals("d", map.get("c"));
    }

    @Test
    public void testMergeOKEmptyEnrichments() throws IOException {
        enrichments.clear();
        String enriched = EnrichmentEvaluatorLibrary.mergeEnrichments(simpleEvent, enrichments, Optional.empty());
        Assert.assertNotNull(enriched);
        Map<String, Object> map = JSON_MAP_READER.readValue(enriched);
        Assert.assertEquals(4, map.size());
        Assert.assertEquals(1, map.get("timestamp"));
        Assert.assertEquals(true, map.get("dummy_bool"));
        Assert.assertEquals("test", map.get("dummy_str"));
        Assert.assertEquals("conflict", map.get("a"));
    }

    @Test(expected = IOException.class)
    public void testMergeInvalidJsonEvent() throws IOException {
        String enriched = EnrichmentEvaluatorLibrary.mergeEnrichments("INVALID", enrichments, Optional.empty());
    }

    @Test(expected = ClassCastException.class)
    public void testMergeInvalidJsonEventArray() throws IOException {
        String enriched = EnrichmentEvaluatorLibrary.mergeEnrichments("[]", enrichments, Optional.empty());
    }

    @Test
    public void testEvaluateCommand() {
        command.setKey("a");
        command.setTableName("test_table");
        ArrayList<Pair<String, String>> ret = EnrichmentEvaluatorLibrary.evaluateCommands(commands, tables);
        Assert.assertEquals(2, ret.size());
        Assert.assertEquals(ret.get(0), enrichments.get(0));
        Assert.assertEquals(ret.get(1), enrichments.get(1));
    }

    @Test
    public void testEvaluateCommandNoTable() {
        command.setKey("a");
        command.setTableName("unknown");
        ArrayList<Pair<String, String>> ret = EnrichmentEvaluatorLibrary.evaluateCommands(commands, tables);
        Assert.assertTrue(ret.isEmpty());
    }
}
