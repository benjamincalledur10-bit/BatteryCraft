package dev.batterycraft.compat;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.class_304;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigurableKeyMappingsTest {
    @Test
    void registersAndConsumesLegacyFabricKeyMappings() {
        KeyBindingHelper.REGISTERED.clear();
        ConfigurableKeyMappings mappings = new ConfigurableKeyMappings();
        assertTrue(mappings.register());
        assertEquals(2, KeyBindingHelper.REGISTERED.size());
        assertEquals("category.batterycraft", KeyBindingHelper.REGISTERED.getFirst().category);
        assertEquals(66, KeyBindingHelper.REGISTERED.getFirst().keyCode);

        ((class_304) KeyBindingHelper.REGISTERED.getFirst()).press();
        assertTrue(mappings.consumeCycle());
        assertFalse(mappings.consumeCycle());
    }
}
