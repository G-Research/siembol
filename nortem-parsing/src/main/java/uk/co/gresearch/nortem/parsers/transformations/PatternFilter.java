package uk.co.gresearch.nortem.parsers.transformations;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PatternFilter implements FieldFilter{
    private final List<Pattern> includingPatterns;
    private final List<Pattern> excludingPatterns;

    PatternFilter(List<Pattern> includingPattern, List<Pattern> excludingPattern) {
        this.includingPatterns = includingPattern;
        this.excludingPatterns = excludingPattern;
    }

    private boolean matchPatternList(List<Pattern> patterns, String str) {
        for (Pattern pattern : patterns) {
            if (pattern.matcher(str).matches()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean match(String str) {
        return matchPatternList(includingPatterns, str) && !matchPatternList(excludingPatterns, str);
    }

    public static PatternFilter create(List<String> includingList, List<String> excludingList) {
        if (includingList == null || includingList.isEmpty()) {
            throw new IllegalArgumentException("Empty including pattern list in field filter");
        }

        List<Pattern> includingPatterns =  includingList.stream()
                .map(x -> Pattern.compile(x))
                .collect(Collectors.toList());

        List<Pattern> excludingPatterns =  excludingList == null ? new ArrayList<>() :
                excludingList.stream()
                .map(x -> Pattern.compile(x))
                .collect(Collectors.toList());


        return new PatternFilter(includingPatterns, excludingPatterns);
    }
}
