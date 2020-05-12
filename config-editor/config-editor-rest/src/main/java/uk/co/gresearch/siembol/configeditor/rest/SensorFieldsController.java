package uk.co.gresearch.siembol.configeditor.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.service.elk.ElkService;

@RestController
public class SensorFieldsController {
    @Autowired
    @Qualifier("elkService")
    private final ElkService elkService;

    public SensorFieldsController(
            @Qualifier("elkService") ElkService elkService) {
        this.elkService = elkService;
    }

    @CrossOrigin
    @GetMapping(value = "/api/v1/sensorfields/{sensor}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ConfigEditorResult getFields(@PathVariable String sensor) {
        return elkService.getTemplateFields(sensor);
    }

    @CrossOrigin
    @GetMapping(value = "/api/v1/sensorfields", produces = MediaType.APPLICATION_JSON_VALUE)
    public ConfigEditorResult getFields() {
        return elkService.getTemplateFields();
    }
}
