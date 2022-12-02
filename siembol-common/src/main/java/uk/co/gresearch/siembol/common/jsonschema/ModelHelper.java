package uk.co.gresearch.siembol.common.jsonschema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.SchemaIgnore;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
/**
 * A class with static helper methods for implementing json schema enhancements
 *
 * <p>This class exposes static methods for implementing enhancements in com.github.reinert.jjschema.
 * It supports using default values from Dto classes and preserving ordering of fields from Dto classes.
 * It extends the schema by supporting a union type - 'oneOf'
 *
 * @author  Marian Novotny
 * @see com.github.reinert.jjschema
 */

public class ModelHelper {
    private enum AnnotationType {
        TITLE,
        IGNORE,
        FIELD_NAME
    }

    private static final Logger LOG = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());
    private static final ObjectReader JSON_READER = new ObjectMapper()
            .readerFor(new TypeReference<Map<String, Object>>() {});
    private static final int REQUIRED_UNION_FIELD_LENGTH = 3;
    private static final int UNION_TYPE_FIELD_INDEX = 0;
    private static final int UNION_ATTRIBUTES_FIELD_INDEX = 1;

    private static final String WRONG_UNION_DTO_CLASS = "Wrong definition of union Dto Class with title: %s";
    private static final String NO_TITLE_ERROR_MSG = "Can not find title annotation in Dto Class %s";
    private static final String NO_FIELDS_WARN_MSG = "Dto: {} without fields used in the Dto: {}";
    private static ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final String PROPERTIES_KEY = "properties";
    private static final String REQUIRED_KEY = "required";
    private static final String TITLE_KEY = "title";
    private static final String DEFAULT_KEY = "default";
    private static final String TYPE_KEY = "type";
    private static final String ITEMS_KEY = "items";
    private static final String ARRAY_TYPE = "array";
    private static final String OBJECT_TYPE = "object";
    private static final String ONE_OF_KEY = "oneOf";
    private static final String REF_KEY = "$ref";

    private final Map<String, Map<String, Object>> defaultValues;
    private final Map<String, List<String>> fieldNames;
    private final Map<String, String> unionTypeSchemas;

    private ModelHelper(Map<String, Map<String, Object>> defaultValues,
                        Map<String, List<String>> fieldNames,
                        Map<String, String> unionTypeSchemas) {
        this.defaultValues = defaultValues;
        this.fieldNames = fieldNames;
        this.unionTypeSchemas = unionTypeSchemas;
    }

    private static Map<AnnotationType, String> getAnnotationMap(Annotation[] annotations) {
        Map<AnnotationType, String> ret = new HashMap<>();

        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(Attributes.class)) {
                ret.put(AnnotationType.TITLE, ((Attributes)annotation).title());
            }
            if (annotation.annotationType().equals(SchemaIgnore.class)) {
                ret.put(AnnotationType.IGNORE, "");
            }
            if (annotation.annotationType().equals(JsonProperty.class)) {
                ret.put(AnnotationType.FIELD_NAME,((JsonProperty) annotation).value());
            }
        }
        return ret;
    }

    private static boolean isInterestingType(Class<?> type) {
        return !ClassUtils.isPrimitiveOrWrapper(type)
                && !type.equals(String.class)
                && !type.isEnum();
    }

    public static String getUnionTypeSchema(UnionJsonType unionType, Field[] fields ) {
        if (fields.length != REQUIRED_UNION_FIELD_LENGTH) {
            throw new IllegalArgumentException(String.format(WRONG_UNION_DTO_CLASS, unionType.getUnionTitle()));
        }

        Optional<String> typeFieldName = getFieldName(fields[UNION_TYPE_FIELD_INDEX]);
        Optional<String> attributesFieldName = getFieldName(fields[UNION_ATTRIBUTES_FIELD_INDEX]);
        if (!typeFieldName.isPresent() || !attributesFieldName.isPresent()) {
            throw new IllegalArgumentException(String.format(WRONG_UNION_DTO_CLASS, unionType.getUnionTitle()));
        }

        return unionType.getJsonSchema(typeFieldName.get(), attributesFieldName.get());
    }

    private static Optional<String> getFieldName(Field field) {
        Map<AnnotationType, String> fieldAnnotations = getAnnotationMap(field.getDeclaredAnnotations());
        if (fieldAnnotations.containsKey(AnnotationType.IGNORE)) {
            return Optional.empty();
        }

        String fieldName = fieldAnnotations.containsKey(AnnotationType.FIELD_NAME)
                ? fieldAnnotations.get(AnnotationType.FIELD_NAME)
                : field.getName();
        return Optional.of(fieldName);
    }

    public static ModelHelper createModelHelper(Class<?> clazz, Optional<List<UnionJsonType>> unionTypes) throws Exception {
        Map<String, Map<String, Object>> defaultValues = new HashMap<>();
        Map<String, List<String>> fieldNames = new HashMap<>();
        Map<String, String> unionTypeSchemas = new HashMap<>();
        Map<String, UnionJsonType> unionTypesMap = new HashMap<>();
        if (unionTypes.isPresent()) {
            unionTypes.get().forEach(x -> unionTypesMap.put(x.getUnionTitle(), x));
        }

        Stack<Class<?>> stack = new Stack<>();
        stack.push(clazz);
        while (!stack.empty()) {
            Class<?> current = stack.pop();

            Map<AnnotationType, String> classAnnotations = getAnnotationMap(current.getDeclaredAnnotations());
            String title = classAnnotations.get(AnnotationType.TITLE);
            Field[] fields = current.getDeclaredFields();

            if (title == null) {
                String errorMsg = String.format(NO_TITLE_ERROR_MSG, current.toString());
                LOG.error(errorMsg);
                throw new IllegalStateException(errorMsg);
            }

            if (fieldNames.containsKey(title)) {
                continue;
            }

            if (unionTypesMap.containsKey(title)) {
                String unionSchema = getUnionTypeSchema(unionTypesMap.get(title), fields);
                unionTypeSchemas.put(title, unionSchema);
                continue;
            }

            List<String> orderedNames = new ArrayList<>();
            for (Field field : fields) {
                Optional<String> fieldName = getFieldName(field);
                if (!fieldName.isPresent()) {
                    continue;
                }

                orderedNames.add(fieldName.get());
                Type genericFieldType = field.getGenericType();
                if (genericFieldType instanceof ParameterizedType) {
                    //we handle generic parameters  e.g. List<DtoClass>
                    for (Type fieldArgType : ((ParameterizedType)genericFieldType).getActualTypeArguments()) {
                        if (isInterestingType((Class<?>)fieldArgType)) {
                            stack.push((Class<?>)fieldArgType);
                        }
                    }
                } else if (isInterestingType(field.getType())) {
                    stack.push(field.getType());
                }
            }

            if (orderedNames.isEmpty()) {
                LOG.warn(NO_FIELDS_WARN_MSG, current.toString(), clazz.toString());
            }
            fieldNames.put(title, orderedNames);
            if (current.isEnum()) {
                continue;
            }

            Object defaultInstance = current.getDeclaredConstructor().newInstance();
            ObjectWriter writerNonNull = new ObjectMapper()
                    .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                    .writerFor(current);

            String nonNullJson = writerNonNull.writeValueAsString(defaultInstance);
            Map<String, Object> nonNullObject = JSON_READER.readValue(nonNullJson);
            nonNullObject.keySet().removeIf(x -> !orderedNames.contains(x));

            if (!nonNullObject.isEmpty()) {
                defaultValues.put(title, nonNullObject);
            }
        }

        return new ModelHelper(defaultValues, fieldNames, unionTypeSchemas);
    }

    private void addDefaultValue(ObjectNode objectNode, Map<String, Object> defaults) {
        ObjectNode properties = (ObjectNode)objectNode.get(PROPERTIES_KEY);
        for (String propertyName : defaults.keySet()) {
            ObjectNode property = (ObjectNode)properties.get(propertyName);

            JsonNode defaultValue = JSON_MAPPER.convertValue(defaults.get(propertyName), JsonNode.class);
            property.set(DEFAULT_KEY, defaultValue);
        }
    }

    private void reorderFields(ObjectNode objectNode, List<String> fields) {
        ObjectNode properties = (ObjectNode)objectNode.get(PROPERTIES_KEY);
        for (String field : fields) {
            JsonNode current = properties.get(field);

            if (current != null) {
                properties.remove(field);
                properties.set(field, current);
            }
        }
    }

    private boolean isRefType(ObjectNode node) {
        return node.get(REF_KEY) != null && node.get(REF_KEY).isTextual();
    }

    public ObjectNode getEnrichedSchema(JsonNode schema) throws IOException {
        ObjectNode root = schema.deepCopy();
        Stack<ObjectNode> stack = new Stack<>();
        stack.push(root);
        while (!stack.empty()) {
            ObjectNode current = stack.pop();
            if (isRefType(current)) {
                continue;
            }

            String type = current.get(TYPE_KEY).asText();
            if (ARRAY_TYPE.equals(type)) {
                ObjectNode items = (ObjectNode)current.get(ITEMS_KEY);
                stack.push(items);
            } else if (OBJECT_TYPE.equals(type)) {
                ObjectNode properties = (ObjectNode)current.get(PROPERTIES_KEY);
                String title = current.get(TITLE_KEY).asText();

                if(unionTypeSchemas.containsKey(title)) {
                    JsonNode unionSchema = JSON_MAPPER.readValue(unionTypeSchemas.get(title), JsonNode.class);
                    current.set(ONE_OF_KEY, unionSchema);
                    current.remove(PROPERTIES_KEY);
                    current.remove(REQUIRED_KEY);
                    continue;
                }

                if (defaultValues.containsKey(title)) {
                    addDefaultValue(current, defaultValues.get(title));
                }

                if (fieldNames.containsKey(title)) {
                    reorderFields(current, fieldNames.get(title));
                }

                if (properties != null) {
                    for (Iterator<Map.Entry<String, JsonNode>> it = properties.fields(); it.hasNext(); ) {
                        Map.Entry<String, JsonNode> property = it.next();
                        stack.push((ObjectNode) property.getValue());
                    }
                }
            }
        }
        return root;
    }
}
