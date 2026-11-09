package com.jprofilerx.process;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public final class JvmProcessDiscovery {
    public List<JvmProcessInfo> discover() {
        Map<String, VirtualMachineDescriptor> attach = new HashMap<>();
        try { for (VirtualMachineDescriptor d : VirtualMachine.list()) attach.put(d.id(), d); }
        catch (Throwable ignored) { /* ProcessHandle still provides best-effort discovery. */ }
        return ProcessHandle.allProcesses().filter(p -> p.info().command().map(c -> c.toLowerCase(Locale.ROOT).contains("java")).orElse(false)
                        || attach.containsKey(Long.toString(p.pid())))
                .map(p -> {
                    ProcessHandle.Info i = p.info();
                    String command = i.commandLine().orElse(i.command().orElse("Java process"));
                    String args = i.arguments().map(a -> String.join(" ", a)).orElse("");
                    String main = args.isBlank() ? "" : Arrays.stream(args.split(" ")).filter(s -> !s.startsWith("-")).findFirst().orElse("");
                    String name = attach.containsKey(Long.toString(p.pid())) ? attach.get(Long.toString(p.pid())).displayName() : command;
                    return new JvmProcessInfo(Long.toString(p.pid()), name, main, "Unavailable until connected", args,
                            i.startInstant().orElse(null), attach.containsKey(Long.toString(p.pid())));
                }).sorted(Comparator.comparing(JvmProcessInfo::pid)).collect(Collectors.toList());
    }
}
