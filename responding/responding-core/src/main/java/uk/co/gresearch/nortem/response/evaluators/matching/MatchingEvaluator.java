package uk.co.gresearch.nortem.response.evaluators.matching;

import uk.co.gresearch.nortem.nikita.common.EvaluationResult;
import uk.co.gresearch.nortem.nikita.engine.IsInSetMatcher;
import uk.co.gresearch.nortem.nikita.engine.RegexMatcher;
import uk.co.gresearch.nortem.nikita.engine.RuleMatcher;
import uk.co.gresearch.nortem.response.common.Evaluable;
import uk.co.gresearch.nortem.response.common.ResponseEvaluationResult;
import uk.co.gresearch.nortem.response.common.RespondingResult;
import uk.co.gresearch.nortem.response.common.ResponseAlert;
import uk.co.gresearch.nortem.response.model.MatcherDto;
import uk.co.gresearch.nortem.response.model.MatchingEvaluatorAttributesDto;

import java.util.List;
import java.util.stream.Collectors;

public class MatchingEvaluator implements Evaluable {
    private final List<RuleMatcher> matchers;
    private final ResponseEvaluationResult resultAfterMatch;

    public MatchingEvaluator(MatchingEvaluatorAttributesDto attributesDto) {
        matchers = attributesDto.getMatchers().stream()
                .map(this::createNikitaMatcher).collect(Collectors.toList());
        resultAfterMatch = attributesDto.getEvaluationResult().getResponseEvaluationResult();
    }

    @Override
    public RespondingResult evaluate(ResponseAlert alert) {
        ResponseAlert current = (ResponseAlert)alert.clone();
        for (RuleMatcher matcher : matchers) {
            EvaluationResult result = matcher.match(current);
            if (result == EvaluationResult.NO_MATCH) {
                return RespondingResult.fromEvaluationResult(ResponseEvaluationResult.NO_MATCH, alert);
            }
        }
        return RespondingResult.fromEvaluationResult(resultAfterMatch, current);
    }

    private RuleMatcher createNikitaMatcher(MatcherDto matcherDto) {
        switch (matcherDto.getType()) {
            case REGEX_MATCH:
                return RegexMatcher
                        .builder()
                        .pattern(matcherDto.getData())
                        .fieldName(matcherDto.getField())
                        .isNegated(matcherDto.getNegated())
                        .build();
            case IS_IN_SET:
                return IsInSetMatcher
                        .builder()
                        .data(matcherDto.getData())
                        .isCaseInsensitiveCompare(matcherDto.getCaseInsensitiveCompare())
                        .fieldName(matcherDto.getField())
                        .isNegated(matcherDto.getNegated())
                        .build();
        }

        throw new UnsupportedOperationException();
    }
}
