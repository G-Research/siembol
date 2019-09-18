package uk.co.gresearch.nortem.parsers.transformations;

import uk.co.gresearch.nortem.parsers.model.TransformationAttributesDto;
import uk.co.gresearch.nortem.parsers.model.TransformationDto;
import uk.co.gresearch.nortem.parsers.model.TransformationTypeDto;

import java.util.Map;
import java.util.stream.Collectors;

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
            case RENAME_FIELDS:
                return createRenameFieldTransformation(specification.getAttributes());
            case DELETE_FIELDS:
                return createDeleteFieldsTransformation(specification.getAttributes());
            case TRIM_VALUE:
                return createTrimValueTransformation(specification.getAttributes());
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

    private Transformation createTrimValueTransformation(TransformationAttributesDto attributes) {
        if (attributes == null || attributes.getFieldsFilter() == null) {
            throw new IllegalArgumentException(MISSING_TRANSFORMATION_ATTRIBUTES);
        }
        final PatternFilter fieldFilter = PatternFilter.create(
                attributes.getFieldsFilter().getIncludingFields(),
                attributes.getFieldsFilter().getExcludingFields());
        return x -> TransformationsLibrary.valueTransformation(x, TransformationsLibrary::trim, fieldFilter);
    }

}
