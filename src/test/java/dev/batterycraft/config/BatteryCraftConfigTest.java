package dev.batterycraft.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class BatteryCraftConfigTest {
    @Test
    void createsAndReloadsDefaults(@TempDir Path directory) {
        Path path = directory.resolve("batterycraft.json");
        BatteryCraftConfig config = BatteryCraftConfig.load(path);
        assertTrue(config.enabled());
        assertFalse(config.hudIndicator());
        assertEquals(10, config.pollSeconds());
        BatteryCraftConfig reloaded = BatteryCraftConfig.load(path);
        assertEquals(config.lowThreshold(), reloaded.lowThreshold());
        assertEquals(config.profile(dev.batterycraft.profile.PowerProfile.BATTERY),
                reloaded.profile(dev.batterycraft.profile.PowerProfile.BATTERY));
    }

    @Test
    void disablesPeriodicStatusWhenMigratingBeta3(@TempDir Path directory) throws Exception {
        Path path = directory.resolve("batterycraft.json");
        Files.writeString(path, """
                {
                  "enabled": true,
                  "hudIndicator": true,
                  "hudIntervalSeconds": 10
                }
                """);

        BatteryCraftConfig migrated = BatteryCraftConfig.load(path);

        assertFalse(migrated.hudIndicator());
        assertTrue(Files.readString(path).contains("\"configVersion\": 3"));
    }

    @Test
    void supportsLongerStatusIntervals(@TempDir Path directory) throws Exception {
        BatteryCraftConfig config = BatteryCraftConfig.load(directory.resolve("batterycraft.json"));
        config.hudIntervalSeconds(1800);
        config.save();

        assertEquals(1800, BatteryCraftConfig.load(config.path()).hudIntervalSeconds());
    }

    @Test
    void migratesOnlyUnmodifiedLegacyProfiles(@TempDir Path directory) throws Exception {
        Path path = directory.resolve("batterycraft.json");
        BatteryCraftConfig config = BatteryCraftConfig.load(path);
        config.profile(dev.batterycraft.profile.PowerProfile.BATTERY,
                new dev.batterycraft.profile.ProfileSettings(60, 10, 8, 1, true, true, 2, 0.80));
        config.profile(dev.batterycraft.profile.PowerProfile.LOW_BATTERY,
                new dev.batterycraft.profile.ProfileSettings(77, 7, 4, 2, false, false, 0, 0.50));
        config.save();
        Files.writeString(path, Files.readString(path).replace("\"configVersion\": 3", "\"configVersion\": 1"));
        BatteryCraftConfig migrated = BatteryCraftConfig.load(path);
        assertEquals(120, migrated.profile(dev.batterycraft.profile.PowerProfile.BATTERY).maxFps());
        assertEquals(77, migrated.profile(dev.batterycraft.profile.PowerProfile.LOW_BATTERY).maxFps());
        migrated.hudIndicator(true);
        migrated.save();
        assertTrue(BatteryCraftConfig.load(path).hudIndicator());
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
