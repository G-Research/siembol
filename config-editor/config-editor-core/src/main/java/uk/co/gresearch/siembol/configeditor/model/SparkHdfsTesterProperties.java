package uk.co.gresearch.siembol.configeditor.model;

import java.util.HashMap;
import java.util.Map;
/**
 * An object that represents an alerting spark tester properties
 *
 * <p>This class represents an alerting spark tester properties. It is used in the spark alerting main function
 * in order specify the test requirements.
 *
 * @author  Marian Novotny
 */
public class SparkHdfsTesterProperties {
    private String url;

    private String folderPath;

    private String fileExtension;

    Map<String, Object> attributes = new HashMap<>();

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }
}