package dev.batterycraft.profile;

import dev.batterycraft.battery.BatteryStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PowerProfileTest {
    @Test
    void selectsPluggedInRegardlessOfPercentage() {
        assertEquals(PowerProfile.PLUGGED_IN, PowerProfile.select(new BatteryStatus(10, true, true), 30, 15));
    }

    @Test
    void selectsEveryBatteryProfileAtItsBoundary() {
        assertEquals(PowerProfile.BATTERY, PowerProfile.select(new BatteryStatus(31, false, false), 30, 15));
        assertEquals(PowerProfile.LOW_BATTERY, PowerProfile.select(new BatteryStatus(30, false, false), 30, 15));
        assertEquals(PowerProfile.CRITICAL_BATTERY, PowerProfile.select(new BatteryStatus(15, false, false), 30, 15));
    }
}
