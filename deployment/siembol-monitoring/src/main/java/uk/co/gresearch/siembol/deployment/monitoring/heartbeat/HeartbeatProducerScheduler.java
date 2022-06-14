package uk.co.gresearch.siembol.deployment.monitoring.heartbeat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import uk.co.gresearch.siembol.common.metrics.SiembolMetricsRegistrar;

import java.io.Closeable;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HeartbeatProducerScheduler implements Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final Map<String, HeartbeatProducer> producerMap =  new HashMap<>();
    private final int errorThreshold;

    public HeartbeatProducerScheduler(Map<String, HeartbeatProducerProperties> producerPropertiesMap,
                                      Map<String, Object> heartbeatMessageProperties,
                                      int heartbeatIntervalSeconds,
                                      SiembolMetricsRegistrar metricsRegistrar) {
        this(producerPropertiesMap,
                heartbeatMessageProperties,
                heartbeatIntervalSeconds,
                metricsRegistrar,
                Executors.newSingleThreadScheduledExecutor(),
                new HeartbeatProducerFactory());
    }

    HeartbeatProducerScheduler(Map<String, HeartbeatProducerProperties> producerPropertiesMap,
                               Map<String, Object> heartbeatMessageProperties,
                               int heartbeatIntervalSeconds,
                               SiembolMetricsRegistrar metricsRegistrar,
                               ScheduledExecutorService executorService,
                               HeartbeatProducerFactory factory) {
        this.errorThreshold = producerPropertiesMap.size();
        for (Map.Entry<String, HeartbeatProducerProperties> producerProperties : producerPropertiesMap.entrySet()) {
            var producerName = producerProperties.getKey();
            LOG.info("Initialising producer {}", producerName);
            var producer = factory.createHeartbeatProducer(producerProperties.getValue(), producerName,
                    heartbeatMessageProperties, metricsRegistrar);
            producerMap.put(producerName, producer);
            executorService.scheduleAtFixedRate(
                    producer::sendHeartbeat,
                heartbeatIntervalSeconds,
                heartbeatIntervalSeconds,
                TimeUnit.SECONDS);
            LOG.info("Finished initialising heartbeat producer {}", producerName);
        }
    }

    public Health checkHealth() {
        var countErrors = 0;
        for (var producer: this.producerMap.values()) {
            if (producer.checkHealth().getStatus() == Status.DOWN) {
                countErrors ++;
            }
        }
        return countErrors >= errorThreshold? Health.down().build(): Health.up().build();
    }

    public void close() {
        for (var producer: this.producerMap.values()) {
            producer.close();
        }
    }
}
