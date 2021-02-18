package uk.co.gresearch.siembol.configeditor.sync.actions;

import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorServiceContext;

public interface SynchronisationAction {
    ConfigEditorResult execute(ConfigEditorServiceContext context);
}
