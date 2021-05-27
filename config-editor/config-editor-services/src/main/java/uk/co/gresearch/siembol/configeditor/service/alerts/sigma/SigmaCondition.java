package uk.co.gresearch.siembol.configeditor.service.alerts.sigma;

import uk.co.gresearch.siembol.alerts.model.MatcherDto;

import java.util.List;
import java.util.Map;

public class SigmaCondition {
    private Map<String, SigmaSearch> searches;

    private List<MatcherDto> evaluateAnd(List<MatcherDto> matchersLeft, List<MatcherDto> matchersRight) {
        return null;
    }

    private List<MatcherDto> evaluateOr(List<MatcherDto> matchersLeft, List<MatcherDto> matchersRight) {
        return null;
    }

    private List<MatcherDto> evaluateNegate(List<MatcherDto> matchers) {
        return null;
    }

    private List<MatcherDto> evaluateOneOf(List<List<MatcherDto>> matchers) {
        return null;
    }

    private List<MatcherDto> evaluateAllOf(List<List<MatcherDto>> matchers) {
        return null;
    }

}
