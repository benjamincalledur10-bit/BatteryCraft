package dev.batterycraft.battery;

public record BatteryStatus(int percentage, boolean pluggedIn, boolean charging, int remainingMinutes) {
    public BatteryStatus {
        percentage = Math.max(0, Math.min(100, percentage));
        remainingMinutes = Math.max(-1, remainingMinutes);
    }

    public BatteryStatus(int percentage, boolean pluggedIn, boolean charging) {
        this(percentage, pluggedIn, charging, -1);
    }

    public String remainingText() {
        if (remainingMinutes < 0 || pluggedIn) return "";
        return "%dh %02dm".formatted(remainingMinutes / 60, remainingMinutes % 60);
    }
}
