package uk.co.gresearch.siembol.enrichments.table;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import uk.co.gresearch.siembol.enrichments.common.EnrichmentCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
/**
 * An object that represents and enrichment table
 *
 * <p>This interface provides functionality for representing an enrichment table.
 * It provides lookup into the table for an enrichment key.
 * Moreover, it evaluates an enrichment command on the table.
 *
 * @author  Marian Novotny
 */
public interface EnrichmentTable {
    /**
     * Provides information whether the table contains a key
     *
     * @param key an input string for table lookup
     * @return true if the table contains the key, otherwise false
     */
    boolean containsKey(String key);

    /**
     * Gets a row in the table based on the key
     *
     * @param key an input string for table lookup
     * @param fields the list of field names that should be included in the result
     * @return Optional.Empty() if the table does not contain the key,
     *         otherwise the row selected by the key that contains only the fields (columns) from the input list
     */
    Optional<List<Pair<String, String>>> getValues(String key, List<String> fields);

    /**
     * Gets key-value pairs from the table after evaluating an enrichment command
     *
     * @param command an enrichment command
     * @return Optional.Empty() if the table does not contain the key from the command,
     *         otherwise a list of key-value pairs after joining the table and evaluating the command.
     */
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
