package uk.co.gresearch.siembol.parsers.transformations;

import uk.co.gresearch.siembol.common.utils.PatternFilter;
import uk.co.gresearch.siembol.parsers.model.TransformationAttributesDto;
import uk.co.gresearch.siembol.parsers.model.TransformationDto;
import uk.co.gresearch.siembol.parsers.model.TransformationTypeDto;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static uk.co.gresearch.siembol.parsers.model.CaseTypeDto.LOWERCASE;
import static uk.co.gresearch.siembol.parsers.model.TransformationTypeDto.*;
/**
 * An object for creating transformations
 *
 * <p>This class is for creating a Transformation instances using functional programming style.
 *
 * @author  Marian Novotny
 * @see Transformation
 *
 */
public class TransformationFactory {
    private static final String MISSING_TRANSFORMATION_TYPE = "Missing transformation type";
    private static final String UNKNOWN_TRANSFORMATION_TYPE = "Unknown transformation type";
    private static final String MISSING_TRANSFORMATION_ATTRIBUTES = "Missing transformation attributes";

    /**
     * Creates a transformation instance from a specification
     *
     * @param specification a data transform object that specify the transformation
     * @return Transformation instance using a lambda function
     * @throws IllegalArgumentException when the specification is not valid and
     *         is not possible to create a transformation.
     * @see Transformation
     * @see TransformationDto
     */
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
            case FIELD_NAME_CHANGE_CASE:
                return createCaseFieldTransformation(specification.getAttributes());
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
            case LOWERCASE_VALUE:
                return createValueTransformation(specification.getAttributes(), TransformationsLibrary::toLowerCase);
            case UPPERCASE_VALUE:
                return createValueTransformation(specification.getAttributes(), TransformationsLibrary::toUpperCase);
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

    private Transformation createCaseFieldTransformation(TransformationAttributesDto attributes) {
        if (attributes == null || attributes.getCaseType() == null) {
            throw new IllegalArgumentException(MISSING_TRANSFORMATION_ATTRIBUTES);
        }

        final Function<String, String> fun = attributes.getCaseType() == LOWERCASE
                ? x -> x.toLowerCase()
                : x -> x.toUpperCase();

        return x -> TransformationsLibrary.fieldTransformation(x, fun);
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
