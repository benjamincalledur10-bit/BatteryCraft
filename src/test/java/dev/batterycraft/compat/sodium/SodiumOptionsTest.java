package dev.batterycraft.compat.sodium;

import dev.batterycraft.config.BatteryCraftConfig;
import dev.batterycraft.profile.PowerProfile;
import net.minecraft.class_2960;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import static org.junit.jupiter.api.Assertions.*;
import static dev.batterycraft.compat.sodium.SodiumReflection.*;

/** Tests cover the published API contracts and Sodium's real eager builder implementation. */
class SodiumOptionsTest {
    @Test void buildsPagesWithRealSodiumImplementation(@TempDir Path directory) throws Exception {
        BatteryCraftConfig config = BatteryCraftConfig.load(directory.resolve("batterycraft.json"));
        Class<?> metadataType = type("net.caffeinemc.mods.sodium.client.config.ConfigManager$ModMetadata");
        Object metadata = metadataType.getConstructor(String.class, String.class).newInstance("BatteryCraft", "1.0.0-beta.4");
        java.util.function.Function<String, Object> metadataProvider = id -> metadata;
        Object builder = type("net.caffeinemc.mods.sodium.client.config.builder.ConfigBuilderImpl")
                .getConstructor(java.util.function.Function.class, String.class).newInstance(metadataProvider, "batterycraft");
        new SodiumOptions(config, () -> {}).register(builder);
        Collection<?> mods = (Collection<?>) call(builder, "build");
        assertEquals(1, mods.size());
        Object mod = mods.iterator().next();
        Collection<?> pages = (Collection<?>) call(mod, "pages");
        assertEquals(5, pages.size());
        int optionCount = 0;
        for (Object page : pages) {
            Collection<?> groups = (Collection<?>) call(page, "groups");
            assertFalse(groups.isEmpty());
            for (Object group : groups) {
                Collection<?> options = (Collection<?>) call(group, "options");
                assertFalse(options.isEmpty());
                optionCount += options.size();
            }
        }
        assertEquals(36, optionCount, "All options must survive Sodium's eager snapshots");
    }

    private static final String API = "net.caffeinemc.mods.sodium.api.config.structure.";

    @Test void registersNativePagesWithoutChangingConfiguration(@TempDir Path directory) {
        BatteryCraftConfig config = BatteryCraftConfig.load(directory.resolve("batterycraft.json"));
        Registry registry = new Registry();
        int[] saves = {0};
        new SodiumOptions(config, () -> saves[0]++).register(registry.proxy("ConfigBuilder"));
        assertEquals(5, registry.pages);
        assertEquals(36, registry.options.size());
        assertEquals(0, saves[0]);
        assertFalse(config.hudIndicator());
        assertEquals(900, config.hudIntervalSeconds());
        for (Option option : registry.options.values()) {
            assertNotNull(option.setter);
            assertNotNull(option.getter);
            assertNotNull(option.storage);
            assertNotNull(option.name);
            assertNotNull(option.tooltip);
            assertNotNull(option.fallback);
            if (option.integer) {
                assertNotNull(option.formatter);
                assertNotNull(option.range);
                int value = (int) option.getter.get();
                assertTrue(value >= option.range[0] && value <= option.range[1]);
                assertNotNull(call(option.formatter, "format", value));
            }
        }
    }

    @Test void appliesWholeBatchAndReloadsSavedValues(@TempDir Path directory) {
        BatteryCraftConfig config = BatteryCraftConfig.load(directory.resolve("batterycraft.json"));
        Registry registry = new Registry();
        int[] saves = {0};
        new SodiumOptions(config, () -> {
            saves[0]++;
            try { config.save(); } catch (Exception e) { throw new AssertionError(e); }
        }).register(registry.proxy("ConfigBuilder"));
        registry.set("minutes", 30);
        registry.set("seconds", 7);
        registry.set("status", true);
        registry.set("battery/fps", 0);
        registry.set("battery/render", 6);
        assertFalse(config.hudIndicator(), "Bindings are staged until afterSave");
        registry.flush();
        assertEquals(1, saves[0], "The shared storage handler flushes once");
        BatteryCraftConfig reloaded = BatteryCraftConfig.load(config.path());
        assertTrue(reloaded.hudIndicator());
        assertEquals(1807, reloaded.hudIntervalSeconds());
        assertEquals(0, reloaded.profile(PowerProfile.BATTERY).maxFps());
        assertEquals(6, reloaded.profile(PowerProfile.BATTERY).renderDistance());
        assertEquals(5, reloaded.profile(PowerProfile.BATTERY).simulationDistance());
        assertEquals(30, registry.options.get("minutes").getter.get());
        registry.set("minutes", 15);
        registry.set("seconds", 0);
        registry.flush();
        assertEquals(900, config.hudIntervalSeconds());
    }

