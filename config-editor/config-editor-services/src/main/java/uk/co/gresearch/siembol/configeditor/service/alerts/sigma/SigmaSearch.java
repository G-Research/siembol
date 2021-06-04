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

        private final SearchType searchType;
        private final String identifier;
        private final String wrongAttributesMessage;
        private List<MatcherDto> siembolMatchers = new ArrayList<>();
        private List<Pair<String, List<String>>> fieldValues = new ArrayList<>();
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

        public Builder addList(JsonNode values) {
            return addList(getStringList(values));
        }

        public Builder addList(List<String> values) {
            if (searchType != SearchType.LIST
                    || !fieldValues.isEmpty()
                    || values == null
                    || values.isEmpty()) {
                throw new IllegalArgumentException(wrongAttributesMessage);
            }

            fieldValues.add(ImmutablePair.of(SiembolMessageFields.ORIGINAL.toString(), values));
            return this;
        }

        public Builder addMapEntry(String field, JsonNode node) {
            return node.isArray()
                    ? addMapEntry(field, getStringList(node))
                    : addMapEntry(field, getTextValue(node));
        }

        public Builder addMapEntry(String field, List<String> values) {
            if (searchType != SearchType.MAP
                    || field == null
                    || values == null
                    || values.isEmpty()) {
                throw new IllegalArgumentException(wrongAttributesMessage);
            }

            fieldValues.add(ImmutablePair.of(field, values));
            return this;
        }

        public Builder addMapEntry(String field, String value) {
            return addMapEntry(field, Arrays.asList(value));
        }

        private String getTextValue(JsonNode node) {
            if (!node.isNumber() && !node.isTextual()) {
                throw new IllegalArgumentException();
            }

            return node.isTextual() ? node.asText() : node.toString();
        }

        private List<String> getStringList(JsonNode node) {
            if (!node.isArray()) {
                throw new IllegalArgumentException();
            }

            List<String> values = new ArrayList<>();
            node.iterator().forEachRemaining(x -> values.add(getTextValue(x)));
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
                MatcherDto current = getSiembolMatcher(fieldValue.getKey(), fieldValue.getValue());
                siembolMatchers.add(current);
            }

            return new SigmaSearch(this);
        }
    }
}
