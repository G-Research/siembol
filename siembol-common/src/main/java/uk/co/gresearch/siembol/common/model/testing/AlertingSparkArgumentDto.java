package uk.co.gresearch.siembol.common.model.testing;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AlertingSparkArgumentDto {
    @JsonProperty("max_result_size")
    private Integer maxResultSize = 100;
    private String rules;
    @JsonProperty("files_paths")
    private List<String> filesPaths;

    public Integer getMaxResultSize() {
        return maxResultSize;
    }

    public void setMaxResultSize(Integer maxResultSize) {
        this.maxResultSize = maxResultSize;
    }

    public String getRules() {
        return rules;
    }

    public void setRules(String rules) {
        this.rules = rules;
    }

    public List<String> getFilesPaths() {
        return filesPaths;
    }

    public void setFilesPaths(List<String> filePaths) {
        this.filesPaths = filePaths;
    }
}
