package uk.co.gresearch.siembol.configeditor.service.alerts.spark;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import uk.co.gresearch.siembol.common.utils.HttpProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

public class AlertingSparkTestingProviderTest {
    private HttpProvider httpProvider;
    private Map<String, Object> sparkAttributes;
    private AlertingSparkTestingProvider alertingSparkTestingProvider;

    private String arg = "arg1";
    private String result = "result";
    private ArgumentCaptor<String> argumentBodyCaptor;

    @Before
    public void setUp() throws Exception {
        httpProvider = Mockito.mock(HttpProvider.class);
        sparkAttributes = new HashMap<>();
        sparkAttributes.put("dummy_int", 1);
        sparkAttributes.put("dummy_str", "dummy");

        alertingSparkTestingProvider = new AlertingSparkTestingProvider(httpProvider, sparkAttributes);
        argumentBodyCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.when(httpProvider.post(eq("batches"), argumentBodyCaptor.capture())).thenReturn(result);
    }

    @Test
    public void submitJobOk() throws Exception {
        var currentResult = alertingSparkTestingProvider.submitJob(List.of(arg));
        String body = argumentBodyCaptor.getValue();
        Assert.assertNotNull(body);
        var reader = new ObjectMapper().readerFor(new TypeReference<Map<String, Object>>() {
                });
        Map<String, Object> bodyMap = reader.readValue(body);
        Assert.assertEquals(1, bodyMap.get("dummy_int"));
        Assert.assertEquals("dummy", bodyMap.get("dummy_str"));
        Assert.assertEquals("siembol_alerting_tester", bodyMap.get("name"));
        Assert.assertEquals("uk.co.gresearch.siembol.spark.AlertingSpark", bodyMap.get("className"));
        Assert.assertTrue(bodyMap.get("args") instanceof List);
        var args = (List)bodyMap.get("args");
        Assert.assertEquals(1, args.size());
        Assert.assertEquals("arg1", args.get(0));
    }

    @Test(expected = IllegalStateException.class)
    public void submitJobException() throws Exception {
        Mockito.when(httpProvider.post(eq("batches"), anyString())).thenThrow(new IllegalStateException());
        alertingSparkTestingProvider.submitJob(List.of(arg));
    }
}
