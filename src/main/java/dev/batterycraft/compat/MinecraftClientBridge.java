package dev.batterycraft.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class MinecraftClientBridge {
    private static final String[] CLIENT_CLASSES = {
            "net.minecraft.class_310", "net.minecraft.client.Minecraft", "net.minecraft.client.MinecraftClient"
    };

    public Object client() {
        for (String className : CLIENT_CLASSES) {
            try {
                Class<?> type = Class.forName(className);
                for (String name : new String[]{"getInstance", "method_1551"}) {
                    try {
                        Method method = type.getDeclaredMethod(name);
                        if (Modifier.isStatic(method.getModifiers())) {
                            method.setAccessible(true);
                            return method.invoke(null);
                        }
                    } catch (ReflectiveOperationException ignored) { }
                }
            } catch (ClassNotFoundException ignored) { }
        }
        return null;
    }

    public void message(String text, boolean actionBar) {
        Object client = client();
        if (client == null) return;
        Runnable action = () -> messageOnClientThread(client, text, actionBar);
        try {
            Method execute = findMethod(client.getClass(), new String[]{"execute", "method_18859"}, Runnable.class);
            if (execute != null) execute.invoke(client, action); else action.run();
        } catch (ReflectiveOperationException ignored) { }
    }

    private void messageOnClientThread(Object client, String text, boolean actionBar) {
        try {
            Object player = readField(client, new String[]{"player", "field_1724"});
            if (player == null) return;
            Object component = literal(text);
            if (component == null) return;
            for (Method method : player.getClass().getMethods()) {
                String name = method.getName();
                if (!(name.equals("displayClientMessage") || name.equals("sendMessage") || name.equals("method_7353"))) continue;
                if (method.getParameterCount() == 2 && method.getParameterTypes()[1] == boolean.class) {
                    method.invoke(player, component, actionBar);
                    return;
                }
            }
        } catch (ReflectiveOperationException ignored) { }
    }

    private Object literal(String text) {
        for (String className : new String[]{"net.minecraft.class_2561", "net.minecraft.network.chat.Component", "net.minecraft.text.Text"}) {
            try {
                Class<?> type = Class.forName(className);
                for (String name : new String[]{"literal", "method_43470"}) {
                    try {
                        Method method = type.getMethod(name, String.class);
                        return method.invoke(null, text);
                    } catch (ReflectiveOperationException ignored) { }
                }
            } catch (ClassNotFoundException ignored) { }
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

    private static Method findMethod(Class<?> type, String[] names, Class<?>... params) {
        for (String name : names) {
            try { return type.getMethod(name, params); } catch (NoSuchMethodException ignored) { }
        }
        return null;
    }
}
