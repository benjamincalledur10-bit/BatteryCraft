package dev.batterycraft.compat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecoveryStoreTest {
    @Test
    void savesLoadsAndClearsSnapshot(@TempDir Path directory) {
        RecoveryStore store = new RecoveryStore(directory.resolve("recovery.properties"));
        store.save(Map.of("maxFps", "integer:144", "clouds", "boolean:true"));
        assertTrue(store.exists());
        assertEquals("integer:144", store.load().get("maxFps"));
        store.clear();
        assertFalse(store.exists());
    }
}
