
# JProfilerX

JProfilerX is a local-first Java 21 desktop utility for observing JVM memory, garbage collection counters, threads, and class loading through JMX. It uses the Java Attach API to start the target JVM's local management agent when the target supports it. It is designed as an engineering aid, not as a replacement for Java Flight Recorder, Java Mission Control, or a heap-dump analyzer.

> **Safety and privacy:** heap dumps can contain credentials, tokens, personal data, and application payloads. They are written only to the path you choose. JProfilerX does not upload target data.

## Features

- Local Java process discovery with PID, command/display name, arguments, and start time where exposed by the OS.
- Attach-based local JMX connection with explicit error handling for unsupported JVMs and permission restrictions.
- Live heap/non-heap, runtime uptime, memory pools, GC MXBean counters, threads, deadlock checks, and class-loading metrics.
- Bounded metric history and JavaFX time-series charts based on collected samples.
- Pool-level usage and threshold information where the target JVM supports it.
- Heuristic memory anomaly findings with evidence and investigation suggestions; findings are not proof of a leak.
- Timestamped JSON metric snapshots, JSON/CSV/HTML report export, and optional HotSpot heap-dump generation.
- Configurable polling interval, history limit, warning threshold, and leak sample sensitivity.
- A separate sample target application that creates retained allocations, allocation churn, short-lived threads, and class-loading activity.

## Screenshots

Screenshots will be added after captures from a real running build. No mock screenshots are included.

## Architecture

```text
app        JavaFX lifecycle and composition
ui         JavaFX navigation, dashboard, tables, charts, dialogs
process    ProcessHandle discovery and Attach API / local JMX connection
monitoring MXBean polling, immutable metric snapshots, bounded history
memory     Memory calculations and pool ordering
gc         GC metrics represented in monitoring.GcMetric
threads    Thread metrics represented in monitoring.ThreadMetric
classes    Class-loading metrics in MetricSnapshot
snapshot   Local JSON snapshot storage and comparison
leak       Evidence-based heuristic anomaly detection
jfr        Explicit JFR capability/limitation messaging
report     JSON, CSV, and HTML export
config     Persisted local preferences
```

## Technology stack

- Java 21
- Gradle Kotlin DSL
- JavaFX 21
- JMX / platform MXBeans
- Java Attach API (`jdk.attach`)
- Jackson for JSON
- JUnit 5

## Prerequisites

- JDK 21 (not only a JRE).
- Gradle 8.x installed and available as `gradle`.
- A desktop environment supported by JavaFX.
- The profiler and target process should generally run as the same OS user. Attach may be disabled or restricted by policy, containers, security controls, or JVM implementation.

The Gradle OpenJFX plugin resolves the JavaFX native artifacts for the build host. An internet connection may be needed for the first dependency resolution.

## Build and run

From the project root:

```bash
gradle clean test

gradle run
```

On Linux systems where JavaFX cannot initialize due to missing native desktop libraries, install the platform's OpenJFX runtime dependencies and retry. The app needs the `jdk.attach` and `jdk.management` modules; Gradle's application task adds them.

## Connect to a target JVM

1. Start a target JVM locally. For a reproducible target, run `gradle :sample-target:run` in another terminal.
2. Start JProfilerX with `gradle run`.
3. Click **Refresh**, select the target process, and click **Connect**.
4. If attach succeeds, JProfilerX starts/uses the local management agent and begins sampling. Navigate through Overview, Memory Analysis, Garbage Collection, Threads, and Classes.
5. Use **Disconnect** to close the JMX connector cleanly.

The target may require the same user identity. Some JVMs do not support the Attach API or local management agent. A process can appear in discovery even when attach is unavailable; the connection dialog explains the failure. Process arguments may be unavailable or truncated by OS permissions.

## Sample target application

```bash
gradle :sample-target:run
```

The sample gradually retains small byte arrays, generates short-lived allocation churn, runs short-lived worker threads, and exercises class loading. It is deliberately noisy and is not representative of production workloads.

## Snapshots, reports, and heap dumps

- **Snapshots:** Overview/Snapshots stores the latest sampled metric set as JSON in `~/JProfilerX/snapshots` by default.
- **Reports:** JSON includes sample records and anomaly findings; CSV contains a time-series subset; HTML is a readable summary.
- **Heap dump:** Overview offers a HotSpot diagnostic heap dump when `HotSpotDiagnosticMXBean` is exposed. The operation can pause or heavily load the target and the file may be very large. Protect and delete it securely when no longer needed.

## Feature limitations

- **GC event granularity:** `GarbageCollectorMXBean` provides cumulative count/time counters. Deltas between polls estimate collection activity; they do not identify every individual GC event or exact pause duration.
- **JFR:** JFR start/stop and event-browser controls are not wired into this build. Use `jcmd <pid> JFR.start` / `jcmd <pid> JFR.stop` or Java Mission Control on supported HotSpot JVMs. JFR is optional and core MXBean monitoring does not depend on it.
- **Heap dump viewing:** JProfilerX creates a dump but does not parse it. Use Eclipse MAT, Java Mission Control, or another established HPROF analyzer. It never reads a whole dump into profiler memory.
- **Class histogram:** no automatic histogram is collected. A class count is not an object count or retained-size measurement; diagnostic histograms can be expensive and JVM-specific.
- **Leak detection:** warnings are heuristics requiring a configurable minimum sample window. Workload phases, caches, warmup, and GC timing can all cause sustained growth. A suspected leak should be investigated with comparable post-GC snapshots and heap analysis.
- **JVM coverage:** attach/JMX and HotSpot diagnostics vary by vendor, version, OS, security policy, and process permissions. Unsupported metrics are omitted or labeled unavailable rather than simulated.
- **Snapshot comparison:** the service API supports comparing two loaded snapshots; the initial UI focuses on capture/export and does not yet include an interactive two-file comparison screen.

## Troubleshooting

- **No Java processes:** check that the target is local and that OS process metadata is visible; try Refresh.
- **Attach denied:** run both processes as the same user, check container boundaries and attach policy, and ensure the target JVM has not disabled attach.
- **Local JMX agent unavailable:** the target runtime may not implement the Attach management-agent extension. Use a separately configured, secured JMX endpoint or external tools.
- **Heap dump fails:** the target may not be HotSpot-based, may not expose `HotSpotDiagnosticMXBean`, or may lack disk space/permissions.
- **JavaFX startup errors:** use a full JDK 21 and ensure the platform's JavaFX native dependencies are installed.

## Tests

`gradle test` runs unit tests for memory calculations, snapshot comparison, anomaly heuristics, and CSV/JSON export. Process attach and live JavaFX integration are environment-dependent and should be verified manually against the sample target on each supported OS.

## Future improvements

- JFR control and event summaries through `jdk.management.jfr` where available.
- Interactive snapshot-pair comparison and pool trend selection.
- Optional diagnostic class histogram with explicit target-side cost confirmation.
- More comprehensive accessibility, keyboard navigation, and cross-platform UI testing.
- Packaging with `jpackage` for Windows, macOS, and Linux.

## License

Licensed under the MIT License. See [LICENSE](LICENSE).
=======
# JProfilerX
>>>>>>> a06ee8d0d1d6c1f525c8eecbc60fe528058839c8