    @Test void clampsCombinedIntervalAndDependentThresholds(@TempDir Path directory) {
        BatteryCraftConfig config = BatteryCraftConfig.load(directory.resolve("batterycraft.json"));
        Registry registry = new Registry();
        new SodiumOptions(config, () -> {}).register(registry.proxy("ConfigBuilder"));
        registry.set("minutes", 0);
        registry.set("seconds", 0);
        registry.set("low_threshold", 10);
        registry.set("critical_threshold", 40);
        registry.flush();
        assertEquals(5, config.hudIntervalSeconds());
        assertEquals(9, config.criticalThreshold());
        registry.set("critical_threshold", 60);
        registry.set("low_threshold", 80);
        registry.flush();
        assertEquals(60, config.criticalThreshold(), "Dependent thresholds apply independently of binding order");
    }

    @Test void preservesLongCustomIntervalsDuringRegistration(@TempDir Path directory) {
        BatteryCraftConfig config = BatteryCraftConfig.load(directory.resolve("batterycraft.json"));
        config.hudIntervalSeconds(Integer.MAX_VALUE);
        Registry registry = new Registry();
        new SodiumOptions(config, () -> {}).register(registry.proxy("ConfigBuilder"));
        assertEquals(Integer.MAX_VALUE, config.hudIntervalSeconds());
        assertTrue(registry.options.get("minutes").range[1] >= Integer.MAX_VALUE / 60);
    }

    private static final class Option {
        Consumer<Object> setter;
        Supplier<?> getter;
        Object storage, name, tooltip, fallback, formatter;
        boolean integer;
        int[] range;
    }

    private static final class Registry {
        final Map<String, Option> options = new LinkedHashMap<>();
        int pages;
        Object proxy(String name) {
            Class<?> contract = type(API + name);
            return Proxy.newProxyInstance(contract.getClassLoader(), new Class<?>[]{contract}, (proxy, method, args) -> {
                return switch (method.getName()) {
                    case "registerOwnModOptions" -> proxy("ModOptionsBuilder");
                    case "createOptionPage" -> { pages++; yield proxy("OptionPageBuilder"); }
                    case "createOptionGroup" -> proxy("OptionGroupBuilder");
                    case "createBooleanOption", "createIntegerOption", "createEnumOption" -> {
                        Option option = new Option();
                        option.integer = method.getName().equals("createIntegerOption");
                        options.put(((class_2960) args[0]).value().substring("batterycraft:".length()), option);
                        yield optionProxy(method.getReturnType(), option);
                    }
                    case "toString" -> name;
                    default -> proxy;
                };
            });
        }
        @SuppressWarnings("unchecked")
        Object optionProxy(Class<?> contract, Option option) {
            return Proxy.newProxyInstance(contract.getClassLoader(), new Class<?>[]{contract}, (proxy, method, args) -> {
                switch (method.getName()) {
                    case "setBinding" -> { option.setter = (Consumer<Object>) args[0]; option.getter = (Supplier<?>) args[1]; }
                    case "setStorageHandler" -> option.storage = args[0];
                    case "setName" -> option.name = args[0];
                    case "setTooltip" -> option.tooltip = args[0];
                    case "setDefaultValue" -> option.fallback = args[0];
                    case "setValueFormatter" -> option.formatter = args[0];
                    case "setRange" -> option.range = new int[]{(int) args[0], (int) args[1], (int) args[2]};
                }
                return proxy;
            });
        }
        void set(String key, Object value) { options.get(key).setter.accept(value); }
        void flush() { options.values().forEach(option -> call(option.storage, "afterSave")); }
    }
}
