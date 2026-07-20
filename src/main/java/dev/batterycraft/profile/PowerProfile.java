package dev.batterycraft.profile;

import dev.batterycraft.battery.BatteryStatus;

public enum PowerProfile {
    PLUGGED_IN(0, 0, 0, 0),
    BATTERY(60, 10, 8, 1),
    LOW_BATTERY(45, 8, 6, 2),
    CRITICAL_BATTERY(30, 6, 5, 2);

    private final int maxFps;
    private final int renderDistance;
    private final int simulationDistance;
    private final int particleLevel;

    PowerProfile(int maxFps, int renderDistance, int simulationDistance, int particleLevel) {
        this.maxFps = maxFps;
        this.renderDistance = renderDistance;
        this.simulationDistance = simulationDistance;
        this.particleLevel = particleLevel;
    }

    public static PowerProfile select(BatteryStatus status, int lowThreshold, int criticalThreshold) {
        if (status.pluggedIn()) return PLUGGED_IN;
        if (status.percentage() <= criticalThreshold) return CRITICAL_BATTERY;
        if (status.percentage() <= lowThreshold) return LOW_BATTERY;
        return BATTERY;
    }

    public int maxFps() { return maxFps; }
    public int renderDistance() { return renderDistance; }
    public int simulationDistance() { return simulationDistance; }
    public int particleLevel() { return particleLevel; }
}
