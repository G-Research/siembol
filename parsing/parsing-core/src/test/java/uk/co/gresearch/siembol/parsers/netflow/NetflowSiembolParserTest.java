package uk.co.gresearch.siembol.parsers.netflow;

import com.google.common.io.Files;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static uk.co.gresearch.siembol.common.constants.SiembolMessageFields.ORIGINAL;

public class NetflowSiembolParserTest {

    private SiembolNetflowParser netflowParser;

    private byte[] readFileFromResource(String filename) throws IOException {
        ClassLoader classLoader = this.getClass().getClassLoader();
        File file = new File(classLoader.getResource(filename).getFile());
        return Files.toByteArray(file);
    }

    @Before
    public void setUp() {
        netflowParser = new SiembolNetflowParser();
    }

    @Test
    public void testTemplateAndDataParsing() throws IOException {
        byte[] data = readFileFromResource("example2.netflow9");
        List<Map<String, Object>> ret = netflowParser.parse("10.16.22.254", data);
        Assert.assertEquals(30, ret.size());

        Map<String, Object> first = ret.get(0);
        Set<String> firstKeys = first.keySet();
        Assert.assertEquals(1521721700000L, first.get("timestamp"));

        Assert.assertEquals("172.20.0.118", first.get("ip_src_addr"));
        Assert.assertEquals("172.20.44.72", first.get("ip_dst_addr"));
        Assert.assertEquals(34561L, first.get("ip_src_port") );
        Assert.assertEquals(6L, first.get("protocol"));

        for (Map<String, Object> item : ret){
            Assert.assertEquals(firstKeys, item.keySet());
        }

        //next time we will have template in the map
        List<Map<String, Object>> retNext = netflowParser.parse("10.16.22.254", data);
        Assert.assertEquals(30, retNext.size());
        Assert.assertEquals(ret, retNext);
    }

    @Test
    public void testUnknownTemplate() throws IOException {
        byte[] data = readFileFromResource("example5.netflow9");

        List<Map<String, Object>> ret = netflowParser.parse("10.16.22.254", data);
        Assert.assertEquals(1, ret.size());
        Map<String, Object> unknownTemplate = ret.get(0);

        Assert.assertEquals(unknownTemplate.get(ORIGINAL.getName()),
                Base64.getEncoder().encodeToString(data));

        Assert.assertTrue((Boolean)unknownTemplate.get(SiembolNetflowParser.NETFLOW_UNKNOWN_TEMPLATE));
        Assert.assertEquals( "0", unknownTemplate.get(SiembolNetflowParser.NETFLOW_SOURCE_ID));
        Assert.assertEquals("10.16.22.254", unknownTemplate.get(SiembolNetflowParser.NETFLOW_GLOBAL_SOURCE));
    }

    @Test(expected = IllegalStateException.class)
    public void testUsupportedVersionFail() throws IOException {
        byte[] data = readFileFromResource("unsupportedVersion.netflow9");

        List<Map<String, Object>> ret = netflowParser.parse("10.16.22.254", data);
        Assert.assertNull(ret);
    }

    @Test(expected = IllegalStateException.class)
    public void missingIPFail() throws IOException {
        byte[] data = readFileFromResource("example3.netflow9");

        List<Map<String, Object>> ret = netflowParser.parse("", data);
        Assert.assertNull(ret);
    }
}
