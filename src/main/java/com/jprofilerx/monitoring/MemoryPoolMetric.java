package com.jprofilerx.monitoring;

public record MemoryPoolMetric(String name, String type, long used, long committed, long max,
                               double utilizationPercent, long threshold, long thresholdCount, boolean thresholdSupported) { }
