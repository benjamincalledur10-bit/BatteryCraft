package dev.batterycraft.compat;

import dev.batterycraft.profile.PowerProfile;
import dev.batterycraft.profile.ProfileSettings;
import net.minecraft.class_310;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinecraftSettingsAdapterTest {
    @Test
    void appliesAndRestoresIntermediaryMinecraftOptions() {
        MinecraftSettingsAdapter adapter = new MinecraftSettingsAdapter();
        class_310 client = class_310.method_1551();

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
}
