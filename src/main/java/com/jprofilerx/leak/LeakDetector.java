package com.jprofilerx.leak;

import com.jprofilerx.config.AppSettings;
import com.jprofilerx.monitoring.MetricSnapshot;
import java.time.Instant;
import java.util.*;

public final class LeakDetector {
    public enum Severity { INFO, WARNING, CRITICAL }
    public record Finding(Severity severity, Instant timestamp, String title, String explanation, String evidence, String recommendation) { }
    private final AppSettings settings;
    public LeakDetector(AppSettings settings) { this.settings = settings; }
    public List<Finding> analyze(List<MetricSnapshot> samples) {
        if (samples.size() < settings.getLeakSensitivity()) return List.of();
        int n = Math.min(samples.size(), Math.max(settings.getLeakSensitivity() * 3, 12));
        List<MetricSnapshot> recent = samples.subList(samples.size()-n, samples.size());
        List<Finding> out = new ArrayList<>();
        double first = recent.get(0).heapUsed(), last = recent.get(recent.size()-1).heapUsed();
        long max = recent.get(recent.size()-1).heapMax();
        double growth = first <= 0 ? 0 : (last-first)/first*100;
        long rising = 0;
        for (int i=1;i<recent.size();i++) if (recent.get(i).heapUsed() > recent.get(i-1).heapUsed()) rising++;
        double pct = MetricSnapshot.percent((long) last, max);
        if (growth > 25 && rising >= recent.size()*0.70) {
            out.add(new Finding(growth > 75 ? Severity.CRITICAL : Severity.WARNING, Instant.now(), "Sustained heap growth",
                    "Heap usage increased consistently across the observed window. This is an anomaly, not proof of a memory leak.",
                    String.format(Locale.ROOT, "Heap: %s → %s (%.1f%% growth); %d/%d intervals increased.", bytes(first), bytes(last), growth, rising, recent.size()-1),
                    "Capture snapshots after comparable workload and GC cycles; inspect retained objects with a heap-dump analyzer."));
        }
        if (pct >= settings.getMemoryWarningPercent()) {
            out.add(new Finding(pct >= 95 ? Severity.CRITICAL : Severity.WARNING, Instant.now(), "High heap utilization",
                    "Heap usage is above the configured warning threshold.", String.format(Locale.ROOT, "Heap utilization %.1f%%; threshold %.1f%%.", pct, settings.getMemoryWarningPercent()),
                    "Check allocation rate, old-generation occupancy, GC frequency, and configured heap sizing."));
        }
        // Pool trend is only flagged when the pool has a stable name and enough observations.
        Map<String, List<Long>> oldGen = new HashMap<>();
        for (MetricSnapshot s : recent) for (var p : s.pools()) if (p.type().equals("HEAP") && p.name().toLowerCase(Locale.ROOT).matches(".*(old|tenured).*"))
            oldGen.computeIfAbsent(p.name(), k -> new ArrayList<>()).add(p.used());
        oldGen.forEach((name, values) -> {
            if (values.size() >= settings.getLeakSensitivity() && values.get(values.size()-1) > values.get(0)*1.20)
                out.add(new Finding(Severity.WARNING, Instant.now(), "Old-generation pool growth", "A likely old-generation pool has grown over the sample window; workload effects can produce the same pattern.",
                        name + ": " + bytes(values.get(0)) + " → " + bytes(values.get(values.size()-1)), "Compare post-GC baselines over repeated workload cycles."));
        });
        return List.copyOf(out);
    }
    private static String bytes(double b) { return String.format(Locale.ROOT, "%.1f MiB", b/1048576d); }
}
