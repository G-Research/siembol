package uk.co.gresearch.siembol.configeditor.common;

import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;

import java.util.function.Supplier;
/**
 * An object that enhances error messages for a service
 *
 * <p>This class provides functionality for enhancing error messages for a generic service.
 *
 * @author  Marian Novotny
 */
public class ServiceWithErrorMessage<T> {
    protected final T service;

    public ServiceWithErrorMessage(T service) {
        this.service = service;
    }

    protected ConfigEditorResult executeInternally(Supplier<ConfigEditorResult> supplier,
                                                 String title,
                                                 String message,
                                                 String resolution) {
        var ret = supplier.get();
        if (ret.getStatusCode() == ConfigEditorResult.StatusCode.BAD_REQUEST) {
            var attributes = ret.getAttributes();
            attributes.setErrorTitleIfNotPresent(title);
            attributes.setMessageIfNotPresent(attributes.getException());
            attributes.setMessageIfNotPresent(message);
            attributes.setErrorResolutionIfNotPresent(resolution);
        }
        return ret;
    }
}
