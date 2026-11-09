package com.jprofilerx.monitoring;

public record GcMetric(String name, long collectionCount, long collectionTimeMs, long countDelta, long timeDeltaMs) { }
