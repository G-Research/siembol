package uk.co.gresearch.siembol.configeditor.sync.actions;

import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorServiceContext;

import java.util.ArrayList;
import java.util.List;

import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.OK;

public class CompositeSyncAction implements SynchronisationAction {
    private final List<SynchronisationAction> actions;

    public CompositeSyncAction(Builder builder) {
        this.actions = builder.actions;
    }

    @Override
    public ConfigEditorResult execute(ConfigEditorServiceContext context) {
        ConfigEditorServiceContext currentContext = context;
        for (SynchronisationAction action : actions) {
            ConfigEditorResult currentResult = action.execute(currentContext);
            if (currentResult.getStatusCode() != OK) {
                return currentResult;
            }
            currentContext = currentResult.getAttributes().getServiceContext();
        }

        return ConfigEditorResult.fromServiceContext(currentContext);
    }

    public static class Builder {
        private final List<SynchronisationAction> actions = new ArrayList<>();

        public Builder addAction(SynchronisationAction action) {
            actions.add(action);
            return this;
        }

        public boolean isEmpty() {
            return actions.isEmpty();
        }

        public CompositeSyncAction build() {
            return new CompositeSyncAction(this);
        }
    }
}
