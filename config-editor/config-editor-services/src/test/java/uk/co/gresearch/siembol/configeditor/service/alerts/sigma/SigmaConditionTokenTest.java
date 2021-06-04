package uk.co.gresearch.siembol.configeditor.service.alerts.sigma;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.service.alerts.sigma.SigmaConditionToken;

import java.util.List;

public class SigmaConditionTokenTest {
    /**
     *   not     1 of filter*
     **/
    @Multiline
    private static String condition1;

    /**
     * 1 of A | 1 of B
     **/
    @Multiline
    private static String conditionUnsupported;

    /**
     * 1 of A $ 1 of B
     **/
    @Multiline
    private static String conditionUnknownToken;

    @Test
    public void getTokensOK() {
        List<Pair<SigmaConditionToken, String>> tokens = SigmaConditionToken.tokenize(condition1);
        Assert.assertEquals(3, tokens.size());
        Assert.assertEquals(SigmaConditionToken.TOKEN_NOT, tokens.get(0).getLeft());
        Assert.assertEquals("not", tokens.get(0).getRight());

        Assert.assertEquals(SigmaConditionToken.TOKEN_ONE, tokens.get(1).getLeft());
        Assert.assertEquals("1 of", tokens.get(1).getRight());

        Assert.assertEquals(SigmaConditionToken.TOKEN_ID, tokens.get(2).getLeft());
        Assert.assertEquals("filter*", tokens.get(2).getRight());
    }

    @Test(expected = IllegalArgumentException.class)
    public void tokenizeUnsupported() {
        SigmaConditionToken.tokenize(conditionUnsupported);
    }

    @Test(expected = IllegalArgumentException.class)
    public void tokenizeUnknown() {
        SigmaConditionToken.tokenize(conditionUnknownToken);
    }

}
