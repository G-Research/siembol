package uk.co.gresearch.nortem.enrichments.evaluation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.tuple.Pair;
import uk.co.gresearch.nortem.enrichments.common.EnrichmentCommand;
import uk.co.gresearch.nortem.enrichments.table.EnrichmentTable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EnrichmentEvaluatorLibrary {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static ArrayList<Pair<String, String>> evaluateCommands(List<EnrichmentCommand> commands,
                                                                   Map<String, EnrichmentTable> tables) {
        ArrayList<Pair<String, String>> ret = new ArrayList<>();
        for (EnrichmentCommand command : commands) {
            EnrichmentTable table =  tables.get(command.getTableName());
            if (table == null) {
                continue;
            }

            Optional<List<Pair<String, String>>> current = table.getValues(command);
            if (current.isPresent()) {
                ret.addAll(current.get());
            }
        }
        return ret;
    }

    public static String mergeEnrichments(String event,
                                          List<Pair<String, String>> enrichments,
                                          Optional<String> timestampField) throws IOException {
        ObjectNode node = (ObjectNode)OBJECT_MAPPER.readTree(event);
        enrichments.forEach(x -> node.put(x.getKey(), x.getValue()));
        if (timestampField.isPresent()) {
            node.put(timestampField.get(), System.currentTimeMillis());
        }
        return node.toString();
    }
}
