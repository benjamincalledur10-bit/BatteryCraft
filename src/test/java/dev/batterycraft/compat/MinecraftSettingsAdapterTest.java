package dev.batterycraft.compat;

import dev.batterycraft.profile.PowerProfile;
import dev.batterycraft.profile.ProfileSettings;
import net.minecraft.class_310;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinecraftSettingsAdapterTest {
    @Test
    void appliesAndRestoresIntermediaryMinecraftOptions() {
        MinecraftSettingsAdapter adapter = new MinecraftSettingsAdapter();
        class_310 client = class_310.method_1551();
        reset(client);

        ProfileSettings critical = new ProfileSettings(30, 6, 5, 2, false, false, 0, 0.50);
        assertTrue(adapter.apply(PowerProfile.CRITICAL_BATTERY, critical, true));
        assertEquals(30, client.field_1690.field_1909.method_41753());
        assertEquals(6, client.field_1690.field_1870.method_41753());
        assertEquals(5, client.field_1690.field_34959.method_41753());
        assertEquals(net.minecraft.class_315.Particle.MINIMAL, client.field_1690.field_1882.method_41753());
        assertEquals(false, client.field_1690.field_1888.method_41753());
        assertEquals(0.50, client.field_1690.field_24214.method_41753());

        assertTrue(adapter.apply(PowerProfile.PLUGGED_IN, null, true));
        assertEquals(144, client.field_1690.field_1909.method_41753());
        assertEquals(16, client.field_1690.field_1870.method_41753());
        assertEquals(12, client.field_1690.field_34959.method_41753());
        assertEquals(net.minecraft.class_315.Particle.ALL, client.field_1690.field_1882.method_41753());
        assertEquals(true, client.field_1690.field_1888.method_41753());
        assertEquals(1.0, client.field_1690.field_24214.method_41753());
    }

    @Test
    void restoresOriginalSettingsAfterAdapterRestart(@TempDir Path directory) {
        Path recovery = directory.resolve("batterycraft-recovery.properties");
        class_310 client = class_310.method_1551();
        reset(client);
        client.field_1690.field_1909.method_41748(165);

        MinecraftSettingsAdapter first = new MinecraftSettingsAdapter();
        first.configureRecovery(recovery);
        ProfileSettings battery = new ProfileSettings(60, 10, 8, 1, true, true, 2, 0.80);
        assertTrue(first.apply(PowerProfile.BATTERY, battery, false));
        assertEquals(60, client.field_1690.field_1909.method_41753());

        MinecraftSettingsAdapter restarted = new MinecraftSettingsAdapter();
        restarted.configureRecovery(recovery);
        assertTrue(restarted.hasPendingRecovery());
        assertTrue(restarted.apply(PowerProfile.PLUGGED_IN, null, false));
        assertEquals(165, client.field_1690.field_1909.method_41753());
    }

    private static void reset(class_310 client) {
        client.field_1690.field_1909.method_41748(144);
        client.field_1690.field_1870.method_41748(16);
        client.field_1690.field_34959.method_41748(12);
        client.field_1690.field_1882.method_41748(net.minecraft.class_315.Particle.ALL);
        client.field_1690.field_1888.method_41748(true);
        client.field_1690.field_1878.method_41748(5);
        client.field_1690.field_1814.method_41748(net.minecraft.class_315.Cloud.FANCY);
        client.field_1690.field_24214.method_41748(1.0);
    }
}
