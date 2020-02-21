package uk.co.gresearch.nortem.enrichments.evaluation;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.nortem.enrichments.common.EnrichmentCommand;
import uk.co.gresearch.nortem.nikita.common.EvaluationResult;
import uk.co.gresearch.nortem.nikita.common.NikitaResult;
import uk.co.gresearch.nortem.nikita.engine.NikitaEngineLibrary;
import uk.co.gresearch.nortem.nikita.engine.Rule;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EnrichingRule extends Rule {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String COMMAND_FIELD_ERROR_MSG = "Enrichment command field already in event: %s";
    private final String tableName;
    private final String key;
    private final ArrayList<Pair<String, String>> enrichmentTags;
    private final ArrayList<Pair<String, String>> enrichmentFields;

    private EnrichingRule(Builder<?> builder) {
        super(builder);
        this.tableName = builder.tableName;
        this.key = builder.key;
        this.enrichmentTags = builder.enrichmentTags;
        this.enrichmentFields = builder.enrichmentFields;
    }

    private Optional<EnrichmentCommand> createEnrichmentCommand(Map<String, Object> log) {
        Optional<String> currentKey = NikitaEngineLibrary.substitute(log, key);
        if (!currentKey.isPresent()) {
            return Optional.empty();
        }

        EnrichmentCommand ret = new EnrichmentCommand();
        ret.setKey(currentKey.get());
        ret.setTableName(tableName);
        ret.setTags(enrichmentTags);
        ret.setEnrichmentFields(enrichmentFields);
        return Optional.of(ret);
    }

    @Override
    public NikitaResult match(Map<String, Object> log) {
        NikitaResult result = super.match(log);
        if (result.getStatusCode() != NikitaResult.StatusCode.OK
                || result.getAttributes().getEvaluationResult() != EvaluationResult.MATCH ) {
            return result;
        }

        Optional<EnrichmentCommand> command = createEnrichmentCommand(result.getAttributes().getEvent());
        if (!command.isPresent()) {
            return NikitaResult.fromEvaluationResult(EvaluationResult.NO_MATCH, result.getAttributes().getEvent());
        }

        Map<String, Object> event = result.getAttributes().getEvent();
        if (event.containsKey(EnrichmentFields.ENRICHMENT_COMMAND.toString())) {
            String errorMsg = String.format(COMMAND_FIELD_ERROR_MSG, event.toString());
            LOG.error(errorMsg);
            return NikitaResult.fromErrorMessage(errorMsg);
        }

        event.put(EnrichmentFields.ENRICHMENT_COMMAND.toString(), command.get());
        return result;
    }

    @Override
    public boolean canModifyEvent() {
        return true;
    }

    public static abstract class Builder<T extends EnrichingRule> extends Rule.Builder<T> {
        protected String tableName;
        protected String key;
        protected ArrayList<Pair<String, String>> enrichmentTags = new ArrayList<>();
        protected ArrayList<Pair<String, String>> enrichmentFields = new ArrayList<>();
        protected static final String MISSING_REQUIRED_ARGUMENTS = "Missing required arguments in rule builder";
        protected static final String TAGS_AND_FIELDS_EMPTY = "Enrichment tags and fields are empty";

        public Builder<T> key(String key) {
            this.key = key;
            return this;
        }

        public Builder<T> tableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public Builder<T> enrichmentTags(List<Pair<String, String>> enrichingTags) {
            enrichingTags.forEach(x -> this.enrichmentTags.add(ImmutablePair.of(x.getKey(), x.getValue())));
            return this;
        }

        public Builder<T> enrichmentFields(List<Pair<String, String>> enrichingFields) {
            enrichingFields.forEach(x -> this.enrichmentFields.add(ImmutablePair.of(x.getKey(), x.getValue())));
            return this;
        }
    }

    public static Builder<EnrichingRule> enrichingRuleBuilder() {

        return new Builder<EnrichingRule>() {
            @Override
            protected EnrichingRule buildInternally() {
                if (key == null || tableName == null) {
                    throw new IllegalArgumentException(MISSING_REQUIRED_ARGUMENTS);
                }

                if (enrichmentTags.isEmpty() && enrichmentFields.isEmpty()) {
                    throw new IllegalArgumentException(TAGS_AND_FIELDS_EMPTY);
                }

                prepareBuild();
                return new EnrichingRule(this);
            }
        };
    }
}
