package com.jprofilerx.process;

import java.time.Instant;

public record JvmProcessInfo(String pid, String displayName, String mainClass, String javaVersion,
                             String arguments, Instant startTime, boolean attachListed) {
    @Override public String toString() { return pid + "  ·  " + displayName + (mainClass.isBlank() ? "" : " — " + mainClass); }
}
