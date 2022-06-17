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
import java.util.function.BiFunction;

public class HeartbeatProducerScheduler implements Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final Map<String, HeartbeatProducer> producerMap =  new HashMap<>();
    private final int errorThreshold;

    public HeartbeatProducerScheduler(HeartbeatProperties properties,
                                      SiembolMetricsRegistrar metricsRegistrar) {
        this(properties, Executors.newSingleThreadScheduledExecutor(),
                (HeartbeatProducerProperties x, String y) ->
                      new HeartbeatProducer(x, y, properties.getMessage(), metricsRegistrar));
    }

    HeartbeatProducerScheduler(HeartbeatProperties properties,
                               ScheduledExecutorService executorService,
                               BiFunction<HeartbeatProducerProperties, String, HeartbeatProducer> factory) {
        this.errorThreshold = properties.getHeartbeatProducers().size();
        for (Map.Entry<String, HeartbeatProducerProperties> producerProperties : properties.getHeartbeatProducers().entrySet()) {
            var producerName = producerProperties.getKey();
            var producer = createHeartbeatProducer(producerProperties.getValue(), producerName, factory);
            producerMap.put(producerName, producer);
            executorService.scheduleAtFixedRate(
                    producer::sendHeartbeat,
                properties.getHeartbeatIntervalSeconds(),
                    properties.getHeartbeatIntervalSeconds(),
                TimeUnit.SECONDS);
        }
    }

    public static HeartbeatProducer createHeartbeatProducer(HeartbeatProducerProperties properties,
                                                            String producerName,
                                                            BiFunction<HeartbeatProducerProperties, String, HeartbeatProducer> factory) {
        LOG.info("Initialising producer {}", producerName);
        var producer = factory.apply(properties, producerName);
        LOG.info("Finished initialising heartbeat producer {}", producerName);
        return producer;
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
