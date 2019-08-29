package uk.co.gresearch.nortem.nikita.engine;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NikitaEngineLibrary {
    private static final String VARIABLE_START = "${";
    private static final int VARIABLE_START_LEN = VARIABLE_START.length();
    private static final char VARIABLE_END = '}';
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{[\\w\\_\\:\\.\\-]+\\}");
    private static final Pattern VARIABLE_START_PATTERN = Pattern.compile("\\$\\{");

    public static Optional<String> substitute(Map<String, Object> event, String str) {
        int variableIndex = str.indexOf(VARIABLE_START);
        if (variableIndex < 0) {
            return Optional.of(str);
        }

        StringBuilder sb = new StringBuilder();
        int startIndex = 0;

        while (variableIndex > -1) {
            sb.append(str, startIndex, variableIndex);
            int endVariable = str.indexOf(VARIABLE_END, variableIndex);
            if (endVariable == -1) {
                return Optional.empty();
            }

            String fieldName = str.substring(variableIndex + VARIABLE_START_LEN, endVariable);
            if (event.get(fieldName) == null) {
                return Optional.empty();
            } else {
                sb.append(event.get(fieldName).toString());
            }

            startIndex = endVariable + 1;
            variableIndex = str.indexOf(VARIABLE_START, startIndex);
        }

        if (startIndex < str.length()) {
            sb.append(str, startIndex, str.length());
        }

        return Optional.of(sb.toString());
    }

    public static boolean containsVariables(String str) {
        Matcher variableMatcher = VARIABLE_PATTERN.matcher(str);
        int numVariableMatches = 0;
        while (variableMatcher.find()) {
            numVariableMatches++;
        }

        Matcher variableStartMatcher = VARIABLE_START_PATTERN.matcher(str);
        int numVariableStartMatches = 0;
        while (variableStartMatcher.find()) {
            numVariableStartMatches++;
        }
         return numVariableMatches == numVariableStartMatches && numVariableMatches > 0;
    }

}
