package com.jprofilerx.snapshot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jprofilerx.monitoring.MetricSnapshot;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public final class SnapshotService {
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules().enable(SerializationFeature.INDENT_OUTPUT);
    public Path save(MetricSnapshot snapshot, Path directory) throws Exception {
        Files.createDirectories(directory);
        String name = "snapshot-" + DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS").withZone(java.time.ZoneOffset.UTC).format(snapshot.timestamp()) + ".json";
        Path path = directory.resolve(name); mapper.writeValue(path.toFile(), snapshot); return path;
    }
    public MetricSnapshot load(Path file) throws Exception { return mapper.readValue(file.toFile(), MetricSnapshot.class); }
    public SnapshotComparison compare(MetricSnapshot a, MetricSnapshot b) {
        return new SnapshotComparison(b.heapUsed()-a.heapUsed(), b.nonHeapUsed()-a.nonHeapUsed(),
                b.liveThreads()-a.liveThreads(), b.loadedClasses()-a.loadedClasses(),
                b.totalLoadedClasses()-a.totalLoadedClasses(), b.unloadedClasses()-a.unloadedClasses(),
                b.gcCount()-a.gcCount(), b.gcTimeMs()-a.gcTimeMs());
    }
    public record SnapshotComparison(long heapDelta, long nonHeapDelta, int liveThreadDelta, int loadedClassDelta,
                                     long totalLoadedClassDelta, long unloadedClassDelta, long gcCountDelta, long gcTimeDeltaMs) { }
}
