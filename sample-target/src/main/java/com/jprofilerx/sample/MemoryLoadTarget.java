package com.jprofilerx.sample;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/** Deliberately noisy local target for exercising JProfilerX. */
public final class MemoryLoadTarget {
    private static final List<byte[]> RETAINED = new CopyOnWriteArrayList<>();
    private static final AtomicBoolean RUNNING = new AtomicBoolean(true);
    private MemoryLoadTarget() { }
    public static void main(String[] args) throws Exception {
        System.out.println("Sample target PID=" + ProcessHandle.current().pid());
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        scheduler.scheduleAtFixedRate(() -> { if (RETAINED.size() < 300) RETAINED.add(new byte[256 * 1024]); }, 0, 700, TimeUnit.MILLISECONDS);
        scheduler.scheduleAtFixedRate(() -> { byte[][] churn = new byte[30][]; for (int i=0;i<churn.length;i++) churn[i]=new byte[128*1024]; System.gc(); }, 1, 5, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(() -> { Thread t=new Thread(() -> { try { Thread.sleep(1200); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); } }, "sample-worker-"+System.nanoTime()); t.start(); }, 0, 2, TimeUnit.SECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> { RUNNING.set(false); scheduler.shutdownNow(); }));
        while (RUNNING.get()) { Class.forName("java.util." + (System.nanoTime()%2==0?"ArrayList":"HashMap")); Thread.sleep(1000); }
    }
}
