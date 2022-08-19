package uk.co.gresearch.siembol.deployment.monitoring.heartbeat;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.boot.actuate.health.Health;
import uk.co.gresearch.siembol.deployment.monitoring.model.HeartbeatProducerProperties;
import uk.co.gresearch.siembol.deployment.monitoring.model.HeartbeatProperties;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class HeartbeatProducerSchedulerTest {
    private interface ProducerFactory extends BiFunction<HeartbeatProducerProperties, String, HeartbeatProducer> {
    }

    private ScheduledExecutorService mockScheduledService;
    private final Map<String, HeartbeatProducerProperties> producerPropertiesMap = new HashMap<>();
    private final Map<String, Object> heartbeatMessageProperties = new HashMap<>();
    private final int heartbeatIntervalSeconds = 10;
    private HeartbeatProducer heartbeatProducer;
    private final HeartbeatProperties properties = new HeartbeatProperties();
    private ProducerFactory factory;

    @Before
    public void setUp() {
        heartbeatProducer = Mockito.mock(HeartbeatProducer.class);
        factory = Mockito.mock(ProducerFactory.class);
        doNothing().when(heartbeatProducer).sendHeartbeat();
        when(factory.apply(any(HeartbeatProducerProperties.class), anyString())).thenReturn(heartbeatProducer);
        mockScheduledService = mock(ScheduledExecutorService.class);
        given(mockScheduledService.scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(),
                any(TimeUnit.class))).willReturn(null);

        var heartbeatProducerProperties1 = new HeartbeatProducerProperties();
        var heartbeatProducerProperties2 = new HeartbeatProducerProperties();
        producerPropertiesMap.put("p1", heartbeatProducerProperties1);
        producerPropertiesMap.put("p2", heartbeatProducerProperties2);
        properties.setHeartbeatProducers(producerPropertiesMap);
        properties.setMessage(heartbeatMessageProperties);
        properties.setHeartbeatIntervalSeconds(heartbeatIntervalSeconds);

    }

    @Test
    public void ok() {
        try (var ignored = new HeartbeatProducerScheduler(properties, mockScheduledService, factory)) {
            ArgumentCaptor<Runnable> argumentCaptor = ArgumentCaptor.forClass(Runnable.class);
            ArgumentCaptor<HeartbeatProducerProperties> producerPropertiesCaptor =
                    ArgumentCaptor.forClass(HeartbeatProducerProperties.class);
            ArgumentCaptor<String> producerNameCaptor = ArgumentCaptor.forClass(String.class);
            verify(mockScheduledService, times(2)).scheduleAtFixedRate(
                    argumentCaptor.capture(),
                    eq(Long.valueOf(heartbeatIntervalSeconds)),
                    eq(Long.valueOf(heartbeatIntervalSeconds)),
                    any(TimeUnit.class));
            argumentCaptor.getAllValues().get(0).run();
            argumentCaptor.getAllValues().get(1).run();
            verify(heartbeatProducer, times(2)).sendHeartbeat();
            verify(factory, times(2)).apply(
                    producerPropertiesCaptor.capture(), producerNameCaptor.capture());
            List<HeartbeatProducerProperties> producerPropertiesList = producerPropertiesCaptor.getAllValues();
            List<String> producerNameList = producerNameCaptor.getAllValues();

            assertEquals(Arrays.asList("p1", "p2"), producerNameList);
            assertEquals(new ArrayList<>(producerPropertiesMap.values()), producerPropertiesList);
        }
    }

    @Test
    public void checkHealthUp() {
        try (var heartbeatProducerScheduler = new HeartbeatProducerScheduler(properties, mockScheduledService,
                (x, y) -> heartbeatProducer)) {
            when(heartbeatProducer.checkHealth()).thenReturn(Health.up().build(), Health.down().build());
            assertEquals(heartbeatProducerScheduler.checkHealth(), Health.up().build());
            verify(heartbeatProducer, times(2)).checkHealth();
        }
    }

    @Test
    public void checkHealthDown() {
        try (var heartbeatProducerScheduler = new HeartbeatProducerScheduler(properties,
                mockScheduledService, (x, y) -> heartbeatProducer)) {
            when(heartbeatProducer.checkHealth()).thenReturn(Health.down().build());
            assertEquals(heartbeatProducerScheduler.checkHealth(), Health.down().build());
            verify(heartbeatProducer, times(2)).checkHealth();
        }
    }
}
