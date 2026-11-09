package com.jprofilerx.report;
import com.jprofilerx.monitoring.MetricSnapshot;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.time.Instant;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class ReportExporterTest {
 @Test void writesCsvAndJson() throws Exception {Path d=Files.createTempDirectory("jpx");var s=new MetricSnapshot(Instant.now(),"42","runtime",1,10,20,30,4,5,2,2,1,3,3,0,0,0,List.of(),List.of(),List.of(),new long[0]);var e=new ReportExporter();Path csv=e.exportCsv(List.of(s),d.resolve("x.csv"));Path json=e.exportJson(List.of(s),List.of(),d.resolve("x.json"));assertTrue(Files.readString(csv).contains("heap_used"));assertTrue(Files.readString(json).contains("runtime"));}
}
