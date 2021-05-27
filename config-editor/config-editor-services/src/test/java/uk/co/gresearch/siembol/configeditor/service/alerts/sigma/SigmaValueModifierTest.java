package uk.co.gresearch.siembol.configeditor.service.alerts.sigma;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class SigmaValueModifierTest {
    /**
     *   not     1 of filter*
     **/
    @Multiline
    private static String condition1;

    @Test
    public void testContainsOK() {
        String value = SigmaValueModifier.CONTAINS.transform("A");
        Assert.assertNotNull(value);
    }

}
