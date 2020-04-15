package uk.co.gresearch.siembol.configeditor.service.elk;

import java.io.IOException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import uk.co.gresearch.siembol.common.utils.HttpProvider;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorAttributes;
import uk.co.gresearch.siembol.configeditor.model.SensorTemplateFields;
import uk.co.gresearch.siembol.configeditor.model.TemplateField;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class ElkServiceImpl implements ElkService {
    private static final String NO_FIELDS_FOR_SENSOR = "No fields available for the sensor";
    private static final int TEMPLATE_UPDATE_PERIOD_IN_SEC = 4 * 3600;

    private static final Logger LOG = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());

    private final ElkProvider elkProvider;
    private final AtomicReference<Map<String, List<TemplateField>>> cache = new AtomicReference<>();
    private final AtomicReference<Exception> exception = new AtomicReference<>();
    private final ScheduledThreadPoolExecutor executor;

    public ElkServiceImpl(ElkProvider elkProvider,
                          Map<String, List<TemplateField>> template,
                          int updatePeriodInSec) {
        this.elkProvider = elkProvider;
        this.cache.set(template);
        executor = new ScheduledThreadPoolExecutor(1);
        executor.setRemoveOnCancelPolicy(true);
        executor.scheduleAtFixedRate(() -> updateCache(),
                updatePeriodInSec,
                updatePeriodInSec,
                TimeUnit.SECONDS);
    }

    @Override
    public ConfigEditorResult getTemplateFields(String sensor) {
        Map<String, List<TemplateField>> current = cache.get();

        if (!current.containsKey(sensor)) {
            return ConfigEditorResult.fromMessage(
                    ConfigEditorResult.StatusCode.BAD_REQUEST,
                    NO_FIELDS_FOR_SENSOR);
        }

        ConfigEditorAttributes attributes = new ConfigEditorAttributes();
        attributes.setTemplateFields(current.get(sensor));
        return new ConfigEditorResult(ConfigEditorResult.StatusCode.OK, attributes);
    }

    @Override
    public ConfigEditorResult getTemplateFields() {
        Map<String, List<TemplateField>> current = cache.get();

        List<SensorTemplateFields> sensorTemplateFields = current
                .keySet()
                .stream()
                .map(x -> new SensorTemplateFields(x, current.get(x)))
                .collect(Collectors.toList());

        ConfigEditorAttributes attributes = new ConfigEditorAttributes();
        attributes.setSensorTemplateFields(sensorTemplateFields);
        return new ConfigEditorResult(ConfigEditorResult.StatusCode.OK, attributes);
    }

    private void updateCache() {
        try {
            LOG.info("Initiating updating elk fields cache");
            Map<String, List<TemplateField>> update = elkProvider.getTemplateFields();
            cache.set(update);
            exception.set(null);
            LOG.info("Updating elk fields cache completed");
        } catch (Exception e) {
            LOG.error("Problem during obtaining template fields from elk with exception {}",
                    ExceptionUtils.getStackTrace(e));
            exception.set(e);
        }
    }

    @Override
    public ConfigEditorResult shutDown() {
        executor.shutdown();
        return new ConfigEditorResult(ConfigEditorResult.StatusCode.OK);
    }

    @Override
    public ConfigEditorResult awaitShutDown() {
        executor.shutdownNow();
        return new ConfigEditorResult(ConfigEditorResult.StatusCode.OK);
    }

    @Override
    public Health checkHealth() {
        Exception current = exception.get();
        return current == null
                ? Health.up().build()
                : Health.down().withException(current).build();
    }

    public static ElkService createElkServiceImpl(String elkUrl, String templatePath) throws IOException {
        LOG.info("Initialising Elk Service");
        HttpProvider httpProvider = new HttpProvider(elkUrl, HttpProvider::getKerberosHttpClient);

        ElkProvider elkProvider = new ElkProvider(httpProvider, templatePath);
        Map<String, List<TemplateField>> fields = elkProvider.getTemplateFields();

        ElkService ret = new ElkServiceImpl(elkProvider, fields, TEMPLATE_UPDATE_PERIOD_IN_SEC);
        LOG.info("Initialising Elk Service completed");
        return ret;
    }
}
