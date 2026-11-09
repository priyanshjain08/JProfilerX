package com.jprofilerx.memory;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class MemoryAnalysisTest {
 @Test void utilizationHandlesUndefinedMax(){assertEquals(-1,MemoryAnalysis.utilization(50,-1));}
 @Test void utilizationIsBounded(){assertEquals(50,MemoryAnalysis.utilization(50,100));assertEquals(100,MemoryAnalysis.utilization(200,100));}
 @Test void formatsBytes(){assertEquals("0 B",MemoryAnalysis.formatBytes(0));assertEquals("1.0 KB",MemoryAnalysis.formatBytes(1024));}
}
