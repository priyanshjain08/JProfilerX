package com.jprofilerx.memory;

import com.jprofilerx.monitoring.MemoryPoolMetric;
import java.util.List;

public final class MemoryAnalysis {
    private MemoryAnalysis() { }
    public static double utilization(long used, long max) { return max <= 0 ? -1 : Math.min(100, Math.max(0, used * 100d / max)); }
    public static List<MemoryPoolMetric> sortByUsage(List<MemoryPoolMetric> pools) {
        return pools.stream().sorted((a,b) -> Long.compare(b.used(), a.used())).toList();
    }
    public static String formatBytes(long bytes) {
        if (bytes < 0) return "Unavailable";
        if (bytes < 1024) return bytes + " B";
        String[] units = {"KB", "MB", "GB", "TB"}; double v = bytes; int i = -1;
        do { v /= 1024; i++; } while (v >= 1024 && i < units.length - 1);
        return String.format(java.util.Locale.ROOT, "%.1f %s", v, units[i]);
    }
}
