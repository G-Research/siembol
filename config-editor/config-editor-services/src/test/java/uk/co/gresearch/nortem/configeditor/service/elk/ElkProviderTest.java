package uk.co.gresearch.nortem.configeditor.service.elk;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.gresearch.nortem.common.utils.HttpProvider;
import uk.co.gresearch.nortem.configeditor.common.ConfigEditorUtils;
import uk.co.gresearch.nortem.configeditor.model.TemplateField;


import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class ElkProviderTest {
    private HttpProvider mockHttp;
    private String templateResponse;
    private ElkProvider provider;
    private String templatePath = "dummy_path";
    @Before
    public void setup() throws IOException {
        mockHttp = Mockito.mock(HttpProvider.class);
        templateResponse = ConfigEditorUtils.readTextFromResources("getTemplateResponse.json").get();
        Mockito.when(mockHttp.get(templatePath)).thenReturn(templateResponse);
        provider = new ElkProvider(mockHttp, templatePath);
    }

    @Test
    public void getTemplates() throws IOException {
        Map<String, List<TemplateField>> ret = provider.getTemplateFields();
        verify(mockHttp, times(1)).get(templatePath);
        Assert.assertEquals(2, ret.size());
        Assert.assertEquals(6, ret.get("secretlogs").size());
        Assert.assertEquals(4, ret.get("plaintext").size());
        Assert.assertEquals("security:category", ret.get("secretlogs").get(0).getName());
        Assert.assertEquals("security:level", ret.get("secretlogs").get(1).getName());
        Assert.assertEquals("ip_src_addr", ret.get("secretlogs").get(2).getName());
        Assert.assertEquals("ip_dst_addr", ret.get("secretlogs").get(3).getName());
        Assert.assertEquals("ip_dst_port", ret.get("secretlogs").get(4).getName());
        Assert.assertEquals("ip_src_port", ret.get("secretlogs").get(5).getName());

        Assert.assertEquals("dlp:category", ret.get("plaintext").get(0).getName());
        Assert.assertEquals("dlp:level", ret.get("plaintext").get(1).getName());
        Assert.assertEquals("ip_src_addr", ret.get("plaintext").get(2).getName());
        Assert.assertEquals("ip_dst_port", ret.get("plaintext").get(3).getName());
    }

    @Test(expected = IOException.class)
    public void getTemplatesHttpProviderException() throws IOException {
        Mockito.when(mockHttp.get(templatePath)).thenThrow(new IOException());
        provider.getTemplateFields();
    }

    @Test(expected = IOException.class)
    public void getTemplatesWrongJson() throws IOException {
        Mockito.when(mockHttp.get(templatePath)).thenReturn("INVALID");
        provider.getTemplateFields();
    }

    @Test(expected = IllegalStateException.class)
    public void getTemplatesEmptyJson() throws IOException {
        Mockito.when(mockHttp.get(templatePath)).thenReturn("{}");
        provider.getTemplateFields();
    }
}
