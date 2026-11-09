package com.jprofilerx.monitoring;

import java.time.Instant;
import java.util.List;

public record MetricSnapshot(Instant timestamp, String pid, String runtimeName, long uptimeMs,
        long heapUsed, long heapCommitted, long heapMax, long nonHeapUsed, long nonHeapCommitted,
        int liveThreads, int peakThreads, int daemonThreads, int loadedClasses, long totalLoadedClasses,
        long unloadedClasses, long gcCount, long gcTimeMs, List<MemoryPoolMetric> pools,
        List<ThreadMetric> threads, List<GcMetric> collectors, long[] deadlockedThreadIds) {
    public MetricSnapshot {
        pools = List.copyOf(pools); threads = List.copyOf(threads); collectors = List.copyOf(collectors);
        deadlockedThreadIds = deadlockedThreadIds == null ? new long[0] : deadlockedThreadIds.clone();
    }
    public double heapPercent() { return percent(heapUsed, heapMax); }
    public static double percent(long used, long max) { return max <= 0 ? -1 : Math.min(100d, Math.max(0d, used * 100d / max)); }
}
