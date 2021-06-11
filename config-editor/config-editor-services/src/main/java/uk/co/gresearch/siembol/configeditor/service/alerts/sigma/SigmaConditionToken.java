package uk.co.gresearch.siembol.configeditor.service.alerts.sigma;

import org.apache.commons.lang3.tuple.Pair;
import uk.co.gresearch.siembol.alerts.model.MatcherDto;
import uk.co.gresearch.siembol.alerts.model.MatcherTypeDto;

import java.util.*;
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
    private static final String WRONG_TOKEN_ARGUMENTS = "Wrong arguments for the token: %s";
    private static final String UNSUPPORTED_OPERATION_MSG = "Unsupported operation: %s, %s";
    private static final String EMPTY_LIST_OF_MATCHERS = "Empty list of matchers in the search";

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
        return matchersList.stream().map(SigmaConditionToken::getSingleAndMatcher).collect(Collectors.toList());
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
                String.format(UNSUPPORTED_OPERATION_MSG, node.getToken().name(), node.getName()));
    }

    private static List<List<MatcherDto>> getMatchersAllOf(SigmaConditionTokenNode node) {
        if (node.getFirstOperand() == null ||
                !Objects.equals(SigmaConditionToken.TOKEN_ID, node.getFirstOperand().getToken())
                || node.getSecondOperand() != null) {
            throw new IllegalArgumentException(String.format(WRONG_TOKEN_ARGUMENTS, TOKEN_ALL.name()));
        }

        return node.getFirstOperand().getToken().getMatchersList(node.getFirstOperand());
    }

    private static List<List<MatcherDto>> getMatchersNot(SigmaConditionTokenNode node) {
        if (node.getFirstOperand() == null || node.getSecondOperand() != null) {
            throw new IllegalArgumentException(String.format(WRONG_TOKEN_ARGUMENTS, TOKEN_NOT.name()));
        }

        MatcherDto matcherToNegate = getSingleAndMatcherFromList(
                node.getFirstOperand().getToken().getMatchersList(node.getFirstOperand()));
        matcherToNegate.setNegated(!matcherToNegate.getNegated());
        return Collections.singletonList(Collections.singletonList(matcherToNegate));
    }

    private static List<List<MatcherDto>> getMatchersOneOf(SigmaConditionTokenNode node) {
        if (node.getFirstOperand() == null ||
                !Objects.equals(SigmaConditionToken.TOKEN_ID, node.getFirstOperand().getToken())
                || node.getSecondOperand() != null) {
            throw new IllegalArgumentException(String.format(WRONG_TOKEN_ARGUMENTS, TOKEN_ONE.name()));
        }

        List<List<MatcherDto>> matchersList = node.getFirstOperand().getToken().getMatchersList(node.getFirstOperand());
        List<MatcherDto> matchers = matchersList.stream()
                .map(SigmaConditionToken::getSingleAndMatcher)
                .collect(Collectors.toList());

        if (matchers.size() == 1) {
            //NOTE: if the size of matchers is one no need to add COMPOSITE_OR matcher type
            return Collections.singletonList(matchers);
        }

        MatcherDto retMatcher = new MatcherDto();
        retMatcher.setNegated(false);
        retMatcher.setType(MatcherTypeDto.COMPOSITE_OR);
        retMatcher.setMatchers(matchers);

        return Collections.singletonList(Collections.singletonList(retMatcher));
    }

    private static List<List<MatcherDto>> getMatchersAnd(SigmaConditionTokenNode node) {
        if (node.getFirstOperand() == null || node.getSecondOperand() == null) {
            throw new IllegalArgumentException(String.format(WRONG_TOKEN_ARGUMENTS, TOKEN_AND.name()));
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
            throw new IllegalArgumentException(String.format(WRONG_TOKEN_ARGUMENTS, TOKEN_OR.name()));
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

        return Collections.singletonList(Collections.singletonList(retMatcher));
    }

    private static List<List<MatcherDto>> getMatchersSearch(SigmaConditionTokenNode node) {
        String name = node.getName();
        Predicate<Map.Entry<String, SigmaSearch>> filter = name.equals(SEARCH_THEM_KEYWORD)
                ? x -> true : name.endsWith(SEARCH_WILDCARD)
                ? x -> x.getKey().startsWith(name.substring(0, name.length() - 1)) : x -> x.getKey().equals(name);

        List<List<MatcherDto>> ret = node.getSearches().entrySet().stream()
                .filter(filter)
                .map(x -> x.getValue().getSiembolMatchers())
                .collect(Collectors.toList());
        if (ret.isEmpty()) {
            throw new IllegalArgumentException(EMPTY_LIST_OF_MATCHERS);
        }

       return ret;
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
                .map(SigmaConditionToken::getSingleAndMatcher)
                .collect(Collectors.toList());

        return getSingleAndMatcher(singleAndMatchers);
    }
}
