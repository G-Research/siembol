package uk.co.gresearch.siembol.common.jsonschema;

import java.util.List;

public class UnionJsonType {
    private static final String FORMAT_OPTION_START = "[";
    private static final String FORMAT_COMMA = ",";
    private static final String FORMAT_OPTION_END = "]";

    private final String unionTitle;
    private final List<UnionJsonTypeOption> unionOptions;

    public UnionJsonType(String unionTitle, List<UnionJsonTypeOption> unionOptions) {
        this.unionTitle = unionTitle;
        this.unionOptions = unionOptions;
    }

    public String getUnionTitle() {
        return unionTitle;
    }

    public List<UnionJsonTypeOption> getUnionOptions() {
        return unionOptions;
    }

    public String getJsonSchema(String selectorFieldName, String attributesFieldName) {
        StringBuilder sb = new StringBuilder();
        sb.append(FORMAT_OPTION_START);
        for (UnionJsonTypeOption option : unionOptions) {
            sb.append(option.getJsonSchema(selectorFieldName, attributesFieldName));
            sb.append(FORMAT_COMMA);

        }
        sb.setLength(sb.length() - 1);
        sb.append(FORMAT_OPTION_END);
        return sb.toString();
    }
}
