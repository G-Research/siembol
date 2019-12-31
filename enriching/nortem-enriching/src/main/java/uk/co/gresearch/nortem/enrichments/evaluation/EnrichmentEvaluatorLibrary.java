package uk.co.gresearch.nortem.enrichments.evaluation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.tuple.Pair;
import uk.co.gresearch.nortem.enrichments.common.EnrichmentCommand;
import uk.co.gresearch.nortem.enrichments.table.EnrichmentsTable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EnrichmentEvaluatorLibrary {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static ArrayList<Pair<String, String>> evaluateCommands(List<EnrichmentCommand> commands,
                                                                   Map<String, EnrichmentsTable> tables) {
        ArrayList<Pair<String, String>> ret = new ArrayList<>();
        for (EnrichmentCommand command : commands) {
            EnrichmentsTable table =  tables.get(command.getTableName());
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

    public static String mergeEnrichments(List<Pair<String, String>> enrichments, String event) throws IOException {
        ObjectNode node = (ObjectNode)OBJECT_MAPPER.readTree(event);
        enrichments.forEach(x -> node.put(x.getKey(), x.getValue()));
        return node.toString();
    }
}
