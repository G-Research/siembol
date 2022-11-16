package uk.co.gresearch.siembol.parsers.transformations;

import java.util.EnumSet;
import java.util.Map;
import java.util.regex.Pattern;
/**
 * An object for representing a matcher for message filtering
 *
 * <p>This class represents a regular expression matcher for message filtering.
 *
 * @author Marian Novotny
 *
 */
public class MessageFilterMatcher {
    private static final String MISSING_ARGUMENT = "FiledName and pattern should not be null";
    public enum Flags {
        NEGATED
    };

    private final String fieldName;
    private final Pattern pattern;
    private final EnumSet<Flags> flags;


    public MessageFilterMatcher(String fieldName, Pattern pattern, EnumSet<Flags> flags) {
        if (fieldName == null || pattern == null) {
            throw new IllegalArgumentException(MISSING_ARGUMENT);
        }

        this.fieldName = fieldName;
        this.pattern = pattern;
        this.flags = flags;
    }

    public boolean match(Map<String, Object> map) {
        Object value = map.get(fieldName);
        boolean matched = (value != null) ? pattern.matcher(value.toString()).matches() : false;
        return flags.contains(Flags.NEGATED) ? !matched : matched;
    }
}
