package dev.batterycraft.profile;

import dev.batterycraft.battery.BatteryStatus;

public enum PowerProfile {
    PLUGGED_IN,
    BATTERY,
    LOW_BATTERY,
    CRITICAL_BATTERY;

    public static PowerProfile select(BatteryStatus status, int lowThreshold, int criticalThreshold) {
        if (status.pluggedIn()) return PLUGGED_IN;
        if (status.percentage() <= criticalThreshold) return CRITICAL_BATTERY;
        if (status.percentage() <= lowThreshold) return LOW_BATTERY;
        return BATTERY;
    }
}
