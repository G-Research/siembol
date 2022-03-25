package uk.co.gresearch.siembol.common.storm;

import org.apache.storm.tuple.Tuple;
import uk.co.gresearch.siembol.common.metrics.SiembolCounter;
import uk.co.gresearch.siembol.common.metrics.SiembolMetricsRegistrar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class KafkaWriterAnchor {
    private final Tuple tuple;
    private final ArrayList<SiembolCounter> siembolCounters = new ArrayList<>();
    private final AtomicInteger referenceCounter = new AtomicInteger(0);

    public KafkaWriterAnchor(Tuple tuple) {
        this.tuple = tuple;
    }

    public void acquire() {
        referenceCounter.incrementAndGet();
    }

    public void acquire(int number) {
        referenceCounter.addAndGet(number);
    }

    public boolean release() {
        return referenceCounter.decrementAndGet() <= 0;
    }

    public Tuple getTuple() {
        return tuple;
    }

    public void addSiembolCounters(List<SiembolCounter> counters) {
        siembolCounters.addAll(counters);
    }

    public void incrementSiembolCounters() {
        siembolCounters.forEach(SiembolCounter::increment);
    }
}
