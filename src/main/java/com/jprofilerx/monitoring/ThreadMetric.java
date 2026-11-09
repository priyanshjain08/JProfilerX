package com.jprofilerx.monitoring;

public record ThreadMetric(long id, String name, String state, long cpuTimeNanos, boolean daemon, int priority, String lockName) { }
