package uk.co.gresearch.siembol.enrichments.storm.common;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class TablesUpdate {
    @JsonProperty("hdfs_tables")
    private List<HdfsTable> hdfsTables;

    public List<HdfsTable> getHdfsTables() {
        return hdfsTables;
    }

    public void setHdfsTables(List<HdfsTable> hdfsTables) {
        this.hdfsTables = hdfsTables;
    }
}
