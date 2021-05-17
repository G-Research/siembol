package uk.co.gresearch.siembol.configeditor.service.alerts.sigma;

import org.apache.commons.lang3.tuple.Pair;

import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum SigmaConditionToken {
    TOKEN_ONE(Pattern.compile("1 of", Pattern.CASE_INSENSITIVE), true),
    TOKEN_ALL(Pattern.compile("all of", Pattern.CASE_INSENSITIVE), true),
    SPACE(Pattern.compile("[\\s\\r\\n]+", Pattern.DOTALL), true),
    TOKEN_AGG(Pattern.compile("count|min|max|avg|sum",Pattern.CASE_INSENSITIVE), false),
    TOKEN_NEAR(Pattern.compile("near",Pattern.CASE_INSENSITIVE), false),
    TOKEN_BY(Pattern.compile("by", Pattern.CASE_INSENSITIVE), false),
    TOKEN_EQ(Pattern.compile("=="), false),
    TOKEN_LT(Pattern.compile("<"), false),
    TOKEN_LTE(Pattern.compile("<="), false),
    TOKEN_GT(Pattern.compile(">"), false),
    TOKEN_GTE(Pattern.compile(">="), false),
    TOKEN_PIPE(Pattern.compile("\\|"), false),
    TOKEN_AND(Pattern.compile("and", Pattern.CASE_INSENSITIVE), true),
    TOKEN_OR(Pattern.compile("or", Pattern.CASE_INSENSITIVE), true),
    TOKEN_NOT(Pattern.compile("not", Pattern.CASE_INSENSITIVE), true),
    TOKEN_ID(Pattern.compile("[\\w*]+"), true),
    TOKEN_LEFT_BRACKET(Pattern.compile("\\("),true),
    TOKEN_RIGHT_BRACKET(Pattern.compile("\\)"), true);

    private final Pattern pattern;
    private final boolean supported;


    SigmaConditionToken(Pattern pattern, boolean supported) {
        this.pattern = pattern;
        this.supported = supported;
    }

    public boolean isSupported() {
        return supported;
    }

    public Matcher getMatcher(CharSequence charSequence) {
        return pattern.matcher(charSequence);
    }

    public static List<Pair<SigmaConditionToken, String>> tokenize(String str) {
        List<Pair<SigmaConditionToken, String>> ret = new ArrayList<>();
        StringBuffer buffer = new StringBuffer(str);
        while (buffer.length() != 0) {
            String current = buffer.toString();
            boolean matched = false;
            for (SigmaConditionToken token: SigmaConditionToken.values()) {
                Matcher matcher = token.getMatcher(current);
                if (matcher.matches()) {
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                throw new IllegalArgumentException("unknown token");
            }
        }

    }

}
