package uk.co.gresearch.siembol.alerts.correlationengine;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AlertContext implements Comparable<AlertContext> {
    private final long timestamp;
    private final Object[] fieldsToSend;

    public AlertContext(long timestamp, Object[] fieldsToSend) {
        this.timestamp = timestamp;
        this.fieldsToSend = fieldsToSend;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getFields(List<String> fieldNames) {
        Map<String, Object> ret = new LinkedHashMap<>();
        for (int i = 0; i < fieldsToSend.length; i++) {
            if (fieldsToSend[i] == null) {
                continue;
            }
            ret.put(fieldNames.get(i), fieldsToSend[i]);
        }
        return ret;
    }

    @Override
    public int compareTo(AlertContext o) {
        return Long.compare(this.timestamp, o.timestamp);
    }
}
