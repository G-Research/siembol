package uk.co.gresearch.siembol.configeditor.service.alerts.sigma;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public enum SigmaValueModifier {
    CONTAINS("contains", SigmaValueModifier::containsTransformation),//: puts * wildcards around the values, such that the value is matched anywhere in the field.
    ALL("all", SigmaValueModifier::allTransformation),//all: Normally, lists of values were linked with OR in the generated query. This modifier changes this to AND. This is useful if you want to express a command line invocation with different parameters where the order may vary and removes the need for some cumbersome workarounds.
    BASE_64("base64", SigmaValueModifier::base64Transformation),//: The value is encoded with Base64.
    BASE_64_OFFSET("base64offset", x -> notSupportedTransformation("base64offset")),//base64offset: If a value might appear somewhere in a base64-encoded value the representation might change depending on the position in the overall value. There are three variants for shifts by zero to two bytes and except the first and last byte the encoded values have a static part in the middle that can be recognized.
    ENDS_WITH("endswith", SigmaValueModifier::endsWithTransformation ),//: The value is expected at the end of the field's content (replaces e.g. '*\cmd.exe')
    STARTS_WITH("startswith", SigmaValueModifier::startWithTransformation),//: The value is expected at the beginning of the field's content. (replaces e.g. 'adm*')
    UTF_16_LE("utf16le", x -> notSupportedTransformation("utf16le")), //: transforms value to UTF16-LE encoding, e.g. cmd > 63 00 6d 00 64 00 (only used in combination with base64 modifiers)
    UTF_16_BE("utf16be", x -> notSupportedTransformation("utf16be")),//  transforms value to UTF16-BE encoding, e.g. cmd > 00 63 00 6d 00 64 (only used in combination with base64 modifiers)
    WIDE("wide", x -> notSupportedTransformation("wide")),//: alias for utf16le modifier
    UTF_16("utf16", x -> notSupportedTransformation("utf16")),// prepends a byte order mark and encodes UTF16, e.g. cmd > FF FE 63 00 6d 00 64 00 (only used in combination with base64 modifiers)
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

    private static String containsTransformation(String str) {
        return String.format(".*%s", str);
    }

    private static String endsWithTransformation(String str) {
        return String.format(".*%s$", str);
    }

    private static String startWithTransformation(String str) {
        return String.format("^%s.*", str);
    }

    private static String allTransformation(String str) {
        return String.format("(?=.*%s)", str);
    }

    private static String base64Transformation(String str) {
        return Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
    }
}
