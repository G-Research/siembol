package uk.co.gresearch.siembol.common.storm;

import org.apache.storm.tuple.Tuple;

import java.util.concurrent.atomic.AtomicInteger;

public class KafkaWriterAnchor {
    private final Tuple tuple;
    private final AtomicInteger counter = new AtomicInteger(0);

    public KafkaWriterAnchor(Tuple tuple) {
        this.tuple = tuple;
    }

    public void acquire() {
        counter.incrementAndGet();
    }

    public void acquire(int number) {
        counter.addAndGet(number);
    }

    public boolean release() {
        return counter.decrementAndGet() <= 0;
    }

    public Tuple getTuple() {
        return tuple;
    }
}
