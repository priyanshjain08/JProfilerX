package com.jprofilerx.process;

import com.sun.tools.attach.VirtualMachine;
import javax.management.MBeanServerConnection;
import javax.management.remote.*;
import java.io.IOException;
import java.util.Map;

public final class JmxConnectionService implements AutoCloseable {
    private JMXConnector connector;
    private String connectedPid;

    public synchronized MBeanServerConnection connect(String pid) throws Exception {
        disconnect();
        VirtualMachine vm = null;
        try {
            vm = VirtualMachine.attach(pid);
            String address;
            try { address = vm.startLocalManagementAgent(); }
            catch (UnsupportedOperationException | IOException ex) {
                address = vm.getAgentProperties().getProperty("com.sun.management.jmxremote.localConnectorAddress");
                if (address == null) throw new IOException("This JVM does not expose a local JMX management agent. " + ex.getMessage(), ex);
            }
            if (address == null || address.isBlank()) throw new IOException("The target JVM did not provide a local JMX connector address.");
            connector = JMXConnectorFactory.connect(new JMXServiceURL(address), Map.of());
            connectedPid = pid;
            return connector.getMBeanServerConnection();
        } catch (Exception e) {
            disconnect();
            throw new IOException("Unable to attach to PID " + pid + ": " + e.getMessage() + ". Check user permissions, JVM compatibility, and whether the process allows attach.", e);
        } finally { if (vm != null) try { vm.detach(); } catch (IOException ignored) { } }
    }
    public synchronized void disconnect() {
        if (connector != null) try { connector.close(); } catch (IOException ignored) { }
        connector = null; connectedPid = null;
    }
    public synchronized javax.management.MBeanServerConnection serverConnection() throws IOException {
        if (connector == null) throw new IOException("No active JMX connection.");
        return connector.getMBeanServerConnection();
    }
    public synchronized boolean isConnected() { return connector != null; }
    public synchronized String connectedPid() { return connectedPid; }
    @Override public void close() { disconnect(); }
}
