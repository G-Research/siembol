package uk.co.gresearch.siembol.alerts.storm.model;

import java.util.ArrayList;
/**
 * A serializable object for representing list of alerting messages after triggering by an alerting engine
 *
 * <p>This class implements serializable interface and is used for representing list of alerting messages after
 * being triggered by an alerting engine.
 *
 * @author Marian Novotny
 * @see AlertMessage
 *
 */
public class AlertMessages extends ArrayList<AlertMessage> {
    private static final long serialVersionUID = 1L;
}
