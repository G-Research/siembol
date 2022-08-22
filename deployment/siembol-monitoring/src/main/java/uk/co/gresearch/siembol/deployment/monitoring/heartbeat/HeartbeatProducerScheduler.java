package uk.co.gresearch.siembol.deployment.monitoring.heartbeat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import uk.co.gresearch.siembol.common.metrics.SiembolMetricsRegistrar;
import uk.co.gresearch.siembol.deployment.monitoring.model.HeartbeatProducerProperties;
import uk.co.gresearch.siembol.deployment.monitoring.model.HeartbeatProperties;

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
    private static final int AWAITING_TERMINATION_TIME_SEC = 1;
    private static final String INIT_PRODUCER_START_MSG = "Initialising producer {}";
    private static final String INIT_PRODUCER_FINISHED_MSG = "Finished initialising heartbeat producer {}";

    private final Map<String, HeartbeatProducer> producerMap;

    private final ScheduledExecutorService executorService;
    private final int errorThreshold;

    public HeartbeatProducerScheduler(HeartbeatProperties properties,
                                      SiembolMetricsRegistrar metricsRegistrar) {
        this(properties,
                Executors.newSingleThreadScheduledExecutor(),
                (x, y) -> new HeartbeatProducer(x, y, properties.getMessage(), metricsRegistrar));
    }

    HeartbeatProducerScheduler(HeartbeatProperties properties,
                               ScheduledExecutorService executorService,
                               BiFunction<HeartbeatProducerProperties, String, HeartbeatProducer> factory) {
        this.executorService = executorService;
        this.errorThreshold = properties.getHeartbeatProducers().size();
        producerMap = new HashMap<>();
        for (var producerProperties : properties.getHeartbeatProducers().entrySet()) {
            var producerName = producerProperties.getKey();
            var producer = createHeartbeatProducer(producerProperties.getValue(), producerName, factory);
            producerMap.put(producerName, producer);
            this.executorService.scheduleAtFixedRate(
                    producer::sendHeartbeat,
                    properties.getHeartbeatIntervalSeconds(),
                    properties.getHeartbeatIntervalSeconds(),
                    TimeUnit.SECONDS);
        }
    }

    private HeartbeatProducer createHeartbeatProducer(
            HeartbeatProducerProperties properties,
            String producerName,
            BiFunction<HeartbeatProducerProperties, String, HeartbeatProducer> factory) {
        LOG.info(INIT_PRODUCER_START_MSG, producerName);
        var producer = factory.apply(properties, producerName);
        LOG.info(INIT_PRODUCER_FINISHED_MSG, producerName);
        return producer;
    }

    public Health checkHealth() {
        var countErrors = 0;
        for (var producer : this.producerMap.values()) {
            if (producer.checkHealth().getStatus() == Status.DOWN) {
                countErrors++;
            }
        }
        return countErrors >= errorThreshold ? Health.down().build() : Health.up().build();
    }

    @Override
    public void close() {
        executorService.shutdownNow();
        try {
            executorService.awaitTermination(AWAITING_TERMINATION_TIME_SEC, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            //NOTE: ignored
        }

        for (var producer : this.producerMap.values()) {
            producer.close();
        }
    }
}
