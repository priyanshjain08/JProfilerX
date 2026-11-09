package com.jprofilerx.monitoring;

import com.jprofilerx.config.AppSettings;
import com.sun.management.ThreadMXBean;
import javax.management.MBeanServerConnection;
import java.lang.management.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.*;

public final class MonitoringService implements AutoCloseable {
    private static final Logger LOG = Logger.getLogger(MonitoringService.class.getName());
    private final AppSettings settings;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> { Thread t = new Thread(r, "jprofilerx-monitor"); t.setDaemon(true); return t; });
    private final Deque<MetricSnapshot> history = new ArrayDeque<>();
    private final AtomicBoolean paused = new AtomicBoolean(true);
    private volatile MBeanServerConnection connection;
    private volatile String pid = "";
    private volatile Consumer<MetricSnapshot> listener = s -> { };
    private volatile ScheduledFuture<?> task;
    private Map<String, long[]> previousGc = new HashMap<>();

    public MonitoringService(AppSettings settings) { this.settings = settings; }
    public synchronized void attach(MBeanServerConnection c, String targetPid) { stop(); connection = c; pid = targetPid; previousGc.clear(); history.clear(); }
    public synchronized void start() {
        if (connection == null) throw new IllegalStateException("Connect to a JVM first.");
        paused.set(false); schedule();
    }
    public synchronized void pause() { paused.set(true); if (task != null) task.cancel(false); }
    public synchronized void resume() { if (connection == null) return; paused.set(false); schedule(); }
    public synchronized void stop() { pause(); connection = null; history.clear(); }
    public boolean isPaused() { return paused.get(); }
    public synchronized void setListener(Consumer<MetricSnapshot> l) { listener = l == null ? s -> { } : l; }
    public synchronized List<MetricSnapshot> history() { return List.copyOf(history); }
    public synchronized void resetHistory() { history.clear(); }
    public synchronized void setRefreshSeconds(int seconds) { settings.setRefreshSeconds(seconds); if (!paused.get()) schedule(); }
    private synchronized void schedule() {
        if (task != null) task.cancel(false);
        task = executor.scheduleWithFixedDelay(this::collectSafely, 0, settings.getRefreshSeconds(), TimeUnit.SECONDS);
    }
    private void collectSafely() {
        if (paused.get() || connection == null) return;
        try {
            MetricSnapshot raw = collect(connection, pid);
            List<GcMetric> deltas = new ArrayList<>();
            for (GcMetric gc : raw.collectors()) {
                long[] prev = previousGc.get(gc.name());
                long dc = prev == null || gc.collectionCount() < prev[0] ? 0 : gc.collectionCount() - prev[0];
                long dt = prev == null || gc.collectionTimeMs() < prev[1] ? 0 : gc.collectionTimeMs() - prev[1];
                deltas.add(new GcMetric(gc.name(), gc.collectionCount(), gc.collectionTimeMs(), dc, dt));
                previousGc.put(gc.name(), new long[]{gc.collectionCount(), gc.collectionTimeMs()});
            }
            MetricSnapshot s = new MetricSnapshot(raw.timestamp(), raw.pid(), raw.runtimeName(), raw.uptimeMs(), raw.heapUsed(), raw.heapCommitted(), raw.heapMax(), raw.nonHeapUsed(), raw.nonHeapCommitted(), raw.liveThreads(), raw.peakThreads(), raw.daemonThreads(), raw.loadedClasses(), raw.totalLoadedClasses(), raw.unloadedClasses(), raw.gcCount(), raw.gcTimeMs(), raw.pools(), raw.threads(), deltas, raw.deadlockedThreadIds());
            synchronized (this) { history.addLast(s); while (history.size() > settings.getHistoryLimit()) history.removeFirst(); }
            listener.accept(s);
        } catch (Exception e) { LOG.log(Level.WARNING, "Metric collection failed", e); }
    }
    public static MetricSnapshot collect(MBeanServerConnection c, String pid) throws Exception {
        MemoryMXBean mem = ManagementFactory.newPlatformMXBeanProxy(c, ManagementFactory.MEMORY_MXBEAN_NAME, MemoryMXBean.class);
        RuntimeMXBean runtime = ManagementFactory.newPlatformMXBeanProxy(c, ManagementFactory.RUNTIME_MXBEAN_NAME, RuntimeMXBean.class);
        ThreadMXBean threads = ManagementFactory.newPlatformMXBeanProxy(c, ManagementFactory.THREAD_MXBEAN_NAME, ThreadMXBean.class);
        ClassLoadingMXBean classes = ManagementFactory.newPlatformMXBeanProxy(c, ManagementFactory.CLASS_LOADING_MXBEAN_NAME, ClassLoadingMXBean.class);
        MemoryUsage heap = mem.getHeapMemoryUsage(), non = mem.getNonHeapMemoryUsage();
        List<MemoryPoolMetric> pools = new ArrayList<>();
        for (MemoryPoolMXBean p : ManagementFactory.getPlatformMXBeans(c, MemoryPoolMXBean.class)) {
            MemoryUsage u = p.getUsage(); if (u == null) continue;
            long max = u.getMax(); boolean supported = p.isUsageThresholdSupported();
            pools.add(new MemoryPoolMetric(p.getName(), p.getType().name(), u.getUsed(), u.getCommitted(), max,
                    MetricSnapshot.percent(u.getUsed(), max), supported ? p.getUsageThreshold() : -1,
                    supported ? p.getUsageThresholdCount() : -1, supported));
        }
        List<GcMetric> gcs = new ArrayList<>(); long gcCount = 0, gcTime = 0;
        for (GarbageCollectorMXBean gc : ManagementFactory.getPlatformMXBeans(c, GarbageCollectorMXBean.class)) {
            long count = gc.getCollectionCount(), time = gc.getCollectionTime();
            gcCount += Math.max(0, count); gcTime += Math.max(0, time);
            gcs.add(new GcMetric(gc.getName(), count, time, 0, 0));
        }
        List<ThreadMetric> threadList = new ArrayList<>();
        long[] ids = threads.getAllThreadIds();
        ThreadInfo[] infos = threads.getThreadInfo(ids, 0);
        boolean cpu = threads.isThreadCpuTimeSupported() && threads.isThreadCpuTimeEnabled();
        for (int i=0; i<infos.length; i++) {
            ThreadInfo info = infos[i]; if (info == null) continue;
            long cpuTime = -1;
            if (cpu) try { cpuTime = threads.getThreadCpuTime(info.getThreadId()); } catch (UnsupportedOperationException ignored) { }
            threadList.add(new ThreadMetric(info.getThreadId(), info.getThreadName(), info.getThreadState().name(), cpuTime,
                    info.isDaemon(), info.getPriority(), info.getLockName() == null ? "" : info.getLockName()));
        }
        long[] deadlocked;
        try { deadlocked = threads.findDeadlockedThreads(); } catch (UnsupportedOperationException e) { deadlocked = null; }
        return new MetricSnapshot(Instant.now(), pid, runtime.getName(), runtime.getUptime(), heap.getUsed(), heap.getCommitted(), heap.getMax(),
                non.getUsed(), non.getCommitted(), threads.getThreadCount(), threads.getPeakThreadCount(), threads.getDaemonThreadCount(),
                classes.getLoadedClassCount(), classes.getTotalLoadedClassCount(), classes.getUnloadedClassCount(), gcCount, gcTime,
                pools, threadList, gcs, deadlocked);
    }
    @Override public void close() { stop(); executor.shutdownNow(); }
}
