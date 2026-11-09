package com.jprofilerx.snapshot;
import com.jprofilerx.monitoring.*;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class SnapshotServiceTest {
 private MetricSnapshot s(long heap,int threads){return new MetricSnapshot(Instant.now(),"1","test",10,heap,heap,1000,20,30,threads,threads,0,4,4,0,1,2,List.of(),List.of(),List.of(),new long[0]);}
 @Test void comparesMetricDeltas(){var d=new SnapshotService().compare(s(100,2),s(160,5));assertEquals(60,d.heapDelta());assertEquals(3,d.liveThreadDelta());}
}
