package uk.co.gresearch.nortem.nikita.protection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.gresearch.nortem.nikita.common.NikitaResult;

public class RuleProtectionSystemTest {
    private RuleProtectionSystem protection;
    private String ruleName = "test";

    @Before
    public void setUp() {
        protection = new RuleProtectionSystemImpl();
    }

    @Test
    public void testUnknown() {
        NikitaResult ret = protection.getRuleMatches(ruleName);
        Assert.assertEquals(NikitaResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertEquals("No matches of the rule test", ret.getAttributes().getMessage());
    }

    @Test
    public void testIncrement() {
        //NOTE: this test can theoretically  fails we can turn it of in case of issues
        for (int i = 1; i < 2; i++) {
            NikitaResult ret = protection.incrementRuleMatches(ruleName);
            Assert.assertEquals(NikitaResult.StatusCode.OK, ret.getStatusCode());
            Assert.assertEquals(Integer.valueOf(i), ret.getAttributes().getDailyMatches());
            Assert.assertEquals(Integer.valueOf(i), ret.getAttributes().getHourlyMatches());
        }
    }

    @Test
    public void testIncrementAndGet() {
        NikitaResult ret = protection.incrementRuleMatches(ruleName);
        Assert.assertEquals(Integer.valueOf(1), ret.getAttributes().getDailyMatches());
        Assert.assertEquals(Integer.valueOf(1), ret.getAttributes().getHourlyMatches());

        ret = protection.getRuleMatches(ruleName);
        Assert.assertEquals(Integer.valueOf(1), ret.getAttributes().getDailyMatches());
        Assert.assertEquals(Integer.valueOf(1), ret.getAttributes().getHourlyMatches());
    }
}
