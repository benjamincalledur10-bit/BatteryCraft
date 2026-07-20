package dev.batterycraft.compat;

import dev.batterycraft.profile.PowerProfile;
import net.minecraft.class_310;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinecraftSettingsAdapterTest {
    @Test
    void appliesAndRestoresIntermediaryMinecraftOptions() {
        MinecraftSettingsAdapter adapter = new MinecraftSettingsAdapter();
        class_310 client = class_310.method_1551();

        assertTrue(adapter.apply(PowerProfile.CRITICAL_BATTERY));
        assertEquals(30, client.field_1690.field_1909.method_41753());
        assertEquals(6, client.field_1690.field_1870.method_41753());
        assertEquals(5, client.field_1690.field_34959.method_41753());
        assertEquals(net.minecraft.class_315.Particle.MINIMAL, client.field_1690.field_1882.method_41753());

        assertTrue(adapter.apply(PowerProfile.PLUGGED_IN));
        assertEquals(144, client.field_1690.field_1909.method_41753());
        assertEquals(16, client.field_1690.field_1870.method_41753());
        assertEquals(12, client.field_1690.field_34959.method_41753());
        assertEquals(net.minecraft.class_315.Particle.ALL, client.field_1690.field_1882.method_41753());
    }
}
