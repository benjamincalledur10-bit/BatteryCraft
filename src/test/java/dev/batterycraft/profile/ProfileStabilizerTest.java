package dev.batterycraft.profile;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProfileStabilizerTest {
    @Test
    void delaysNormalTransitionsButAppliesCriticalImmediately() {
        ProfileStabilizer stabilizer = new ProfileStabilizer();
        assertEquals(PowerProfile.BATTERY, stabilizer.select(PowerProfile.BATTERY, 0, 8));
        assertEquals(PowerProfile.BATTERY, stabilizer.select(PowerProfile.LOW_BATTERY, 1_000, 8));
        assertEquals(PowerProfile.BATTERY, stabilizer.select(PowerProfile.LOW_BATTERY, 8_999, 8));
        assertEquals(PowerProfile.LOW_BATTERY, stabilizer.select(PowerProfile.LOW_BATTERY, 9_000, 8));
        assertEquals(PowerProfile.CRITICAL_BATTERY,
                stabilizer.select(PowerProfile.CRITICAL_BATTERY, 9_001, 8));
    }
}
