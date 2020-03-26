package uk.co.gresearch.nortem.parsers.extractors;

import java.util.Map;
import java.util.HashMap;

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
