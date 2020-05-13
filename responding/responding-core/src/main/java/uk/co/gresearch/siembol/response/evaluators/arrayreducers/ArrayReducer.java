package uk.co.gresearch.siembol.response.evaluators.arrayreducers;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public enum ArrayReducer implements BiFunction<List<Map<String, Object>>, String, Object> {
    FIRST(ArrayReducer::first),
    CONCATENATE(ArrayReducer::concatenate);

    private final BiFunction<List<Map<String, Object>>, String, Object> reducer;

    ArrayReducer(BiFunction<List<Map<String, Object>>, String, Object> reducer) {
        this.reducer = reducer;
    }

    static private Object first(List<Map<String, Object>> array, String field) {
        for (Map<String, Object> obj : array) {
            if (obj == null || !obj.containsKey(field)) {
                continue;
            }
            return obj.get(field);
        }
        return null;
    }

    static private Object concatenate(List<Map<String, Object>> array, String field) {
        StringBuilder sb = new StringBuilder();

        for (Map<String, Object> obj : array) {
            if (obj == null
                    || !obj.containsKey(field)
                    || obj.get(field) == null) {
                continue;
            }

            sb.append(obj.get(field).toString());
            sb.append(',');
        }

        if (sb.length() == 0) {
            return null;
        }

        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    @Override
    public Object apply(List<Map<String, Object>> objects, String field) {
        return reducer.apply(objects, field);
    }
}