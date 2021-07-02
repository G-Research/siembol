package uk.co.gresearch.siembol.response.evaluators.matching;

import uk.co.gresearch.siembol.alerts.common.EvaluationResult;
import uk.co.gresearch.siembol.alerts.engine.IsInSetMatcher;
import uk.co.gresearch.siembol.alerts.engine.RegexMatcher;
import uk.co.gresearch.siembol.alerts.engine.BasicMatcher;
import uk.co.gresearch.siembol.response.common.Evaluable;
import uk.co.gresearch.siembol.response.common.RespondingResult;
import uk.co.gresearch.siembol.response.common.ResponseAlert;
import uk.co.gresearch.siembol.response.model.MatcherDto;
import uk.co.gresearch.siembol.response.model.MatchingEvaluatorAttributesDto;
import uk.co.gresearch.siembol.response.model.MatchingEvaluatorResultDto;

import java.util.List;
import java.util.stream.Collectors;

public class MatchingEvaluator implements Evaluable {
    private final List<BasicMatcher> matchers;
    private final MatchingEvaluatorResultDto matchingResult;

    public MatchingEvaluator(MatchingEvaluatorAttributesDto attributesDto) {
        matchers = attributesDto.getMatchers().stream()
                .map(this::createMatcher)
                .collect(Collectors.toList());
        matchingResult = attributesDto.getEvaluationResult();
    }

    @Override
    public RespondingResult evaluate(ResponseAlert alert) {
        ResponseAlert current = (ResponseAlert)alert.clone();
        for (BasicMatcher matcher : matchers) {
            EvaluationResult result = matcher.match(current);
            if (result == EvaluationResult.NO_MATCH) {
                return RespondingResult.fromEvaluationResult(
                        matchingResult.computeFromEvaluationResult(EvaluationResult.NO_MATCH), alert);
            }
        }
        return RespondingResult.fromEvaluationResult(
                matchingResult.computeFromEvaluationResult(EvaluationResult.MATCH), current);
    }

    private BasicMatcher createMatcher(MatcherDto matcherDto) {
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
