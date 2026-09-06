package dev.batterycraft.compat.sodium;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Function;

/** Keeps the optional API independent of Minecraft's intermediary/named namespace. */
final class SodiumReflection {
    private SodiumReflection() { }

    static Class<?> type(String... names) {
        for (String name : names) {
            try { return Class.forName(name); } catch (ClassNotFoundException ignored) { }
        }
        throw new IllegalStateException("Missing optional API class: " + names[0]);
    }

    static Object call(Object target, String name, Object... args) {
        Class<?> type = target instanceof Class<?> c ? c : target.getClass();
        for (Method method : type.getMethods()) {
            if (!method.getName().equals(name) || method.getParameterCount() != args.length) continue;
            Class<?>[] parameters = method.getParameterTypes();
            boolean matches = true;
            for (int i = 0; i < args.length; i++) {
                Class<?> expected = parameters[i] == int.class ? Integer.class
                        : parameters[i] == boolean.class ? Boolean.class : parameters[i];
                if (args[i] != null && !expected.isInstance(args[i])) matches = false;
            }
            if (!matches) continue;
            try {
                method.setAccessible(true);
                return method.invoke(target instanceof Class<?> ? null : target, args);
            } catch (ReflectiveOperationException error) {
                throw new IllegalStateException("Sodium config API call failed: " + name, error);
            }
        }
        throw new IllegalStateException("Missing Sodium config API method: " + type.getName() + "." + name);
    }

    static Object callback(Class<?> type, Function<Object[], Object> action) {
        return Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (proxy, method, args) -> {
            if (method.getDeclaringClass() == Object.class) {
                return switch (method.getName()) {
                    case "equals" -> proxy == args[0];
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "toString" -> "BatteryCraft " + type.getSimpleName();
                    default -> null;
                };
            }
            return action.apply(args == null ? new Object[0] : args);
        });
    }

    static Object id(String path) {
        Class<?> type = type("net.minecraft.class_2960", "net.minecraft.resources.Identifier",
                "net.minecraft.resources.ResourceLocation", "net.minecraft.util.Identifier");
        return factory(type, new String[]{"method_60654", "parse", "of"}, "batterycraft:" + path);
    }

    static Object text(String value) {
        return factory(type("net.minecraft.class_2561", "net.minecraft.network.chat.Component", "net.minecraft.text.Text"),
                new String[]{"method_43470", "literal"}, value);
    }

    static Object translated(String key) {
        return factory(type("net.minecraft.class_2561", "net.minecraft.network.chat.Component", "net.minecraft.text.Text"),
                new String[]{"method_43471", "translatable"}, "batterycraft.options." + key);
    }

    private static Object factory(Class<?> type, String[] names, String value) {
        for (String name : names) {
            try { return type.getMethod(name, String.class).invoke(null, value); }
            catch (NoSuchMethodException ignored) { }
            catch (ReflectiveOperationException error) { throw new IllegalStateException(error); }
        }
        throw new IllegalStateException("Missing Minecraft text/identifier factory: " + type.getName());
    }
}
