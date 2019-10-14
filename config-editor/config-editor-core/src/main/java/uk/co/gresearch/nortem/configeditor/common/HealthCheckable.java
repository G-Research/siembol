package uk.co.gresearch.nortem.configeditor.common;

import org.springframework.boot.actuate.health.Health;

public interface HealthCheckable {
    Health checkHealth();
}
