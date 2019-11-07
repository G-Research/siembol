package uk.co.gresearch.nortem.parsers.transformations;

import uk.co.gresearch.nortem.parsers.model.MessageFilterDto;
import uk.co.gresearch.nortem.parsers.model.TransformationAttributesDto;
import uk.co.gresearch.nortem.parsers.model.TransformationDto;
import uk.co.gresearch.nortem.parsers.model.TransformationTypeDto;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static uk.co.gresearch.nortem.parsers.model.TransformationTypeDto.FIELD_NAME_STRING_REPLACE_ALL;

public class TransformationFactory {
    private static final String MISSING_TRANSFORMATION_TYPE = "Missing transformation type";
    private static final String UNKNOWN_TRANSFORMATION_TYPE = "Unknown transformation type";
    private static final String MISSING_TRANSFORMATION_ATTRIBUTES = "Missing transformation attributes";


    public Transformation create(TransformationDto specification) {
        if(specification.getType() == null) {
            throw new IllegalArgumentException(MISSING_TRANSFORMATION_TYPE);
        }

        switch (specification.getType()) {
            case FIELD_NAME_STRING_REPLACE:
            case FIELD_NAME_STRING_REPLACE_ALL:
                return createStringReplaceTransformation(specification.getAttributes(), specification.getType());
            case FIELD_NAME_STRING_DELETE_ALL:
                specification.getAttributes().setStringReplaceReplacement("");
                return createStringReplaceTransformation(specification.getAttributes(), FIELD_NAME_STRING_REPLACE_ALL);
            case RENAME_FIELDS:
                return createRenameFieldTransformation(specification.getAttributes());
            case DELETE_FIELDS:
                return createDeleteFieldsTransformation(specification.getAttributes());
            case FILTER_MESSAGE:
                return createFilterMessageTransformation(specification.getAttributes());
            case TRIM_VALUE:
                return createValueTransformation(specification.getAttributes(), TransformationsLibrary::trim);
            case CHOMP_VALUE:
                return createValueTransformation(specification.getAttributes(), TransformationsLibrary::chomp);
        }

        throw new IllegalArgumentException(UNKNOWN_TRANSFORMATION_TYPE);
    }

    private Transformation createStringReplaceTransformation(TransformationAttributesDto attributes,
                                                             TransformationTypeDto type) {
        if (attributes == null
                || attributes.getStringReplaceTarget() == null
                || attributes.getStringReplaceReplacement() == null) {
            throw new IllegalArgumentException(MISSING_TRANSFORMATION_ATTRIBUTES);
        }
        final String target = attributes.getStringReplaceTarget();
        final String replacement = attributes.getStringReplaceReplacement();

        if (type == TransformationTypeDto.FIELD_NAME_STRING_REPLACE) {
            return x -> TransformationsLibrary.fieldTransformation(x,
                    y -> y.replaceFirst(target, replacement));
        } else {
            return x -> TransformationsLibrary.fieldTransformation(x,
                    y -> y.replaceAll(target, replacement));
        }
    }

    private Transformation createRenameFieldTransformation(TransformationAttributesDto attributes) {
        if (attributes == null || attributes.getFieldRenameMap() == null) {
            throw new IllegalArgumentException(MISSING_TRANSFORMATION_ATTRIBUTES);
        }

        final Map<String, String> renameMap = attributes.getFieldRenameMap().stream()
                .collect(Collectors.toMap(x -> x.getFieldToRename(), x -> x.getNewName()));

        return x -> TransformationsLibrary.fieldTransformation(x, y -> renameMap.getOrDefault(y, y));
    }

    private Transformation createDeleteFieldsTransformation(TransformationAttributesDto attributes) {
        if (attributes == null || attributes.getFieldsFilter() == null) {
            throw new IllegalArgumentException(MISSING_TRANSFORMATION_ATTRIBUTES);
        }
        final PatternFilter patternFilter = PatternFilter.create(
                attributes.getFieldsFilter().getIncludingFields(),
                attributes.getFieldsFilter().getExcludingFields());
        return x -> TransformationsLibrary.removeFields(x, patternFilter);
    }

    private Transformation createValueTransformation(TransformationAttributesDto attributes,
                                                     Function<Object, Object> fun) {
        if (attributes == null || attributes.getFieldsFilter() == null) {
            throw new IllegalArgumentException(MISSING_TRANSFORMATION_ATTRIBUTES);
        }
        final PatternFilter fieldFilter = PatternFilter.create(
                attributes.getFieldsFilter().getIncludingFields(),
                attributes.getFieldsFilter().getExcludingFields());
        return x -> TransformationsLibrary.valueTransformation(x, fun, fieldFilter);
    }

    private Transformation createFilterMessageTransformation(TransformationAttributesDto attributes) {
        if (attributes == null
                || attributes.getMessageFilter() == null
                || attributes.getMessageFilter().getMatchers() == null
                || attributes.getMessageFilter().getMatchers().isEmpty()) {
            throw new IllegalArgumentException(MISSING_TRANSFORMATION_ATTRIBUTES);
        }

        final List<MessageFilterMatcher> matchers = attributes.getMessageFilter().getMatchers()
                .stream()
                .map(x -> new MessageFilterMatcher(x.getFieldName(),
                        Pattern.compile(x.getPattern()),
                        x.getNegated()
                                ? EnumSet.of(MessageFilterMatcher.Flags.NEGATED)
                                : EnumSet.noneOf(MessageFilterMatcher.Flags.class)))
                .collect(Collectors.toCollection(ArrayList::new));

        return x -> TransformationsLibrary.filterMassage(x, matchers);
    }

}
