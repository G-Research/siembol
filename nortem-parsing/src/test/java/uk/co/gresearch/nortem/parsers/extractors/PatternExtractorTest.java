package uk.co.gresearch.nortem.parsers.extractors;

import org.adrianwalker.multilinestring.Multiline;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;


public class PatternExtractorTest {
    private String name = "test_name";
    private String field = "test_field";
    private List<String> stringPatterns;

    /**
     * MID (?<my_mid>\d+)
     **/
    @Multiline
    public static String goodMid;


    /**
     * Threat Level=(?<vof_threat_level>\d) Category=(?<vof_threat_cat>\S+) Type=(?<vof_threat_type>.*?)
     **/
    @Multiline
    public static String goodVofDetail;

    /**
     * Threat Level=1 Category=UNKNOWN Type=a
     *bc
     **/
    @Multiline
    public static String vofDetailInstance;

    /**
     * Threat Level=A Category=UNKNOWN Type=abc
     **/
    @Multiline
    public static String vofDetailInstanceWrong1;

    /**
     * Threat Level=1 Category= Type=abc
     **/
    @Multiline
    public static String vofDetailInstanceWrong2;

    @Before
    public void setUp() {
        stringPatterns = new ArrayList<>();
    }

    @Test
    public void testGoodMid() {

        stringPatterns.add(goodMid.trim());
        PatternExtractor extractor = PatternExtractor.builder()
                .patterns(stringPatterns)
                .name(name)
                .field(field)
                .build();

        Assert.assertEquals(name, extractor.getName());
        Assert.assertEquals(field, extractor.getField());

        Map<String, Object> ret = extractor.extract("MID 12345");
        Assert.assertEquals(1, ret.size());
        Assert.assertEquals("12345", ret.get("my_mid"));

        ret = extractor.extract("Info: MID 12345");
        Assert.assertEquals(0, ret.size());

        ret = extractor.extract("SID 12345");
        Assert.assertEquals(0, ret.size());

        ret = extractor.extract("");
        Assert.assertEquals(0, ret.size());
    }

    @Test
    public void testGoodMid2() {

        stringPatterns.add(goodMid.trim());
        stringPatterns.add(".*" + goodMid.trim());

        PatternExtractor extractor = PatternExtractor.builder()
                .patterns(stringPatterns)
                .name(name)
                .field(field)
                .build();

        Map<String, Object> ret = extractor.extract("Info: MID 12345");

        Assert.assertEquals(1, ret.size());
        Assert.assertEquals("12345", ret.get("my_mid"));

        ret = extractor.extract("MID 12345");
        Assert.assertEquals(1, ret.size());
        Assert.assertEquals("12345", ret.get("my_mid"));

        ret = extractor.extract("SID 12345");
        Assert.assertEquals(0, ret.size());

        ret = extractor.extract("");
        Assert.assertEquals(0, ret.size());
    }

    @Test
    public void testGoodVofDetail() {

        stringPatterns.add(goodVofDetail.trim());
        PatternExtractor extractor = PatternExtractor.builder()
                .patterns(stringPatterns)
                .patternExtractorFlags(EnumSet.of(PatternExtractor.PatternExtractorFlags.DOTALL))
                .name(name)
                .field(field)
                .build();

        Map<String, Object> ret = extractor.extract(vofDetailInstance.trim());
        Assert.assertEquals(3, ret.size());
        Assert.assertEquals("1", ret.get("vof_threat_level"));
        Assert.assertEquals("UNKNOWN", ret.get("vof_threat_cat"));
        Assert.assertEquals("a\nbc", ret.get("vof_threat_type"));

        ret = extractor.extract(vofDetailInstanceWrong1.trim());
        Assert.assertEquals(0, ret.size());

        ret = extractor.extract(vofDetailInstanceWrong2.trim());
        Assert.assertEquals(0, ret.size());

        ret = extractor.extract("Info: " + vofDetailInstance.trim());
        Assert.assertEquals(0, ret.size());
    }

    @Test
    public void testGoodVofDetail2() {

        stringPatterns.add(goodVofDetail.trim());
        stringPatterns.add(".*" + goodVofDetail.trim());
        PatternExtractor extractor = PatternExtractor.builder()
                .patterns(stringPatterns)
                .patternExtractorFlags(EnumSet.of(PatternExtractor.PatternExtractorFlags.DOTALL))
                .name(name)
                .field(field)
                .build();

        Map<String, Object> ret = extractor.extract("Info: " + vofDetailInstance.trim());
        Assert.assertEquals(3, ret.size());
        Assert.assertEquals("1", ret.get("vof_threat_level"));
        Assert.assertEquals("UNKNOWN", ret.get("vof_threat_cat"));
        Assert.assertEquals("a\nbc", ret.get("vof_threat_type"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongRegExp(){
        stringPatterns.add("(?<");
        PatternExtractor extractor = PatternExtractor.builder()
                .patterns(stringPatterns)
                .name(name)
                .field(field)
                .build();
        Assert.assertNull(extractor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongRegExp2(){
        stringPatterns.add("abc");
        PatternExtractor extractor = PatternExtractor.builder()
                .patterns(stringPatterns)
                .name(name)
                .field(field)
                .build();
        Assert.assertNull(extractor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongRegExp3(){
        stringPatterns.add(goodMid.trim() + goodMid.trim());
        PatternExtractor extractor = PatternExtractor.builder()
                .patterns(stringPatterns)
                .name(name)
                .field(field)
                .build();
        Assert.assertNull(extractor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongRegExp4(){
        stringPatterns.add(goodMid.trim() + "[1");
        PatternExtractor extractor = PatternExtractor.builder()
                .patterns(stringPatterns)
                .name(name)
                .field(field)
                .build();
        Assert.assertNull(extractor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongRegExp5(){
        PatternExtractor extractor = PatternExtractor.builder()
                .patterns(stringPatterns)
                .name(name)
                .field(field)
                .build();
        Assert.assertNull(extractor);
    }
}
