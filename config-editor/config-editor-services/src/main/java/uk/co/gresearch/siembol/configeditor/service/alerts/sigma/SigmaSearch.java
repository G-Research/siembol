package uk.co.gresearch.siembol.configeditor.service.alerts.sigma;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import uk.co.gresearch.siembol.alerts.model.MatcherDto;
import uk.co.gresearch.siembol.alerts.model.MatcherTypeDto;
import uk.co.gresearch.siembol.common.constants.SiembolMessageFields;

import java.util.*;
import java.util.stream.Collectors;

import static uk.co.gresearch.siembol.configeditor.service.alerts.sigma.SigmaValueModifier.ALL;

public class SigmaSearch {
    public enum SearchType {
        LIST,
        MAP
    }

    private final String identifier;
    private final List<MatcherDto> siembolMatchers;

    SigmaSearch(Builder builder) {
        identifier = builder.identifier;
        siembolMatchers = builder.siembolMatchers;
    }

    public String getIdentifier() {
        return identifier;
    }

    public List<MatcherDto> getSiembolMatchers() {
        return siembolMatchers;
    }

    public static class Builder {
        private static final String INVALID_SEARCH_ATTRIBUTES = "Wrong search attributes in search with identifier: %s";
        private static final String VALUE_MODIFIER_SEPARATOR = "\\|";
        private static final Character PATTERN_OR = '|';
        private static final String NULL_MATCHER_NEGATED_PATTERN = ".*";

        private final SearchType searchType;
        private final String identifier;
        private final String wrongAttributesMessage;
        private final List<MatcherDto> siembolMatchers = new ArrayList<>();
        private final List<Pair<String, List<String>>> fieldValues = new ArrayList<>();
        private Map<String, String> fieldMapping = new HashMap<>();

        public Builder(SearchType searchType, String identifier) {
            this.searchType = searchType;
            this.identifier = identifier;
            this.wrongAttributesMessage = String.format(INVALID_SEARCH_ATTRIBUTES, identifier);
        }

        public Builder fieldMapping(Map<String, String> fieldMapping) {
            this.fieldMapping = fieldMapping;
            return this;
        }

        public Builder addList(JsonNode node) {
            if (!node.isArray()) {
                throw new IllegalArgumentException(wrongAttributesMessage);
            }

            return addList(getStringList(node));
        }

        public Builder addList(List<String> values) {
            if (searchType != SearchType.LIST
                    || values == null
                    || values.isEmpty()) {
                throw new IllegalArgumentException(wrongAttributesMessage);
            }

            fieldValues.add(ImmutablePair.of(SiembolMessageFields.ORIGINAL.toString(), values));
            return this;
        }

        public Builder addMapEntry(String field, JsonNode node) {
            if (searchType != SearchType.MAP
                    || field == null) {
                throw new IllegalArgumentException(wrongAttributesMessage);
            }

            return node.isArray()
                    ? addMapEntry(field, getStringList(node))
                    : addMapEntry(field, getTextValue(node));
        }

        private Builder addMapEntry(String field, List<String> values) {
            if (values.isEmpty()) {
                throw new IllegalArgumentException(wrongAttributesMessage);
            }
            fieldValues.add(ImmutablePair.of(field, values));
            return this;
        }

       private Builder addMapEntry(String field, Optional<String> value) {
           List<String> values = new ArrayList<>();
           value.ifPresent(values::add);
           fieldValues.add(ImmutablePair.of(field, values));
           return this;
       }

        private Optional<String> getTextValue(JsonNode node) {
            if (node.isNull()) {
                return Optional.empty();
            }

            if (!node.isNumber() && !node.isTextual()) {
                //NOTE: we are supporting only null, string or numbers
                throw new IllegalArgumentException(wrongAttributesMessage);
            }

            return node.isTextual() ? Optional.of(node.asText()) : Optional.of(node.toString());
        }

        private List<String> getStringList(JsonNode node) {
            List<String> values = new ArrayList<>();
            node.iterator().forEachRemaining(x -> getTextValue(x).ifPresent(values::add));
            return values;
        }

        private Pair<String, List<SigmaValueModifier>> parseField(String field) {
            List<SigmaValueModifier> valueModifiers = new ArrayList<>();
            String[] tokens = field.split(VALUE_MODIFIER_SEPARATOR);
            String fieldName = tokens[0];

            for (int i = 1; i < tokens.length; i++) {
                SigmaValueModifier current = SigmaValueModifier.fromName(tokens[i]);
                valueModifiers.add(current);
            }

            return Pair.of(fieldName, valueModifiers);
        }

        private MatcherDto getNullMatcher(String field) {
            MatcherDto ret = new MatcherDto();
            ret.setField(field);
            ret.setNegated(true);
            ret.setType(MatcherTypeDto.REGEX_MATCH);
            ret.setData(NULL_MATCHER_NEGATED_PATTERN);
            return ret;
        }

        private MatcherDto getSiembolMatcher(String field, List<String> values) {
            MatcherDto ret = new MatcherDto();
            ret.setNegated(false);
            ret.setType(MatcherTypeDto.REGEX_MATCH);

            Pair<String, List<SigmaValueModifier>> parsed = parseField(field);

            ret.setField(fieldMapping.getOrDefault(parsed.getKey(), parsed.getKey()));
            List<String> modifiedValues = values.stream()
                    .map(x -> SigmaValueModifier.transform(x, parsed.getValue()))
                    .collect(Collectors.toList());

            if (modifiedValues.isEmpty()) {
                throw new IllegalArgumentException(wrongAttributesMessage);
            }
            String data = parsed.getValue().contains(ALL)
                    ? StringUtils.join(modifiedValues, null)
                    : StringUtils.join(modifiedValues, PATTERN_OR);
            ret.setData(data);

            return ret;
        }

        public SigmaSearch build() {
            if (fieldValues.isEmpty()) {
                throw new IllegalArgumentException(wrongAttributesMessage);
            }

            for (Pair<String, List<String>> fieldValue : fieldValues) {
                MatcherDto current = fieldValue.getValue().isEmpty()
                        ? getNullMatcher(fieldValue.getKey())
                        : getSiembolMatcher(fieldValue.getKey(), fieldValue.getValue());
                siembolMatchers.add(current);
            }

            return new SigmaSearch(this);
        }
    }
}
