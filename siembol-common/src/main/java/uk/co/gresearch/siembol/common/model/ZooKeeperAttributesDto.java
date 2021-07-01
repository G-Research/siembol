package uk.co.gresearch.siembol.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

import java.io.Serializable;

@Attributes(title = "zookeeper attributes", description = "Zookeeper attributes for node cache")
public class ZooKeeperAttributesDto implements Serializable {
    private static final long serialVersionUID = 1L;
    @JsonProperty("zk.url")
    @Attributes(required = true, description = "Zookeeper servers url. Multiple servers are separated by comma")
    private String zkUrl;
    @Attributes(required = true, description = "Path to a zookeeper node")
    @JsonProperty("zk.path")
    private String zkPath;
    @Attributes(required = true, description = "Increasing sleep time in milliseconds between retries in retry policy")
    @JsonProperty("zk.base.sleep.ms")
    private Integer zkBaseSleepMs = 1000;
    @JsonProperty("zk.max.retries")
    @Attributes(required = true, description = "Maximum number of times to retry in retry policy", minimum = 1,
            maximum = 29)
    private Integer zkMaxRetries = 3;

    public String getZkUrl() {
        return zkUrl;
    }

    public void setZkUrl(String zkUrl) {
        this.zkUrl = zkUrl;
    }

    public String getZkPath() {
        return zkPath;
    }

    public void setZkPath(String zkPath) {
        this.zkPath = zkPath;
    }

    public Integer getZkBaseSleepMs() {
        return zkBaseSleepMs;
    }

    public void setZkBaseSleepMs(Integer zkBaseSleepMs) {
        this.zkBaseSleepMs = zkBaseSleepMs;
    }

    public Integer getZkMaxRetries() {
        return zkMaxRetries;
    }

    public void setZkMaxRetries(Integer zkMaxRetries) {
        this.zkMaxRetries = zkMaxRetries;
    }
}
