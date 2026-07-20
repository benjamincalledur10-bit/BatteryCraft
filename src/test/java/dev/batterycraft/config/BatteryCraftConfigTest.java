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
        assertEquals(config, BatteryCraftConfig.load(path));
    }
}
