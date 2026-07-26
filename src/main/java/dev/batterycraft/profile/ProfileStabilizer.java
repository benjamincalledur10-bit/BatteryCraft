package dev.batterycraft.profile;

public final class ProfileStabilizer {
    private PowerProfile committed;
    private PowerProfile candidate;
    private long candidateSince;

    public synchronized PowerProfile select(PowerProfile requested, long now, int delaySeconds) {
        if (committed == null || requested == PowerProfile.CRITICAL_BATTERY) {
            committed = requested;
            candidate = null;
            return committed;
        }
        if (requested == committed) {
            candidate = null;
            return committed;
        }
        if (candidate != requested) {
            candidate = requested;
            candidateSince = now;
            return committed;
        }
        if (now - candidateSince >= Math.max(0, delaySeconds) * 1_000L) {
            committed = candidate;
            candidate = null;
        }
        return committed;
    }

    public synchronized void reset() {
        committed = null;
        candidate = null;
    }
}
