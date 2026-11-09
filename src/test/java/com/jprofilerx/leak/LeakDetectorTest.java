package com.jprofilerx.leak;
import com.jprofilerx.config.AppSettings;
import com.jprofilerx.monitoring.MetricSnapshot;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class LeakDetectorTest {
 @Test void requiresMinimumSamples(){AppSettings a=new AppSettings();a.setLeakSensitivity(8);assertTrue(new LeakDetector(a).analyze(List.of()).isEmpty());}
 @Test void flagsSustainedGrowthAsHeuristic(){AppSettings a=new AppSettings();a.setLeakSensitivity(3);List<MetricSnapshot> xs=new ArrayList<>();for(int i=1;i<=9;i++)xs.add(new MetricSnapshot(Instant.now(),"1","test",1,i*20,100,1000,1,1,1,1,0,1,1,0,0,0,List.of(),List.of(),List.of(),new long[0]));assertTrue(new LeakDetector(a).analyze(xs).stream().anyMatch(f->f.title().contains("growth")));}
}
