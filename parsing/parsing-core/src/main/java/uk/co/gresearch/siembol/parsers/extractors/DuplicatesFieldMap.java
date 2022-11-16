package uk.co.gresearch.siembol.parsers.extractors;

import java.util.Map;
import java.util.HashMap;
/**
 * An object that represents structure for handling duplicate fields
 *
 * <p>This object represents helper structure for handling duplicate fields used in parser extractors
 *
 * @author  Marian Novotny
 */
public class DuplicatesFieldMap {
    private final Map<String, Integer> indexMap = new HashMap<>();

    public int getIndex(String name) {
        if (!indexMap.containsKey(name)) {
            indexMap.put(name, 1);
            return 1;
        }

        int current = indexMap.get(name) + 1;
        indexMap.put(name, current);
        return current;
    }

    public void clear() {
        indexMap.clear();
    }
}
