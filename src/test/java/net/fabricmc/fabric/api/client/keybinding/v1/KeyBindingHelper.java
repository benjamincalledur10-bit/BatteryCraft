package net.fabricmc.fabric.api.client.keybinding.v1;

import net.minecraft.class_304;
import java.util.ArrayList;
import java.util.List;

public final class KeyBindingHelper {
    public static final List<class_304> REGISTERED = new ArrayList<>();
    public static class_304 registerKeyBinding(class_304 key) {
        REGISTERED.add(key);
        return key;
    }
}
