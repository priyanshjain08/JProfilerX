package com.jprofilerx.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.*;

public final class AppSettings {
    private static final Path FILE = Path.of(System.getProperty("user.home"), ".jprofilerx", "settings.json");
    private int refreshSeconds = 2;
    private int historyLimit = 300;
    private double memoryWarningPercent = 85;
    private int leakSensitivity = 8;
    private String reportDirectory = Path.of(System.getProperty("user.home"), "JProfilerX", "reports").toString();
    private String snapshotDirectory = Path.of(System.getProperty("user.home"), "JProfilerX", "snapshots").toString();
    private boolean darkTheme = true;
    private boolean autoMonitor = false;

    public static AppSettings load() {
        try { if (Files.exists(FILE)) return new ObjectMapper().readValue(FILE.toFile(), AppSettings.class); }
        catch (Exception ignored) { }
        return new AppSettings();
    }
    public void save() {
        try { Files.createDirectories(FILE.getParent()); new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(FILE.toFile(), this); }
        catch (Exception e) { throw new IllegalStateException("Could not save settings: " + e.getMessage(), e); }
    }
    public int getRefreshSeconds() { return refreshSeconds; }
    public void setRefreshSeconds(int v) { refreshSeconds = Math.max(1, Math.min(60, v)); }
    public int getHistoryLimit() { return historyLimit; }
    public void setHistoryLimit(int v) { historyLimit = Math.max(30, Math.min(5000, v)); }
    public double getMemoryWarningPercent() { return memoryWarningPercent; }
    public void setMemoryWarningPercent(double v) { memoryWarningPercent = Math.max(50, Math.min(99, v)); }
    public int getLeakSensitivity() { return leakSensitivity; }
    public void setLeakSensitivity(int v) { leakSensitivity = Math.max(3, Math.min(30, v)); }
    public String getReportDirectory() { return reportDirectory; }
    public void setReportDirectory(String v) { reportDirectory = v; }
    public String getSnapshotDirectory() { return snapshotDirectory; }
    public void setSnapshotDirectory(String v) { snapshotDirectory = v; }
    public boolean isDarkTheme() { return darkTheme; }
    public void setDarkTheme(boolean v) { darkTheme = v; }
    public boolean isAutoMonitor() { return autoMonitor; }
    public void setAutoMonitor(boolean v) { autoMonitor = v; }
}
