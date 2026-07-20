package dev.batterycraft.compat;

import dev.batterycraft.profile.PowerProfile;
import dev.batterycraft.profile.ProfileSettings;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public final class MinecraftSettingsAdapter {
    private static final String[] CLIENT_CLASSES = {
            "net.minecraft.class_310", "net.minecraft.client.Minecraft", "net.minecraft.client.MinecraftClient"
    };
    private final Map<String, Object> originalValues = new HashMap<>();

    public synchronized boolean apply(PowerProfile profile, ProfileSettings profileSettings, boolean sodiumLoaded) {
        try {
            Object client = findClient();
            if (client == null) return false;
            Runnable update = () -> updateOnClientThread(client, profile, profileSettings, sodiumLoaded);
            Method execute = findMethod(client.getClass(), new String[]{"execute", "method_18859"}, Runnable.class);
            if (execute != null) execute.invoke(client, update); else update.run();
            return true;
        } catch (ReflectiveOperationException | RuntimeException error) {
            System.err.println("[BatteryCraft] Could not apply profile: " + error.getMessage());
            return false;
        }
    }

    private void updateOnClientThread(Object client, PowerProfile profile, ProfileSettings profileSettings, boolean sodiumLoaded) {
        try {
            Object options = readField(client, new String[]{"options", "field_1690"});
            if (options == null) return;
            if (profile == PowerProfile.PLUGGED_IN) {
                restore(options);
                return;
            }
            setOption(options, "maxFps", new String[]{"framerateLimit", "maxFps", "field_1909"}, profileSettings.maxFps());
            setOption(options, "viewDistance", new String[]{"renderDistance", "viewDistance", "field_1870"}, profileSettings.renderDistance());
            setOption(options, "simulationDistance", new String[]{"simulationDistance", "field_34959"}, profileSettings.simulationDistance());
            setParticleOption(options, profileSettings.particleLevel());
            setOption(options, "entityShadows", new String[]{"entityShadows", "field_1888"}, profileSettings.entityShadows());
            setOption(options, "biomeBlend", new String[]{"biomeBlendRadius", "field_1878"}, profileSettings.biomeBlendRadius());
            setCloudOption(options, profileSettings.clouds());
            if (sodiumLoaded) {
                setOption(options, "entityDistance", new String[]{"entityDistanceScaling", "entityDistanceScale", "field_24214"}, profileSettings.entityDistanceScale());
            }
        } catch (ReflectiveOperationException error) {
            System.err.println("[BatteryCraft] Settings update failed: " + error.getMessage());
        }
    }

    private void setCloudOption(Object options, boolean enabled) throws ReflectiveOperationException {
        Object option = readField(options, new String[]{"cloudRenderMode", "cloudStatus", "field_1814"});
        if (option == null) return;
        Object current = getOptionValue(option);
        originalValues.putIfAbsent("clouds", current);
        if (current instanceof Enum<?> enumValue) {
            Object[] constants = enumValue.getDeclaringClass().getEnumConstants();
            int index = enabled ? Math.min(1, constants.length - 1) : 0;
            setOptionValue(option, constants[index]);
        }
    }

    private void setParticleOption(Object options, int level) throws ReflectiveOperationException {
        Object option = readField(options, new String[]{"particles", "field_1882"});
        if (option == null) return;
        Object current = getOptionValue(option);
        originalValues.putIfAbsent("particles", current);
        if (current instanceof Enum<?> enumValue) {
            Object[] constants = enumValue.getDeclaringClass().getEnumConstants();
            setOptionValue(option, constants[Math.min(level, constants.length - 1)]);
        }
    }

    private void setOption(Object options, String key, String[] names, Object value) throws ReflectiveOperationException {
        Object option = readField(options, names);
        if (option == null) return;
        originalValues.putIfAbsent(key, getOptionValue(option));
        setOptionValue(option, value);
    }

    private void restore(Object options) throws ReflectiveOperationException {
        restoreOption(options, "maxFps", new String[]{"framerateLimit", "maxFps", "field_1909"});
        restoreOption(options, "viewDistance", new String[]{"renderDistance", "viewDistance", "field_1870"});
        restoreOption(options, "simulationDistance", new String[]{"simulationDistance", "field_34959"});
        restoreOption(options, "particles", new String[]{"particles", "field_1882"});
        restoreOption(options, "entityShadows", new String[]{"entityShadows", "field_1888"});
        restoreOption(options, "biomeBlend", new String[]{"biomeBlendRadius", "field_1878"});
        restoreOption(options, "clouds", new String[]{"cloudRenderMode", "cloudStatus", "field_1814"});
        restoreOption(options, "entityDistance", new String[]{"entityDistanceScaling", "entityDistanceScale", "field_24214"});
        originalValues.clear();
    }

    private void restoreOption(Object options, String key, String[] names) throws ReflectiveOperationException {
        if (!originalValues.containsKey(key)) return;
        Object option = readField(options, names);
        if (option != null) setOptionValue(option, originalValues.get(key));
    }

    private Object findClient() throws ReflectiveOperationException {
        for (String className : CLIENT_CLASSES) {
            try {
                Class<?> type = Class.forName(className);
                Method getter = findStaticGetter(type, new String[]{"getInstance", "method_1551"});
                if (getter != null) return getter.invoke(null);
            } catch (ClassNotFoundException ignored) { }
        }
        return null;
    }

    private static Method findStaticGetter(Class<?> type, String[] names) {
        for (String name : names) {
            try {
                Method method = type.getDeclaredMethod(name);
                if (Modifier.isStatic(method.getModifiers())) {
                    method.setAccessible(true);
                    return method;
                }
            } catch (NoSuchMethodException ignored) { }
        }
        return null;
    }

    private static Object readField(Object target, String[] names) throws IllegalAccessException {
        for (String name : names) {
            Class<?> type = target.getClass();
            while (type != null) {
                try {
                    Field field = type.getDeclaredField(name);
                    field.setAccessible(true);
                    return field.get(target);
                } catch (NoSuchFieldException ignored) { type = type.getSuperclass(); }
            }
        }
        return null;
    }

    private static Object getOptionValue(Object option) throws ReflectiveOperationException {
        Method getter = findMethod(option.getClass(), new String[]{"get", "getValue", "method_41753"});
        if (getter == null) throw new NoSuchMethodException("option getter");
        return getter.invoke(option);
    }

    private static void setOptionValue(Object option, Object value) throws ReflectiveOperationException {
        Method setter = findCompatibleSetter(option.getClass(), value, new String[]{"set", "setValue", "method_41748"});
        if (setter == null) throw new NoSuchMethodException("option setter");
        setter.invoke(option, value);
    }

    private static Method findMethod(Class<?> type, String[] names, Class<?>... parameters) {
        for (String name : names) {
            try {
                Method method = type.getMethod(name, parameters);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) { }
        }
        return null;
    }

    private static Method findCompatibleSetter(Class<?> type, Object value, String[] names) {
        for (Method method : type.getMethods()) {
            boolean nameMatches = false;
            for (String name : names) if (method.getName().equals(name)) nameMatches = true;
            if (!nameMatches || method.getParameterCount() != 1) continue;
            if (value == null || wrap(method.getParameterTypes()[0]).isInstance(value)) {
                method.setAccessible(true);
                return method;
            }
        }
        return null;
    }

    private static Class<?> wrap(Class<?> type) {
        if (!type.isPrimitive()) return type;
        if (type == int.class) return Integer.class;
        if (type == boolean.class) return Boolean.class;
        if (type == double.class) return Double.class;
        if (type == float.class) return Float.class;
        if (type == long.class) return Long.class;
        return type;
    }
}
