package com.jprofilerx.app;

import com.jprofilerx.config.AppSettings;
import com.jprofilerx.process.JvmProcessDiscovery;
import com.jprofilerx.process.JmxConnectionService;
import com.jprofilerx.monitoring.MonitoringService;
import com.jprofilerx.snapshot.SnapshotService;
import com.jprofilerx.leak.LeakDetector;
import com.jprofilerx.report.ReportExporter;
import com.jprofilerx.ui.MainWindow;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class JProfilerXApp extends Application {
    private MonitoringService monitoring;
    private JmxConnectionService connections;

    @Override public void start(Stage stage) {
        AppSettings settings = AppSettings.load();
        connections = new JmxConnectionService();
        monitoring = new MonitoringService(settings);
        MainWindow root = new MainWindow(stage, settings, new JvmProcessDiscovery(), connections,
                monitoring, new SnapshotService(), new LeakDetector(settings), new ReportExporter());
        Scene scene = new Scene(root, 1420, 900);
        scene.getStylesheets().add(getClass().getResource("/com/jprofilerx/ui/theme.css").toExternalForm());
        stage.setTitle("JProfilerX — JVM Memory Profiler");
        stage.setMinWidth(1100); stage.setMinHeight(720); stage.setScene(scene); stage.show();
        stage.setOnCloseRequest(e -> shutdown());
    }

    private void shutdown() {
        if (monitoring != null) monitoring.close();
        if (connections != null) connections.disconnect();
    }
    @Override public void stop() { shutdown(); }
    public static void main(String[] args) { launch(args); }
}
