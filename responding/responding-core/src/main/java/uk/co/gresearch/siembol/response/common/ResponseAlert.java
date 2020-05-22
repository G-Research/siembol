package uk.co.gresearch.siembol.response.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import uk.co.gresearch.siembol.common.constants.SiembolMessageFields;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ResponseAlert extends HashMap<String, Object> {
    private static ObjectReader ALERT_READER = new ObjectMapper()
            .readerFor(new TypeReference<Map<String, Object>>() {});
    private static ObjectWriter RESPONSE_ALERT_WRITER = new ObjectMapper()
            .writerFor(ResponseAlert.class);

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

    @Override
    public String toString() {
        try {
            return RESPONSE_ALERT_WRITER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static ResponseAlert fromRandomId() {
        return new ResponseAlert(UUID.randomUUID().toString());
    }

    public static ResponseAlert fromOriginalString(String alertId, String alertStr) throws IOException {
        Map<String, Object> originalAlert = ALERT_READER.readValue(alertStr);
        ResponseAlert alert = new ResponseAlert(alertId);
        alert.putAll(originalAlert);
        alert.put(ResponseFields.ORIGINAL_STRING.toString(), alertStr);
        return alert;
    }
}
