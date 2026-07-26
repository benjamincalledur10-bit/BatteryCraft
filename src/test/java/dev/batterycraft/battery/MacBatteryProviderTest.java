package dev.batterycraft.battery;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MacBatteryProviderTest {
    @Test
    void parsesPercentageAndRemainingTime() {
        String output = "Now drawing from 'Battery Power'\n"
                + " -InternalBattery-0 (id=1) 73%; discharging; 2:35 remaining present: true\n";
        BatteryStatus status = MacBatteryProvider.parse(output).orElseThrow();
        assertEquals(73, status.percentage());
        assertFalse(status.pluggedIn());
        assertEquals(155, status.remainingMinutes());
        assertEquals("2h 35m", status.remainingText());
    }
}
