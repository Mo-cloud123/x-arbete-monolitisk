package se.mo.xarbetemonolitisk.monitoring;

import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PerformanceMetricsLogger {

    private static final Logger log = LoggerFactory.getLogger(PerformanceMetricsLogger.class);
    private static final double INTERVAL_SECONDS = 30.0;

    private final RequestMetricsCollector metricsCollector;
    private final OperatingSystemMXBean osBean;

    public PerformanceMetricsLogger(RequestMetricsCollector metricsCollector) {
        this.metricsCollector = metricsCollector;
        this.osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    }

    @Scheduled(fixedRateString = "${metrics.log.interval-ms:30000}")
    public void logPerformanceMetrics() {
        RequestMetricsCollector.Snapshot snapshot = metricsCollector.snapshotAndResetWindow(INTERVAL_SECONDS);

        double processCpuPercent = osBean.getProcessCpuLoad() * 100;
        double systemCpuPercent = osBean.getCpuLoad() * 100;

        Runtime runtime = Runtime.getRuntime();
        long usedBytes = runtime.totalMemory() - runtime.freeMemory();
        long maxBytes = runtime.maxMemory();
        long usedMb = usedBytes / (1024 * 1024);
        long maxMb = maxBytes / (1024 * 1024);

        log.info("Perf metrics: latencyMsAvg={} throughputRps={} requestCountWindow={} cpuProcessPct={} cpuSystemPct={} memoryUsedMb={} memoryMaxMb={}",
                String.format("%.2f", snapshot.avgLatencyMs()),
                String.format("%.2f", snapshot.throughputRps()),
                snapshot.requests(),
                String.format("%.2f", processCpuPercent),
                String.format("%.2f", systemCpuPercent),
                usedMb,
                maxMb);
    }
}
