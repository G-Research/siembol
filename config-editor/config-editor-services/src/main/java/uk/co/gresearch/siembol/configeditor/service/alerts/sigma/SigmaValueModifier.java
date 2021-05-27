package uk.co.gresearch.siembol.configeditor.service.alerts.sigma;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

public enum SigmaValueModifier {
    CONTAINS("contains", SigmaValueModifier::containsTransformation),
    ALL("all", SigmaValueModifier::allTransformation),
    BASE_64("base64", SigmaValueModifier::base64Transformation),
    BASE_64_OFFSET("base64offset", x -> notSupportedTransformation("base64offset")),
    ENDS_WITH("endswith", SigmaValueModifier::endsWithTransformation ),
    STARTS_WITH("startswith", SigmaValueModifier::startWithTransformation),
    UTF_16_LE("utf16le", x -> notSupportedTransformation("utf16le")),
    UTF_16_BE("utf16be", x -> notSupportedTransformation("utf16be")),
    WIDE("wide", x -> notSupportedTransformation("wide")),
    UTF_16("utf16", x -> notSupportedTransformation("utf16")),
    RE("re", x -> x);

    private static final Map<String, SigmaValueModifier> modifiersMapping = new HashMap<>();
    private static final String UNKNOWN_MODIFIER_MSG = "Unknown modifier: %s";
    static {
        for (SigmaValueModifier modifier : SigmaValueModifier.values()) {
            modifiersMapping.put(modifier.toString(), modifier);
        }
    }

    private final String name;
    private final Function<String, String> transformation;

    SigmaValueModifier(String name, Function<String, String> transformation) {
        this.name = name;
        this.transformation = transformation;
    }

    public String transform(String value) {
        return this.transformation.apply(value);
    }

    @Override
    public String toString() {
        return name;
    }

    public static SigmaValueModifier fromName(String name) {
        if (!modifiersMapping.containsKey(name)) {
            throw new IllegalArgumentException(String.format(UNKNOWN_MODIFIER_MSG, name));
        }
        return modifiersMapping.get(name);
    }

    public static String transform(String value, List<SigmaValueModifier> modifiers) {
        String current = value;
        for (SigmaValueModifier modifier: modifiers) {
            current = modifier.transform(current);
        }
        return current;
    }

    private static String notSupportedTransformation(String name) {
        throw new IllegalStateException(String.format("Not supported transformation %s:", name));
    }

    private static String escapeString(String str) {
        return Pattern.quote(str);
    }
    private static String containsTransformation(String str) {
        return String.format("(.*%s)", escapeString(str));
    }

    private static String endsWithTransformation(String str) {
        return String.format("(.*%s$)", escapeString(str));
    }

    private static String startWithTransformation(String str) {
        return String.format("(^%s.*)", escapeString(str));
    }

    private static String allTransformation(String str) {
        return String.format("(?=.*%s)", escapeString(str));
    }

    private static String base64Transformation(String str) {
        return Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
    }
}
