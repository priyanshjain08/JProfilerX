package com.jprofilerx.jfr;

/** JFR recordings are intentionally not fabricated. This build reports support only when a target-side JFR control path is implemented. */
public final class JfrSupport {
    private JfrSupport() { }
    public static String availabilityMessage() {
        return "JFR event streaming/control is not enabled in this build. Use the target JVM's jcmd JFR.start/JFR.stop commands (or Java Mission Control) to record supported events. Core MXBean monitoring remains available.";
    }
}
