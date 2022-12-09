package uk.co.gresearch.siembol.common.jsonschema;
/**
 * An object for a union type option
 *
 * <p>This class supports a union type option that is implemented as an object under 'OneOf' in the json schema.
 *
 * @author  Marian Novotny
 * @see UnionJsonType
 */
public class UnionJsonTypeOption {
    private final String selectorName;
    private final String attributesJsonSchema;
    private static final String FORMAT_OPTION = "{\"type\":\"object\",\"title\":\"%s\"," +
            "\"properties\":{\"%s\":{\"enum\":[\"%s\"],\"default\":\"%s\"}," +
            "\"%s\": %s},\"required\":[\"%s\",\"%s\"]}";

    public UnionJsonTypeOption(String selectorName, String attributesJsonSchema) {
        this.selectorName = selectorName;
        this.attributesJsonSchema = attributesJsonSchema;
    }

    public String getSelectorName() {
        return selectorName;
    }

    public String getAttributesJsonSchema() {
        return attributesJsonSchema;
    }

    public String getJsonSchema(String selectroFieldName, String attributesFieldName) {
        return String.format(FORMAT_OPTION, selectorName, selectroFieldName, selectorName,
                selectorName, attributesFieldName, attributesJsonSchema, selectroFieldName, attributesFieldName);
    }
}
