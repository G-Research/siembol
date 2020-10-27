package uk.co.gresearch.siembol.configeditor.rest.common;

import uk.co.gresearch.siembol.configeditor.model.ConfigEditorAttributes;

import java.util.Optional;

public class ConfigEditorHelper {

    public static Optional<String> getFileContent(ConfigEditorAttributes attributes) {
        return attributes == null || attributes.getFiles() == null || attributes.getFiles().isEmpty()
                ? Optional.empty()
                : Optional.ofNullable(attributes.getFiles().get(0).getContentValue());
    }
}

