package dev.batterycraft.stats;

import dev.batterycraft.battery.BatteryStatus;
import dev.batterycraft.profile.PowerProfile;

import java.time.Duration;
import java.time.Instant;

public final class SessionStats {
    private final Instant startedAt = Instant.now();
    private int initialBattery = -1;
    private int currentBattery = -1;
    private int profileChanges;
    private PowerProfile currentProfile = PowerProfile.PLUGGED_IN;

    public synchronized void battery(BatteryStatus status) {
        if (initialBattery < 0) initialBattery = status.percentage();
        currentBattery = status.percentage();
    }

    public synchronized void profileChanged(PowerProfile profile) {
        if (profile != currentProfile) profileChanges++;
        currentProfile = profile;
    }

    public synchronized String summary() {
        long minutes = Duration.between(startedAt, Instant.now()).toMinutes();
        return "Sesion: " + minutes + " min | bateria " + initialBattery + "% -> " + currentBattery
                + "% | cambios " + profileChanges + " | perfil " + currentProfile;
    }
}
