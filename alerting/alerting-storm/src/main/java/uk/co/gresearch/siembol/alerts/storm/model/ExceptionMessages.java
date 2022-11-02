package uk.co.gresearch.siembol.alerts.storm.model;

import java.util.ArrayList;
/**
 * A serializable object for representing list of exceptions
 *
 * <p>This class implements serializable interface and is used for representing list of exception messages
 * as json string of an ErrorMessage.
 *
 * @author Marian Novotny
 * @see uk.co.gresearch.siembol.common.error.ErrorMessage
 *
 */
public class ExceptionMessages extends ArrayList<String> {
    private static final long serialVersionUID = 1L;
}
