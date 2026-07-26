package dev.batterycraft.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BatteryCraftConfigTest {
    @Test
    void createsAndReloadsDefaults(@TempDir Path directory) {
        Path path = directory.resolve("batterycraft.json");
        BatteryCraftConfig config = BatteryCraftConfig.load(path);
        assertTrue(config.enabled());
        assertEquals(10, config.pollSeconds());
        BatteryCraftConfig reloaded = BatteryCraftConfig.load(path);
        assertEquals(config.lowThreshold(), reloaded.lowThreshold());
        assertEquals(config.profile(dev.batterycraft.profile.PowerProfile.BATTERY),
                reloaded.profile(dev.batterycraft.profile.PowerProfile.BATTERY));
    }

    @Test
    void persistsBetaOptionsAndProfiles(@TempDir Path directory) throws Exception {
        Path path = directory.resolve("batterycraft.json");
        BatteryCraftConfig config = BatteryCraftConfig.load(path);
        config.notifications(false);
        config.hudIndicator(false);
        config.sodiumIntegration(false);
        config.thermalMode(true);
        config.transitionDelaySeconds(12);
        config.hudIntervalSeconds(20);
        config.manualMode(dev.batterycraft.profile.ManualMode.LOW_BATTERY);
        config.profile(dev.batterycraft.profile.PowerProfile.BATTERY,
                new dev.batterycraft.profile.ProfileSettings(75, 12, 9, 0, false, false, 3, 0.7));
        config.save();

        BatteryCraftConfig reloaded = BatteryCraftConfig.load(path);
        assertEquals(false, reloaded.notifications());
        assertEquals(false, reloaded.hudIndicator());
        assertEquals(false, reloaded.sodiumIntegration());
        assertTrue(reloaded.thermalMode());
        assertEquals(12, reloaded.transitionDelaySeconds());
        assertEquals(20, reloaded.hudIntervalSeconds());
        assertEquals(dev.batterycraft.profile.ManualMode.LOW_BATTERY, reloaded.manualMode());
        assertEquals(config.profile(dev.batterycraft.profile.PowerProfile.BATTERY),
                reloaded.profile(dev.batterycraft.profile.PowerProfile.BATTERY));
    }
}
