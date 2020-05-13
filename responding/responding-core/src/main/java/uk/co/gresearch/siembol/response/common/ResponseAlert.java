package uk.co.gresearch.siembol.response.common;

import uk.co.gresearch.siembol.common.constants.SiembolMessageFields;

import java.util.HashMap;
import java.util.UUID;

public class ResponseAlert extends HashMap<String, Object> {
    public ResponseAlert(String alertId) {
        this.put(ResponseFields.ALERT_ID.toString(), alertId);
    }

    public ResponseAlert() {}

    public long getTimestamp() {
        Object timestampObj = get(SiembolMessageFields.TIMESTAMP.toString());
        if (timestampObj instanceof Number) {
            return ((Number)timestampObj).longValue();
        }
        return System.currentTimeMillis();
    }

    public String getResponseAlertId() {
        Object alertIdObj = get(ResponseFields.ALERT_ID.toString());
        if (alertIdObj instanceof String) {
            return alertIdObj.toString();
        }
        throw new IllegalStateException();
    }

    public static ResponseAlert fromRandomId() {
        return new ResponseAlert(UUID.randomUUID().toString());
    }
}
