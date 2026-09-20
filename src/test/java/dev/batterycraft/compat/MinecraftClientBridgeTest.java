package dev.batterycraft.compat;

import net.minecraft.class_2561;
import net.minecraft.class_310;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MinecraftClientBridgeTest {
    public static final class ModernPlayer {
        String chat;
        String overlay;
        public void sendSystemMessage(class_2561 text) { chat = text.getString(); }
        public void sendOverlayMessage(class_2561 text) { overlay = text.getString(); }
    }

    public static final class LegacyPlayer {
        String message;
        boolean overlay;
        public void method_7353(class_2561 text, boolean actionBar) {
            message = text.getString();
            overlay = actionBar;
        }
    }

    @AfterEach
    void clearPlayer() { class_310.method_1551().field_1724 = null; }

    @Test
    void routesModernMessagesToChatAndOverlay() {
        ModernPlayer player = new ModernPlayer();
        class_310.method_1551().field_1724 = player;
        MinecraftClientBridge bridge = new MinecraftClientBridge();
        bridge.message("Profile changed", false);
        assertEquals("Profile changed", player.chat);
        assertNull(player.overlay);
        bridge.message("Battery 50%", true);
        assertEquals("Battery 50%", player.overlay);
        assertEquals("Profile changed", player.chat);
    }

    @Test
    void preservesLegacyMessageRouting() {
        LegacyPlayer player = new LegacyPlayer();
        class_310.method_1551().field_1724 = player;
        MinecraftClientBridge bridge = new MinecraftClientBridge();
        bridge.message("Profile changed", false);
        assertEquals("Profile changed", player.message);
        assertEquals(false, player.overlay);
        bridge.message("Battery 50%", true);
        assertEquals("Battery 50%", player.message);
        assertEquals(true, player.overlay);
    }
}
