package uk.co.gresearch.siembol.deployment.storm.application;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import uk.co.gresearch.siembol.common.model.ZookeeperAttributesDto;
import uk.co.gresearch.siembol.deployment.storm.model.KubernetesAttributesDto;
import uk.co.gresearch.siembol.deployment.storm.model.StormClusterDto;

@ConfigurationProperties(prefix = "topology-manager")
class ServiceConfigurationProperties {
    private int scheduleAtFixedRateSeconds = 300;

    @NestedConfigurationProperty
    private ZookeeperAttributesDto desiredState;

    @NestedConfigurationProperty
    private ZookeeperAttributesDto savedState;

    @NestedConfigurationProperty
    private StormClusterDto storm;

    @NestedConfigurationProperty
    private KubernetesAttributesDto k8s;

    public ZookeeperAttributesDto getDesiredState() {
        return desiredState;
    }

    public void setDesiredState(ZookeeperAttributesDto desiredState) {
        this.desiredState = desiredState;
    }

    public ZookeeperAttributesDto getSavedState() {
        return savedState;
    }

    public void setSavedState(ZookeeperAttributesDto savedState) {
        this.savedState = savedState;
    }

    public StormClusterDto getStorm() {
        return storm;
    }

    public void setStorm(StormClusterDto storm) {
        this.storm = storm;
    }

    public KubernetesAttributesDto getK8s() {
        return k8s;
    }

    public void setK8s(KubernetesAttributesDto k8s) {
        this.k8s = k8s;
    }

    public int getScheduleAtFixedRateSeconds() {
        return scheduleAtFixedRateSeconds;
    }

    public void setScheduleAtFixedRateSeconds(int scheduleAtFixedRateSeconds) {
        this.scheduleAtFixedRateSeconds = scheduleAtFixedRateSeconds;
    }
}
