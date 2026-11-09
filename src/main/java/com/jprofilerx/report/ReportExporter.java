package com.jprofilerx.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jprofilerx.leak.LeakDetector;
import com.jprofilerx.monitoring.MetricSnapshot;
import java.nio.file.*;
import java.util.*;

public final class ReportExporter {
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules().enable(SerializationFeature.INDENT_OUTPUT);
    public Path exportJson(List<MetricSnapshot> samples, List<LeakDetector.Finding> findings, Path file) throws Exception {
        Files.createDirectories(file.toAbsolutePath().getParent()); mapper.writeValue(file.toFile(), report(samples, findings)); return file;
    }
    public Path exportCsv(List<MetricSnapshot> samples, Path file) throws Exception {
        Files.createDirectories(file.toAbsolutePath().getParent());
        try (var w = Files.newBufferedWriter(file)) {
            w.write("timestamp,pid,heap_used,heap_committed,heap_max,non_heap_used,live_threads,loaded_classes,gc_count,gc_time_ms\n");
            for (MetricSnapshot s : samples) w.write(String.join(",", quote(s.timestamp().toString()), quote(s.pid()), Long.toString(s.heapUsed()), Long.toString(s.heapCommitted()), Long.toString(s.heapMax()), Long.toString(s.nonHeapUsed()), Integer.toString(s.liveThreads()), Integer.toString(s.loadedClasses()), Long.toString(s.gcCount()), Long.toString(s.gcTimeMs())) + "\n");
        } return file;
    }
    public Path exportHtml(List<MetricSnapshot> samples, List<LeakDetector.Finding> findings, Path file) throws Exception {
        Files.createDirectories(file.toAbsolutePath().getParent());
        MetricSnapshot latest = samples.isEmpty() ? null : samples.get(samples.size()-1);
        StringBuilder html = new StringBuilder("<!doctype html><html><head><meta charset='utf-8'><title>JProfilerX report</title><style>body{font:15px system-ui;background:#111827;color:#e5e7eb;margin:40px}table{border-collapse:collapse;width:100%}td,th{padding:10px;border-bottom:1px solid #374151;text-align:left}h1{color:#a78bfa}</style></head><body><h1>JProfilerX profiling report</h1>");
        html.append("<p>Samples collected: ").append(samples.size()).append("</p>");
        if (latest != null) html.append("<h2>Latest JVM metrics</h2><table>").append(row("PID",latest.pid())).append(row("Runtime",latest.runtimeName())).append(row("Heap used",latest.heapUsed()+" bytes")).append(row("Heap max",latest.heapMax()+" bytes")).append(row("Non-heap used",latest.nonHeapUsed()+" bytes")).append(row("Threads",Integer.toString(latest.liveThreads()))).append(row("Loaded classes",Integer.toString(latest.loadedClasses()))).append(row("GC count / time",latest.gcCount()+" / "+latest.gcTimeMs()+" ms")).append("</table>");
        html.append("<h2>Anomaly findings</h2>"); if (findings.isEmpty()) html.append("<p>No heuristic findings (or insufficient samples).</p>");
        for (var f : findings) html.append("<h3>").append(esc(f.severity()+" — "+f.title())).append("</h3><p>").append(esc(f.explanation()+" Evidence: "+f.evidence()+" Recommendation: "+f.recommendation())).append("</p>");
        html.append("<p><small>Metrics are sampled; GC events and leak findings are estimates/heuristics, not exhaustive event capture or proof of a leak.</small></p></body></html>");
        Files.writeString(file, html.toString()); return file;
    }
    private Map<String,Object> report(List<MetricSnapshot> s, List<LeakDetector.Finding> f) { return Map.of("generatedAt",java.time.Instant.now(),"sampleCount",s.size(),"samples",s,"findings",f,"limitations",List.of("GC MXBean counters are cumulative and do not expose every individual event.","Memory growth alone cannot prove a memory leak.","Unavailable target metrics are represented as unavailable or omitted.")); }
    private static String row(String k,String v) { return "<tr><th>"+esc(k)+"</th><td>"+esc(v)+"</td></tr>"; }
    private static String esc(String s) { return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;"); }
    private static String quote(String s) { return "\""+s.replace("\"","\"\"")+"\""; }
}
