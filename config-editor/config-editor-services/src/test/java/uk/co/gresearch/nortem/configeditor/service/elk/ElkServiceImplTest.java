package uk.co.gresearch.nortem.configeditor.service.elk;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import uk.co.gresearch.nortem.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.nortem.configeditor.model.SensorTemplateFields;
import uk.co.gresearch.nortem.configeditor.model.TemplateField;


import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class ElkServiceImplTest {
    private ElkProvider elkProvider;
    private ElkServiceImpl elkService;
    private final List<String> dummyFieldNames = Arrays.asList("a", "b", "c");
    private final List<String> updatedFieldNames = Arrays.asList("d");
    private final String sensorName = "secret";
    private Map<String, List<TemplateField>> template;
    private Map<String, List<TemplateField>> updateTemplate;

    @Before
    public void Setup() throws IOException {
        elkProvider = Mockito.mock(ElkProvider.class);
        template = new HashMap<>();
        template.put(sensorName,
                dummyFieldNames.stream().map(x -> new TemplateField(x)).collect(Collectors.toList()));

        updateTemplate = new HashMap<>();
        updateTemplate.put(sensorName,
                updatedFieldNames.stream().map(x -> new TemplateField(x)).collect(Collectors.toList()));

        Mockito.when(elkProvider.getTemplateFields()).thenReturn(updateTemplate);
    }

    @Test
    public void getTemplateFieldsOK() throws IOException {
        elkService = new ElkServiceImpl(elkProvider, template, 1000);
        ConfigEditorResult ret = elkService.getTemplateFields();
        Assert.assertEquals( ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        List<SensorTemplateFields> sensorFields = ret.getAttributes().getSensorTemplateFields();
        Assert.assertEquals(1, sensorFields.size());
        Assert.assertEquals("secret", sensorFields.get(0).getSensorName());
        Assert.assertEquals(3, sensorFields.get(0).getFields().size());
        Assert.assertEquals("a", sensorFields.get(0).getFields().get(0).getName());
        Assert.assertEquals("b", sensorFields.get(0).getFields().get(1).getName());
        Assert.assertEquals("c", sensorFields.get(0).getFields().get(2).getName());
        elkService.shutDown();
    }

    @Test
    public void getSensorTemplateFieldsNotExisted() throws IOException {
        elkService = new ElkServiceImpl(elkProvider, template, 1000);
        ConfigEditorResult ret = elkService.getTemplateFields("UNKNOWN");
        Assert.assertEquals(ConfigEditorResult.StatusCode.BAD_REQUEST, ret.getStatusCode());
        elkService.shutDown();
    }

    @Test
    public void getSensorTemplateFieldOK() throws IOException {
        elkService = new ElkServiceImpl(elkProvider, template, 1000);
        ConfigEditorResult ret = elkService.getTemplateFields(sensorName);
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        List<TemplateField> fields = ret.getAttributes().getTemplateFields();
        Assert.assertEquals(3, fields.size());
        Assert.assertEquals("a", fields.get(0).getName());
        Assert.assertEquals("b", fields.get(1).getName());
        Assert.assertEquals("c", fields.get(2).getName());
        elkService.shutDown();
    }

    @Test
    public void updateOK() throws IOException, InterruptedException {
        elkService = new ElkServiceImpl(elkProvider, template, 1);
        Thread.sleep(3000);

        ConfigEditorResult ret = elkService.getTemplateFields();
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        List<SensorTemplateFields> sensorFields = ret.getAttributes().getSensorTemplateFields();
        Assert.assertEquals(1, sensorFields.size());
        Assert.assertEquals("secret", sensorFields.get(0).getSensorName());
        Assert.assertEquals(1, sensorFields.get(0).getFields().size());
        Assert.assertEquals("d", sensorFields.get(0).getFields().get(0).getName());

        ret = elkService.getTemplateFields(sensorName);
        List<TemplateField> fields = ret.getAttributes().getTemplateFields();
        Assert.assertEquals(1, fields.size());
        Assert.assertEquals("d", fields.get(0).getName());
        elkService.shutDown();
    }

    @Test
    public void healthCheck() throws IOException, InterruptedException, ExecutionException {
        elkService = new ElkServiceImpl(elkProvider, template, 1);
        Health health = elkService.checkHealth();
        Assert.assertEquals( Status.UP, health.getStatus());

        Mockito.when(elkProvider.getTemplateFields())
                .thenThrow(new IOException())
                .thenThrow(new IOException())
                .thenReturn(updateTemplate);
        Thread.sleep(1500);
        health = elkService.checkHealth();
        Assert.assertEquals( Status.DOWN, health.getStatus());
        Thread.sleep(3000);

        health = elkService.checkHealth();
        Assert.assertEquals(Status.UP, health.getStatus());

        elkService.shutDown();
    }
}
