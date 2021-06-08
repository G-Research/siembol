package uk.co.gresearch.siembol.configeditor.service.alerts.sigma;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

public class SigmaConditionTokenNode {
    private final SigmaConditionToken token;
    private final String name;
    private final Map<String, SigmaSearch> searches;
    private SigmaConditionTokenNode firstOperand;
    private SigmaConditionTokenNode secondOperand;

    public SigmaConditionTokenNode(Pair<SigmaConditionToken, String> conditionToken,
                                   Map<String, SigmaSearch> searches) {
        this.token = conditionToken.getLeft();
        this.name = conditionToken.getRight();
        this.searches = searches;
    }

    public SigmaConditionToken getToken() {
        return token;
    }

    public String getName() {
        return name;
    }

    public SigmaConditionTokenNode getFirstOperand() {
        return firstOperand;
    }

    public void setFirstOperand(SigmaConditionTokenNode firstOperand) {
        this.firstOperand = firstOperand;
    }

    public SigmaConditionTokenNode getSecondOperand() {
        return secondOperand;
    }

    public void setSecondOperand(SigmaConditionTokenNode secondOperand) {
        this.secondOperand = secondOperand;
    }

    public Map<String, SigmaSearch> getSearches() {
        return searches;
    }
}
