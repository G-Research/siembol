package uk.co.gresearch.siembol.common.zookeper;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public class ZookeperAttributes implements Serializable {
    @JsonProperty("zk.url")
    private String zkUrl;
    @JsonProperty("zk.path")
    private String zkPath;
    @JsonProperty("zk.base.sleep.ms")
    private Integer zkBaseSleepMs;
    @JsonProperty("zk.max.retries")
    private Integer zkMaxRetries;

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
