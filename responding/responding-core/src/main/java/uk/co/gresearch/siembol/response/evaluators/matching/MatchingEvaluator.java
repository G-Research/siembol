package uk.co.gresearch.siembol.response.evaluators.matching;

import uk.co.gresearch.siembol.alerts.common.EvaluationResult;
import uk.co.gresearch.siembol.alerts.engine.IsInSetMatcher;
import uk.co.gresearch.siembol.alerts.engine.RegexMatcher;
import uk.co.gresearch.siembol.alerts.engine.RuleMatcher;
import uk.co.gresearch.siembol.response.common.Evaluable;
import uk.co.gresearch.siembol.response.common.ResponseEvaluationResult;
import uk.co.gresearch.siembol.response.common.RespondingResult;
import uk.co.gresearch.siembol.response.common.ResponseAlert;
import uk.co.gresearch.siembol.response.model.MatcherDto;
import uk.co.gresearch.siembol.response.model.MatchingEvaluatorAttributesDto;

import java.util.List;
import java.util.stream.Collectors;

public class MatchingEvaluator implements Evaluable {
    private final List<RuleMatcher> matchers;
    private final ResponseEvaluationResult resultAfterMatch;

    public MatchingEvaluator(MatchingEvaluatorAttributesDto attributesDto) {
        matchers = attributesDto.getMatchers().stream()
                .map(this::createMatcher).collect(Collectors.toList());
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

    private RuleMatcher createMatcher(MatcherDto matcherDto) {
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
