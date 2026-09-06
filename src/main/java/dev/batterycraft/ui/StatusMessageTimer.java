package dev.batterycraft.ui;

/** Uses elapsed time so enabling a reminder always waits one complete interval. */
public final class StatusMessageTimer {
    private long started;
    private long interval;
    private boolean armed;

    public synchronized boolean due(long now, boolean enabled, int seconds) {
        long requested = seconds * 1_000_000_000L;
        if (!enabled) { armed = false; return false; }
        if (!armed || interval != requested) {
            armed = true;
            started = now;
            interval = requested;
            return false;
        }
        if (now - started < interval) return false;
        started = now;
        return true;
    }

    public synchronized void reset() { armed = false; }
}
