package uk.co.gresearch.siembol.response.evaluators.arrayreducers;

import uk.co.gresearch.siembol.common.utils.FieldFilter;
import uk.co.gresearch.siembol.common.utils.PatternFilter;
import uk.co.gresearch.siembol.response.common.Evaluable;
import uk.co.gresearch.siembol.response.common.RespondingResult;
import uk.co.gresearch.siembol.response.common.ResponseAlert;
import uk.co.gresearch.siembol.response.common.ResponseEvaluationResult;
import uk.co.gresearch.siembol.response.model.ArrayReducerTypeDto;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ArrayReducerEvaluator implements Evaluable {
    private final String arrayFieldName;
    private final String fieldFormatMessage;
    private final ArrayReducer reducer;
    private final FieldFilter fieldFilter;


    public ArrayReducerEvaluator(Builder builder) {
        this.arrayFieldName = builder.arrayFieldName;
        this.fieldFormatMessage = builder.fieldFormatMessage;
        this.reducer = builder.reducer;
        this.fieldFilter = builder.fieldFilter;
    }

    @Override
    public RespondingResult evaluate(ResponseAlert alert) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> array = (List<Map<String, Object>>)alert.get(arrayFieldName);
            if (array == null) {
                return RespondingResult.fromEvaluationResult(ResponseEvaluationResult.NO_MATCH, alert);
            }
            Set<String> fieldSet = new HashSet<>();
            array.forEach(x -> fieldSet.addAll(x.keySet()));
            fieldSet.removeIf(x -> !fieldFilter.match(x));
            if (fieldSet.isEmpty()) {
                return RespondingResult.fromEvaluationResult(ResponseEvaluationResult.NO_MATCH, alert);
            }
            for (String field : fieldSet) {
                Object obj = reducer.apply(array, field);
                if (obj != null) {
                    alert.put(String.format(fieldFormatMessage, field), obj);
                }
            }

            return RespondingResult.fromEvaluationResult(ResponseEvaluationResult.MATCH, alert);
        } catch (Exception e) {
            return RespondingResult.fromException(e);
        }
    }

    public static class Builder {
        private static final String MISSING_ARGUMENT_MSG = "missing Array reducer evaluator attributes";
        private String arrayFieldName;
        private String prefixName;
        private String fieldFormatMessage;
        private String delimiter;
        private ArrayReducerTypeDto reducerType;
        private ArrayReducer reducer;

        private FieldFilter fieldFilter = x -> true;

        public Builder prefixName(String prefixName) {
            this.prefixName = prefixName;
            return this;
        }

        public Builder delimiter(String delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        public Builder arrayFieldName(String arrayFieldName) {
            this.arrayFieldName = arrayFieldName;
            return this;
        }

        public Builder reducerType(ArrayReducerTypeDto reducerType) {
            this.reducerType = reducerType;
            return this;
        }

        public Builder patternFilter(List<String> includingFields, List<String> excludingFields) {
            fieldFilter = PatternFilter.create(includingFields, excludingFields);
            return this;
        }

        public ArrayReducerEvaluator build() {
            if (arrayFieldName == null
                    || prefixName == null
                    || delimiter == null
                    || reducerType == null) {
                throw new IllegalArgumentException(MISSING_ARGUMENT_MSG);
            }

            reducer = reducerType == ArrayReducerTypeDto.FIRST_FIELD
                    ? ArrayReducer.FIRST
                    : ArrayReducer.CONCATENATE;

            fieldFormatMessage = String.format("%s%s%%s", prefixName, delimiter);
            return new ArrayReducerEvaluator(this);
        }

    }

}
