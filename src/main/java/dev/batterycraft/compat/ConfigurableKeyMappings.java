package dev.batterycraft.compat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class ConfigurableKeyMappings {
    private Object cycle;
    private Object config;

    public boolean register() {
        try {
            return registerModern();
        } catch (ReflectiveOperationException modernError) {
            try {
                return registerLegacy();
            } catch (ReflectiveOperationException legacyError) {
                System.err.println("[BatteryCraft] Key mappings unavailable: " + legacyError.getMessage());
                return false;
            }
        }
    }

    public boolean consumeCycle() { return consume(cycle); }
    public boolean consumeConfig() { return consume(config); }

    private boolean registerLegacy() throws ReflectiveOperationException {
        Class<?> keyType = load("net.minecraft.class_304", "net.minecraft.client.option.KeyBinding");
        Constructor<?> constructor = keyType.getConstructor(String.class, int.class, String.class);
        cycle = constructor.newInstance("key.batterycraft.cycle_profile", 66, "category.batterycraft");
        config = constructor.newInstance("key.batterycraft.open_config", 79, "category.batterycraft");
        Class<?> helper = Class.forName("net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper");
        invokeRegistration(helper, "registerKeyBinding", cycle);
        invokeRegistration(helper, "registerKeyBinding", config);
        return true;
    }

    private boolean registerModern() throws ReflectiveOperationException {
        Class<?> keyType = Class.forName("net.minecraft.client.KeyMapping");
        Class<?> categoryType = Class.forName("net.minecraft.client.KeyMapping$Category");
        Class<?> identifierType = load("net.minecraft.resources.Identifier", "net.minecraft.resources.ResourceLocation");
        Method identifierFactory = identifierType.getMethod("fromNamespaceAndPath", String.class, String.class);
        Object identifier = identifierFactory.invoke(null, "batterycraft", "controls");
        Method categoryFactory = categoryType.getMethod("register", identifierType);
        Object category = categoryFactory.invoke(null, identifier);

        Class<?> inputType = Class.forName("com.mojang.blaze3d.platform.InputConstants$Type");
        Object keysym = enumConstant(inputType, "KEYSYM");
        Constructor<?> constructor = keyType.getConstructor(String.class, inputType, int.class, categoryType);
        cycle = constructor.newInstance("key.batterycraft.cycle_profile", keysym, 66, category);
        config = constructor.newInstance("key.batterycraft.open_config", keysym, 79, category);
        Class<?> helper = Class.forName("net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper");
        invokeRegistration(helper, "registerKeyMapping", cycle);
        invokeRegistration(helper, "registerKeyMapping", config);
        return true;
    }

    private static void invokeRegistration(Class<?> helper, String name, Object mapping) throws ReflectiveOperationException {
        for (Method method : helper.getMethods()) {
            if (method.getName().equals(name) && Modifier.isStatic(method.getModifiers()) && method.getParameterCount() == 1) {
                method.invoke(null, mapping);
                return;
            }
        }
        throw new NoSuchMethodException(name);
    }

    private static boolean consume(Object mapping) {
        if (mapping == null) return false;
        for (String name : new String[]{"consumeClick", "wasPressed", "method_1436"}) {
            try {
                Method method = mapping.getClass().getMethod(name);
                return Boolean.TRUE.equals(method.invoke(mapping));
            } catch (ReflectiveOperationException ignored) { }
        }
        return false;
    }

    private static Class<?> load(String... names) throws ClassNotFoundException {
        for (String name : names) {
            try { return Class.forName(name); } catch (ClassNotFoundException ignored) { }
        }
        throw new ClassNotFoundException(names[0]);
    }

    private static Object enumConstant(Class<?> type, String name) throws ReflectiveOperationException {
        if (type.isEnum()) {
            for (Object value : type.getEnumConstants()) if (((Enum<?>) value).name().equals(name)) return value;
        }
        Field field = type.getField(name);
        return field.get(null);
    }
}
