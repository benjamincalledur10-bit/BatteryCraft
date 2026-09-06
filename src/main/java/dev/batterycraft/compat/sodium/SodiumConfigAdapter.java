package dev.batterycraft.compat.sodium;

import dev.batterycraft.BatteryCraftClient;
import net.fabricmc.loader.api.LanguageAdapter;
import net.fabricmc.loader.api.LanguageAdapterException;
import net.fabricmc.loader.api.ModContainer;
import java.lang.reflect.Proxy;

/** Fabric loads this optional entrypoint only when Sodium requests its config API. */
public final class SodiumConfigAdapter implements LanguageAdapter {
    @Override
    public <T> T create(ModContainer mod, String value, Class<T> type) throws LanguageAdapterException {
        if (!type.getName().equals("net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint")) {
            throw new LanguageAdapterException("Unsupported Sodium entrypoint: " + type.getName());
        }
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (proxy, method, args) -> {
            return switch (method.getName()) {
                case "registerConfigLate" -> {
                    BatteryCraftClient.registerSodiumOptions(args[0]);
                    yield null;
                }
                case "registerConfigEarly" -> null;
                case "equals" -> proxy == args[0];
                case "hashCode" -> System.identityHashCode(proxy);
                case "toString" -> "BatteryCraft Sodium config entrypoint";
                default -> throw new UnsupportedOperationException(method.getName());
            };
        }));
    }
}
