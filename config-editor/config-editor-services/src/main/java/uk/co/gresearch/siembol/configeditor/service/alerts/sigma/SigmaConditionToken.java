package uk.co.gresearch.siembol.configeditor.service.alerts.sigma;

import org.apache.commons.lang3.tuple.Pair;
import uk.co.gresearch.siembol.alerts.model.MatcherDto;
import uk.co.gresearch.siembol.alerts.model.MatcherTypeDto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public enum SigmaConditionToken {
    SPACE(Pattern.compile("[\\s\\r\\n]+", Pattern.DOTALL),
            SigmaConditionTokenType.AUXILIARY,
            SigmaConditionToken::getMatchersUnsupported),
    TOKEN_ONE(Pattern.compile("1 of", Pattern.CASE_INSENSITIVE),
            SigmaConditionTokenType.UNARY_OPERATOR,
            SigmaConditionToken::getMatchersOneOf),
    TOKEN_ALL(Pattern.compile("all of", Pattern.CASE_INSENSITIVE),
            SigmaConditionTokenType.UNARY_OPERATOR,
            SigmaConditionToken::getMatchersAllOf),
    TOKEN_AGG(Pattern.compile("(count|min|max|avg|sum)",Pattern.CASE_INSENSITIVE),
            SigmaConditionTokenType.NOT_SUPPORTED,
            SigmaConditionToken::getMatchersUnsupported),
    TOKEN_NEAR(Pattern.compile("near",Pattern.CASE_INSENSITIVE),
            SigmaConditionTokenType.NOT_SUPPORTED,
            SigmaConditionToken::getMatchersUnsupported),
    TOKEN_BY(Pattern.compile("by", Pattern.CASE_INSENSITIVE),
            SigmaConditionTokenType.NOT_SUPPORTED,
            SigmaConditionToken::getMatchersUnsupported),
    TOKEN_EQ(Pattern.compile("=="),
            SigmaConditionTokenType.NOT_SUPPORTED,
            SigmaConditionToken::getMatchersUnsupported),
    TOKEN_LT(Pattern.compile("<"),
            SigmaConditionTokenType.NOT_SUPPORTED,
            SigmaConditionToken::getMatchersUnsupported),
    TOKEN_LTE(Pattern.compile("<="),
            SigmaConditionTokenType.NOT_SUPPORTED,
            SigmaConditionToken::getMatchersUnsupported),
    TOKEN_GT(Pattern.compile(">"),
            SigmaConditionTokenType.NOT_SUPPORTED,
            SigmaConditionToken::getMatchersUnsupported),
    TOKEN_GTE(Pattern.compile(">="),
            SigmaConditionTokenType.NOT_SUPPORTED,
            SigmaConditionToken::getMatchersUnsupported),
    TOKEN_PIPE(Pattern.compile("\\|"),
            SigmaConditionTokenType.NOT_SUPPORTED,
            SigmaConditionToken::getMatchersUnsupported),
    TOKEN_AND(Pattern.compile("and", Pattern.CASE_INSENSITIVE),
            SigmaConditionTokenType.BINARY_OPERATOR,
            SigmaConditionToken::getMatchersAnd),
    TOKEN_OR(Pattern.compile("or", Pattern.CASE_INSENSITIVE),
            SigmaConditionTokenType.BINARY_OPERATOR,
            SigmaConditionToken::getMatchersOr),
    TOKEN_NOT(Pattern.compile("not", Pattern.CASE_INSENSITIVE),
            SigmaConditionTokenType.UNARY_OPERATOR,
            SigmaConditionToken::getMatchersNot),
    TOKEN_ID(Pattern.compile("[\\w*]+"),
            SigmaConditionTokenType.SEARCH_ID,
            SigmaConditionToken::getMatchersSearch),
    TOKEN_LEFT_BRACKET(Pattern.compile("\\("),
            SigmaConditionTokenType.AUXILIARY,
            SigmaConditionToken::getMatchersUnsupported),
    TOKEN_RIGHT_BRACKET(Pattern.compile("\\)"),
            SigmaConditionTokenType.AUXILIARY,
            SigmaConditionToken::getMatchersUnsupported);

    private static final String SEARCH_THEM_KEYWORD = "them";
    private static final String SEARCH_WILDCARD = "*";
    private static final String UNSUPPORTED_TOKEN_MSG = "Unsupported token: %s";
    private static final String UNKNOWN_TOKEN_MSG = "Unknown token: %s";

    private final Pattern pattern;
    private final SigmaConditionTokenType type;
    private final Function<SigmaConditionTokenNode, List<List<MatcherDto>>> matchersFactory;

    SigmaConditionToken(Pattern pattern,
                        SigmaConditionTokenType type,
                        Function<SigmaConditionTokenNode, List<List<MatcherDto>>> matchersFactory) {
        this.pattern = pattern;
        this.type = type;
        this.matchersFactory = matchersFactory;
    }

    public boolean isSupported() {
        return !SigmaConditionTokenType.NOT_SUPPORTED.equals(type);
    }

    public boolean isIgnored() {
        return this.equals(SPACE);
    }

    public SigmaConditionTokenType getType() {
        return type;
    }

    public List<List<MatcherDto>> getMatchersList(SigmaConditionTokenNode node) {
        return matchersFactory.apply(node);
    }

    public List<MatcherDto> getMatchers(SigmaConditionTokenNode node) {
        List<List<MatcherDto>> matchersList = getMatchersList(node);
        return matchersList.stream().map(x -> getSingleAndMatcher(x)).collect(Collectors.toList());
    }

    private Matcher getPatternMatcher(CharSequence charSequence) {
        return pattern.matcher(charSequence);
    }

    public static List<Pair<SigmaConditionToken, String>> tokenize(String str) {
        List<Pair<SigmaConditionToken, String>> ret = new ArrayList<>();
        String current = str;
        
        while (!current.isEmpty()) {
            boolean matched = false;
            for (SigmaConditionToken token: SigmaConditionToken.values()) {
                Matcher matcher = token.getPatternMatcher(current);
                if (!matcher.find() || matcher.start() != 0) {
                   continue;
                }
                
                matched = true;
                String tokenString = current.substring(0, matcher.end());
                current = current.substring(matcher.end());

                if (token.isIgnored()) {
                    break;
                }
                if (!token.isSupported()) {
                    throw new IllegalArgumentException(String.format(UNSUPPORTED_TOKEN_MSG, tokenString));
                }
                ret.add(Pair.of(token, tokenString));
                break;
            }

            if (!matched) {
                throw new IllegalArgumentException(String.format(UNKNOWN_TOKEN_MSG, current));
            }
        }

        return ret;
    }

    private static List<List<MatcherDto>> getMatchersUnsupported(SigmaConditionTokenNode node) {
        throw new IllegalStateException(
                String.format("Unsupported operation: %s, %s", node.getToken().name(), node.getName()));
    }

    private static List<List<MatcherDto>> getMatchersAllOf(SigmaConditionTokenNode node) {
        if (!SigmaConditionToken.TOKEN_ID.equals(node.getFirstOperand().getToken())
                || node.getSecondOperand() != null) {
            throw new IllegalArgumentException("wrong all of arguments");
        }

        return node.getFirstOperand().getToken().getMatchersList(node.getFirstOperand());
    }

    private static List<List<MatcherDto>> getMatchersNot(SigmaConditionTokenNode node) {
        if (node.getFirstOperand() == null || node.getSecondOperand() != null) {
            throw new IllegalArgumentException("wrong not arguments");
        }

        MatcherDto matcherToNegate = getSingleAndMatcherFromList(
                node.getFirstOperand().getToken().getMatchersList(node.getFirstOperand()));
        matcherToNegate.setNegated(!matcherToNegate.getNegated());
        return Arrays.asList(Arrays.asList(matcherToNegate));
    }

    private static List<List<MatcherDto>> getMatchersOneOf(SigmaConditionTokenNode node) {
        if (!SigmaConditionToken.TOKEN_ID.equals(node.getFirstOperand().getToken())
                || node.getSecondOperand() != null) {
            throw new IllegalArgumentException("wrong one of arguments");
        }

        List<List<MatcherDto>> matchersList = node.getFirstOperand().getToken().getMatchersList(node.getFirstOperand());
        List<MatcherDto> matchers = matchersList.stream()
                .map(x -> getSingleAndMatcher(x))
                .collect(Collectors.toList());

        MatcherDto retMatcher = new MatcherDto();
        retMatcher.setNegated(false);
        retMatcher.setType(MatcherTypeDto.COMPOSITE_OR);
        retMatcher.setMatchers(matchers);

        return Arrays.asList(Arrays.asList(retMatcher));
    }

    private static List<List<MatcherDto>> getMatchersAnd(SigmaConditionTokenNode node) {
        if (node.getFirstOperand() == null || node.getSecondOperand() == null) {
            throw new IllegalArgumentException("wrong and arguments");
        }

        List<List<MatcherDto>> firstOperandMatchers =  node.getFirstOperand().getToken()
                .getMatchersList(node.getFirstOperand());
        List<List<MatcherDto>> secondOperandMatchers =  node.getSecondOperand().getToken()
                .getMatchersList(node.getSecondOperand());

        List<List<MatcherDto>> ret = new ArrayList<>();
        ret.addAll(firstOperandMatchers);
        ret.addAll(secondOperandMatchers);
        return ret;
    }

    private static List<List<MatcherDto>> getMatchersOr(SigmaConditionTokenNode node) {
        if (node.getFirstOperand() == null || node.getSecondOperand() == null) {
            throw new IllegalArgumentException("wrong or arguments");
        }

        MatcherDto firstOperandMatcher = getSingleAndMatcherFromList(node.getFirstOperand().getToken()
                .getMatchersList(node.getFirstOperand()));
        MatcherDto secondOperandMatcher = getSingleAndMatcherFromList(node.getSecondOperand().getToken()
                .getMatchersList(node.getSecondOperand()));
        List<MatcherDto> matchers = Arrays.asList(firstOperandMatcher, secondOperandMatcher);

        MatcherDto retMatcher = new MatcherDto();
        retMatcher.setNegated(false);
        retMatcher.setType(MatcherTypeDto.COMPOSITE_OR);
        retMatcher.setMatchers(matchers);

        return Arrays.asList(Arrays.asList(retMatcher));
    }

    private static List<List<MatcherDto>> getMatchersSearch(SigmaConditionTokenNode node) {
        String name = node.getName();
        Predicate<Map.Entry<String, SigmaSearch>> filter = name.equals(SEARCH_THEM_KEYWORD)
                ? x -> true : name.endsWith(SEARCH_WILDCARD)
                ? x -> x.getKey().startsWith(name.substring(0, name.length() - 1)) : x -> x.getKey().equals(name);

        return node.getSearches().entrySet().stream()
                .filter(filter)
                .map(x -> x.getValue().getSiembolMatchers())
                .collect(Collectors.toList());
    }

    private static MatcherDto getSingleAndMatcher(List<MatcherDto> matchers) {
        if (matchers.size() == 1) {
            return matchers.get(0);
        }

        MatcherDto ret = new MatcherDto();
        ret.setNegated(false);
        ret.setType(MatcherTypeDto.COMPOSITE_AND);
        ret.setMatchers(matchers);
        return ret;
    }

    private static MatcherDto getSingleAndMatcherFromList(List<List<MatcherDto>> matchersList) {
        List<MatcherDto> singleAndMatchers = matchersList.stream()
                .map(x -> getSingleAndMatcher(x))
                .collect(Collectors.toList());;

        return getSingleAndMatcher(singleAndMatchers);
    }
}
