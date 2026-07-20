package dev.batterycraft.profile;

public enum ManualMode {
    AUTOMATIC,
    BATTERY,
    LOW_BATTERY,
    CRITICAL_BATTERY,
    DISABLED;

    public ManualMode next() {
        ManualMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public PowerProfile forcedProfile() {
        return switch (this) {
            case BATTERY -> PowerProfile.BATTERY;
            case LOW_BATTERY -> PowerProfile.LOW_BATTERY;
            case CRITICAL_BATTERY -> PowerProfile.CRITICAL_BATTERY;
            case AUTOMATIC, DISABLED -> PowerProfile.PLUGGED_IN;
        };
    }
}
