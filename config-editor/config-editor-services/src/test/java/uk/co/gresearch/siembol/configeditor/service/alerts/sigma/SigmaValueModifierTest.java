package uk.co.gresearch.siembol.configeditor.service.alerts.sigma;

import com.sun.jersey.core.util.Base64;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SigmaValueModifierTest {
    private List<SigmaValueModifier> modifiers;

    @Test
    public void testContains1() {
        String value = SigmaValueModifier.CONTAINS.transform("A");
        Assert.assertEquals(".*A.*", value);
    }

    @Test
    public void testContainsFromName() {
        String value = SigmaValueModifier.fromName("contains").transform("A");
        Assert.assertEquals(".*A.*", value);
    }

    @Test
    public void testContains2() {
        String value = SigmaValueModifier.CONTAINS.transform(".*A");
        Assert.assertEquals(".*A.*", value);
    }

    @Test
    public void testContains3() {
        String value = SigmaValueModifier.CONTAINS.transform("A.*");
        Assert.assertEquals(".*A.*", value);
    }

    @Test
    public void testContains4() {
        String value = SigmaValueModifier.CONTAINS.transform(".*A.*");
        Assert.assertEquals(".*A.*", value);
    }

    @Test
    public void testAll() {
        String value = SigmaValueModifier.ALL.transform("A");
        Assert.assertEquals("(?=A)", value);
    }

    @Test
    public void testBase64() {
        String value = SigmaValueModifier.BASE_64.transform("A");
        Assert.assertEquals("A", Base64.base64Decode(value));
    }

    @Test(expected = IllegalStateException.class)
    public void testBase64OffsetUnsupported() {
        String value = SigmaValueModifier.BASE_64_OFFSET.transform("A");
        Assert.assertNotNull(value);
    }

    @Test
    public void testStartsWith1() {
        String value = SigmaValueModifier.STARTS_WITH.transform("A");
        Assert.assertEquals("^A.*", value);
    }

    @Test
    public void testStartsWith2() {
        String value = SigmaValueModifier.STARTS_WITH.transform("^A");
        Assert.assertEquals("^A.*", value);
    }

    @Test
    public void testStartsWith3() {
        String value = SigmaValueModifier.STARTS_WITH.transform("A.*");
        Assert.assertEquals("^A.*", value);
    }

    @Test
    public void testEndsWith1() {
        String value = SigmaValueModifier.ENDS_WITH.transform("A");
        Assert.assertEquals(".*A$", value);
    }

    @Test
    public void testEndsWith2() {
        String value = SigmaValueModifier.ENDS_WITH.transform("A$");
        Assert.assertEquals(".*A$", value);
    }

    @Test
    public void testEndsWith3() {
        String value = SigmaValueModifier.ENDS_WITH.transform(".*A");
        Assert.assertEquals(".*A$", value);
    }

    @Test(expected = IllegalStateException.class)
    public void testUtf16LeUnsupported() {
        String value = SigmaValueModifier.UTF_16_LE.transform("A");
        Assert.assertNotNull(value);
    }

    @Test(expected = IllegalStateException.class)
    public void testUtf16BeUnsupported() {
        String value = SigmaValueModifier.UTF_16_BE.transform("A");
        Assert.assertNotNull(value);
    }

    @Test(expected = IllegalStateException.class)
    public void testWideUnsupported() {
        String value = SigmaValueModifier.WIDE.transform("A");
        Assert.assertNotNull(value);
    }

    @Test
    public void testRe() {
        String value = SigmaValueModifier.RE.transform("Siembol");
        Assert.assertEquals("Siembol", value);
    }

    @Test
    public void testEmptyString() {
        String value = SigmaValueModifier.transform("", new ArrayList<>());
        Assert.assertEquals("^$", value);
    }

    @Test
    public void testTransformWithRe() {
        modifiers = Arrays.asList(SigmaValueModifier.RE);
        String value = SigmaValueModifier.transform(".*\\*", modifiers);
        Assert.assertEquals(".*\\*", value);
    }

    @Test
    public void testTransformEscape() {
        modifiers = Arrays.asList(SigmaValueModifier.CONTAINS);
        String value = SigmaValueModifier.transform(".*\\*", modifiers);
        Assert.assertEquals(".*\\Q.*\\*\\E.*", value);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromUnknownName() {
        SigmaValueModifier.fromName("unknown").transform("A");
    }
}
