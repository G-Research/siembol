package uk.co.gresearch.siembol.parsers.extractors;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static uk.co.gresearch.siembol.common.constants.SiembolMessageFields.TIMESTAMP;
/**
 * A library of helper static functions used in parsing extractors
 *
 * <p>This class provides placeholder for helper static functions used in parsing extractors
 *
 * @author  Marian Novotny
 */

public class ParserExtractorLibrary {

    public static Map<String, Object> convertUnixTimestampToMs(Map<String, Object> map, String timeField) {
        if (!map.containsKey(timeField)
                || (!(map.get(timeField) instanceof String) && !(map.get(timeField) instanceof Number))) {
            return map;
        }

        Double d = Double.parseDouble(map.get(timeField).toString()) * 1000;
        map.put(TIMESTAMP.getName(), d.longValue());

        return map;
    }


    public static Map<String, Object> formatTimestampToMs(ArrayList<ParserDateFormat> dateFormats,
                                                          Map<String, Object> map, String timeField) {
        if (!map.containsKey(timeField)
                || !(map.get(timeField) instanceof String)) {
            return map;
        }

        Optional<Long> timestamp = ParserDateFormat.parse(dateFormats, (String)map.get(timeField));
        if (!timestamp.isPresent()) {
            throw new IllegalStateException("Unknown timestamp format");
        }

        map.put(TIMESTAMP.getName(), timestamp.get());
        return map;
    }

    public static int indexOf(String str,
                              char c,
                              int from,
                              Optional<Character> quota,
                              Optional<Character> escaped) {
        int numQuotes = 0;
        int i = from;

        while (i < str.length()) {
            char current = str.charAt(i);
            if (escaped.isPresent() && escaped.get() == current) {
                i++;
            }
            else if (quota.isPresent() && quota.get() == current) {
                numQuotes++;
            } else if (c == current && (numQuotes % 2 == 0)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public static int indexOfQuotedEscaped(String str,
                                           char c,
                                           int from,
                                           Optional<Character> escaped,
                                           boolean quotesHandling) {
        if (from >= str.length()) {
            return -1;
        }

        if (quotesHandling &&
                (str.charAt(from) == '\'' || str.charAt(from) == '"')) {
            return indexOf(str, c, from, Optional.of(str.charAt(from)), escaped);
        } else {
            return escaped.isPresent()
                    ? indexOf(str, c, from, Optional.empty(), escaped)
                    : str.indexOf(c, from);
        }
    }

    public static String replace(String str, String target, String replacement ) {
        return str != null
                ? str.replace(target, replacement)
                : null;
    }

    public static String replaceAll(String str, String target, String replacement ) {
        return str != null
                ? str.replaceAll(target, replacement)
                : null;
    }

    public static Map<String, Object> convertToString(Map<String, Object> map, Set<String> exclusions) {
        for (String key : map.keySet()) {
            if (exclusions.contains(key)
                    || map.get(key) instanceof String) {
                continue;
            }

            map.put(key, map.getOrDefault(key, "").toString());
        }
        return map;
    }
}
