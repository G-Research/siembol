package uk.co.gresearch.nortem.enrichments.table;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import uk.co.gresearch.nortem.enrichments.common.EnrichmentCommand;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface EnrichmentTable extends Serializable {

    boolean containsKey(String key);

    Optional<List<Pair<String, String>>> getValues(String key, List<String> field);

    default Optional<List<Pair<String, String>>> getValues(EnrichmentCommand command) {
        List<String> fields = command.getTableFields();
        Optional<List<Pair<String, String>>> values = getValues(command.getKey(), fields);
        if (!values.isPresent()) {
            return Optional.empty();
        }

        ArrayList<Pair<String, String>> ret = new ArrayList<>();
        if (command.getTags() != null) {
            command.getTags().forEach(x -> ret.add(x));
        }

        values.get().forEach(x -> ret.add(ImmutablePair.of(
                command.getEnrichedEventNameByTableName(x.getKey()), x.getValue())));
        return Optional.of(ret);
    }
}
