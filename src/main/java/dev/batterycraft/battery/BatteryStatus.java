package dev.batterycraft.battery;

public record BatteryStatus(int percentage, boolean pluggedIn, boolean charging) {
    public BatteryStatus { percentage = Math.max(0, Math.min(100, percentage)); }
}
