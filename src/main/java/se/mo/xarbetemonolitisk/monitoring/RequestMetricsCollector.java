package se.mo.xarbetemonolitisk.monitoring;

import java.util.concurrent.atomic.LongAdder;
import org.springframework.stereotype.Component;

@Component
public class RequestMetricsCollector {

    private final LongAdder totalRequests = new LongAdder();
    private final LongAdder totalLatencyNanos = new LongAdder();
    private final LongAdder windowRequests = new LongAdder();
    private final LongAdder windowLatencyNanos = new LongAdder();

    public void record(long durationNanos) {
        totalRequests.increment();
        totalLatencyNanos.add(durationNanos);
        windowRequests.increment();
        windowLatencyNanos.add(durationNanos);
    }

    public Snapshot snapshotAndResetWindow(double intervalSeconds) {
        long requests = windowRequests.sumThenReset();
        long latencyNanos = windowLatencyNanos.sumThenReset();
        double avgLatencyMs = requests == 0 ? 0.0 : (latencyNanos / 1_000_000.0) / requests;
        double throughputRps = intervalSeconds <= 0 ? 0.0 : requests / intervalSeconds;
        return new Snapshot(requests, avgLatencyMs, throughputRps);
    }

    public record Snapshot(long requests, double avgLatencyMs, double throughputRps) {
    }
}
