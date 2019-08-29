package uk.co.gresearch.nortem.nikita.correlationengine;
import java.util.PriorityQueue;

public class AlertCounter {
    private final AlertCounterMetadata counterMetadata;
    private final PriorityQueue<Long> timestampsHeap = new PriorityQueue<>();

    public AlertCounter(AlertCounterMetadata counterMetadata) {
        this.counterMetadata = counterMetadata;
    }

    public void update(long eventTime) {
        if (getSize() == counterMetadata.getThreshold()) {
            timestampsHeap.poll();
        }

        timestampsHeap.add(eventTime);
    }

    public void clean(long waterMark) {
        if (!timestampsHeap.isEmpty() && timestampsHeap.peek() < waterMark - counterMetadata.getExtendedWindowSize()) {
            timestampsHeap.clear();
            return;
        }

        while (!timestampsHeap.isEmpty() && timestampsHeap.peek() < waterMark) {
            timestampsHeap.poll();
        }
    }

    public boolean isEmpty() {
        return timestampsHeap.isEmpty();
    }

    public int getSize() {
        return timestampsHeap.size();
    }

    public Long getOldest() {
        return timestampsHeap.peek();
    }

    public boolean matchThreshold() {
        return timestampsHeap.size() >= counterMetadata.getThreshold();
    }

    public boolean isMandatory() {
        return counterMetadata.isMandatory();
    }
}
