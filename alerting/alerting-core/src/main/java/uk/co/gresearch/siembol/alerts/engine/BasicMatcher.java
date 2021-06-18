package uk.co.gresearch.siembol.alerts.engine;

import uk.co.gresearch.siembol.alerts.common.EvaluationResult;
import java.util.Map;

public abstract class BasicMatcher implements Matcher {
    private final static String MISSING_FIELD_NAME = "Missing field name in a basic matcher";
    private final String fieldName;
    private final boolean isNegated;

    protected BasicMatcher(Builder<?> builder) {
        if (builder.fieldName == null) {
            throw new IllegalArgumentException(MISSING_FIELD_NAME);
        }

        this.fieldName = builder.fieldName;
        this.isNegated = builder.isNegated;
    }

    public EvaluationResult match(Map<String, Object> log) {
        if (log.get(fieldName) == null) {
            return isNegated ? EvaluationResult.MATCH : EvaluationResult.NO_MATCH;
        }

        String fieldValue = log.get(fieldName).toString();
        EvaluationResult result = matchInternally(log, fieldValue);

        if (isNegated) {
            result = EvaluationResult.negate(result);
        }

        return result;
    }

    public boolean canModifyEvent() {
        return false;
    }

    protected abstract EvaluationResult matchInternally(Map<String, Object> map, String fieldValue);

    public static abstract class Builder<T extends BasicMatcher> {
        private String fieldName;
        private boolean isNegated = false;

        public Builder<T> fieldName(String name) {
            this.fieldName = name;
            return this;
        }

        public Builder<T> isNegated(boolean isNegated) {
            this.isNegated = isNegated;
            return this;
        }
        public abstract T build();
    }
}

