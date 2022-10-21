package uk.co.gresearch.siembol.alerts.correlationengine;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
/**
 * An object that implements alerting counters using heap data structure
 *
 * <p>This object implements alerting counters using PriorityQueue of alerting contexts.
 * It evaluates a new alert and supports cleaning of old alerting contexts.
 *
 *
 * @author  Marian Novotny
 * @see AlertContext
 * @see AlertCounterMetadata
 *
 */
public class AlertCounter {
    private final AlertCounterMetadata counterMetadata;
    private final PriorityQueue<AlertContext> alertContextHeap = new PriorityQueue<>();

    public AlertCounter(AlertCounterMetadata counterMetadata) {
        this.counterMetadata = counterMetadata;
    }

    public void update(AlertContext alertContext) {
        if (getSize() == counterMetadata.getThreshold()) {
            alertContextHeap.poll();
        }

        alertContextHeap.add(alertContext);
    }

    public void clean(long waterMark) {
        if (!alertContextHeap.isEmpty()
                && alertContextHeap.peek().getTimestamp() < waterMark - counterMetadata.getExtendedWindowSize()) {
            alertContextHeap.clear();
            return;
        }

        while (!alertContextHeap.isEmpty() && alertContextHeap.peek().getTimestamp() < waterMark) {
            alertContextHeap.poll();
        }
    }

    public boolean isEmpty() {
        return alertContextHeap.isEmpty();
    }

    public int getSize() {
        return alertContextHeap.size();
    }

    public Long getOldest() {
        return alertContextHeap.isEmpty() ? null : alertContextHeap.peek().getTimestamp();
    }

    public boolean matchThreshold() {
        return alertContextHeap.size() >= counterMetadata.getThreshold();
    }

    public boolean isMandatory() {
        return counterMetadata.isMandatory();
    }

    public List<Map<String, Object>> getCorrelatedAlerts(List<String> fieldNames) {
        List<Map<String, Object>> ret = new ArrayList<>();
        alertContextHeap.forEach(x -> ret.add(x.getFields(fieldNames)));
        return ret;
    }
}
