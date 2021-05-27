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

    @Test
    public void getTokensOK() {
        List<Pair<SigmaConditionToken, String>> tokens = SigmaConditionToken.tokenize(condition1);
    }

}
