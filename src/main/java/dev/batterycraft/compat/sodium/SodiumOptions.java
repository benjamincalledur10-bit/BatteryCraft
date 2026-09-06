package dev.batterycraft.compat.sodium;

import dev.batterycraft.config.BatteryCraftConfig;
import dev.batterycraft.profile.ManualMode;
import dev.batterycraft.profile.PowerProfile;
import dev.batterycraft.profile.ProfileSettings;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import static dev.batterycraft.compat.sodium.SodiumReflection.*;

/** Native Sodium pages. Sodium owns editing, cancellation, search, and Apply. */
public final class SodiumOptions {
    private final BatteryCraftConfig config;
    private final Runnable save;
    private final Map<String, Runnable> pending = new LinkedHashMap<>();
    private Object builder;
    private Object storage;
    private Integer minutes;
    private Integer seconds;
    private Integer lowThreshold;
    private Integer criticalThreshold;

    public SodiumOptions(BatteryCraftConfig config, Runnable save) {
        this.config = config;
        this.save = save;
    }

    public void register(Object builder) {
        this.builder = builder;
        storage = callback(type("net.caffeinemc.mods.sodium.api.config.StorageEventHandler"), args -> {
            flush();
            return null;
        });
        Object mod = call(builder, "registerOwnModOptions");
        call(mod, "setNonTintedIcon", id("icon.png"));
        Object general = page(mod, "general");
        bool(general, "enabled", true, config::enabled, config::enabled);
        enumOption(general, "mode", ManualMode.class, ManualMode.AUTOMATIC, config::manualMode, config::manualMode);
        bool(general, "sodium", true, config::sodiumIntegration, config::sodiumIntegration);
        bool(general, "thermal", false, config::thermalMode, config::thermalMode);
        integer(general, "low_threshold", 2, 99, 30, config::lowThreshold, value -> lowThreshold = value, "%");
        integer(general, "critical_threshold", 1, 98, 15, config::criticalThreshold, value -> criticalThreshold = value, "%");
        integer(general, "poll", 5, 300, 10, config::pollSeconds, config::pollSeconds, "s");
        integer(general, "transition", 0, 60, 8, config::transitionDelaySeconds, config::transitionDelaySeconds, "s");

        Object reminders = page(mod, "reminders");
        bool(reminders, "notifications", true, config::notifications, config::notifications);
        bool(reminders, "status", false, config::hudIndicator, config::hudIndicator);
        integer(reminders, "minutes", 0, Math.max(1440, config.hudIntervalSeconds() / 60), 15,
                () -> config.hudIntervalSeconds() / 60, value -> minutes = value, "min");
        integer(reminders, "seconds", 0, 59, 0,
                () -> config.hudIntervalSeconds() % 60, value -> seconds = value, "s");

        profile(mod, "battery", PowerProfile.BATTERY);
        profile(mod, "low_battery", PowerProfile.LOW_BATTERY);
        profile(mod, "critical_battery", PowerProfile.CRITICAL_BATTERY);
    }

    private Object page(Object mod, String key) {
        Object page = call(builder, "createOptionPage");
        call(page, "setName", translated(key));
        Object group = call(builder, "createOptionGroup");
        call(page, "addOptionGroup", group);
        call(mod, "addPage", page);
        return group;
    }

    private <T> void common(Object group, Object option, String key, T fallback, Supplier<T> get, Consumer<T> set) {
        String label = key.contains("/") ? key.substring(key.indexOf('/') + 1) : key;
        call(option, "setName", translated(label));
        call(option, "setTooltip", translated(label + ".tooltip"));
        call(option, "setStorageHandler", storage);
        call(option, "setDefaultValue", fallback);
        // Stage all bindings until Sodium has finished applying the entire option batch.
        Consumer<T> queue = value -> pending.put(key, () -> set.accept(value));
        call(option, "setBinding", queue, get);
        call(group, "addOption", option);
    }

    private void bool(Object group, String key, boolean fallback, Supplier<Boolean> get, Consumer<Boolean> set) {
        Object option = call(builder, "createBooleanOption", id(key));
        common(group, option, key, fallback, get, set);
    }

    private void integer(Object group, String key, int min, int max, int fallback,
                         Supplier<Integer> get, Consumer<Integer> set, String suffix) {
        Object option = call(builder, "createIntegerOption", id(key));
        call(option, "setRange", min, max, 1);
        Object formatter = callback(type("net.caffeinemc.mods.sodium.api.config.option.ControlValueFormatter"),
                args -> key.endsWith("/fps") && (int) args[0] == 0 ? translated("original_fps") : text(args[0] + " " + suffix));
        call(option, "setValueFormatter", formatter);
        common(group, option, key, fallback, get, set);
    }

    private <E extends Enum<E>> void enumOption(Object group, String key, Class<E> type, E fallback,
                                               Supplier<E> get, Consumer<E> set) {
        Object option = call(builder, "createEnumOption", id(key), type);
        Function<E, Object> formatter = value -> translated("mode." + value.name().toLowerCase(java.util.Locale.ROOT));
        call(option, "setElementNameProvider", formatter);
        common(group, option, key, fallback, get, set);
    }

    private void profile(Object mod, String name, PowerProfile profile) {
        Object group = page(mod, name);
        ProfileSettings defaults = switch (profile) {
            case BATTERY -> new ProfileSettings(120, 8, 5, 1, false, false, 0, 0.65);
            case LOW_BATTERY -> new ProfileSettings(90, 6, 4, 2, false, false, 0, 0.50);
            default -> new ProfileSettings(60, 4, 3, 2, false, false, 0, 0.50);
        };
        profileInt(group, name, profile, "fps", 0, 260, defaults.maxFps(), ProfileSettings::maxFps, 0, "FPS");
        profileInt(group, name, profile, "render", 2, 64, defaults.renderDistance(), ProfileSettings::renderDistance, 1, "chunks");
        profileInt(group, name, profile, "simulation", 2, 32, defaults.simulationDistance(), ProfileSettings::simulationDistance, 2, "chunks");
        profileInt(group, name, profile, "particles", 0, 2, defaults.particleLevel(), ProfileSettings::particleLevel, 3, "");
        bool(group, name + "/clouds", defaults.clouds(), () -> config.profile(profile).clouds(), value -> update(profile, 4, value));
        bool(group, name + "/shadows", defaults.entityShadows(), () -> config.profile(profile).entityShadows(), value -> update(profile, 5, value));
        profileInt(group, name, profile, "biome", 0, 7, defaults.biomeBlendRadius(), ProfileSettings::biomeBlendRadius, 6, "");
        integer(group, name + "/entities", 25, 100, (int) Math.round(defaults.entityDistanceScale() * 100),
                () -> (int) Math.round(config.profile(profile).entityDistanceScale() * 100), value -> update(profile, 7, value / 100.0), "%");
    }

    private void profileInt(Object group, String name, PowerProfile profile, String key, int min, int max,
                            int fallback, Function<ProfileSettings, Integer> get, int field, String suffix) {
        integer(group, name + "/" + key, min, max, fallback, () -> get.apply(config.profile(profile)),
                value -> update(profile, field, value), suffix);
    }

    private void update(PowerProfile profile, int field, Object value) {
        ProfileSettings p = config.profile(profile);
        config.profile(profile, new ProfileSettings(field == 0 ? (int) value : p.maxFps(),
                field == 1 ? (int) value : p.renderDistance(), field == 2 ? (int) value : p.simulationDistance(),
                field == 3 ? (int) value : p.particleLevel(), field == 4 ? (boolean) value : p.clouds(),
                field == 5 ? (boolean) value : p.entityShadows(), field == 6 ? (int) value : p.biomeBlendRadius(),
                field == 7 ? (double) value : p.entityDistanceScale()));
    }

    private void flush() {
        if (pending.isEmpty()) return;
        pending.values().forEach(Runnable::run);
        if (lowThreshold != null) config.lowThreshold(lowThreshold);
        config.criticalThreshold(criticalThreshold != null ? criticalThreshold : config.criticalThreshold());
        if (minutes != null || seconds != null) {
            long interval = (minutes != null ? minutes : config.hudIntervalSeconds() / 60) * 60L
                    + (seconds != null ? seconds : config.hudIntervalSeconds() % 60);
            config.hudIntervalSeconds((int) Math.min(Integer.MAX_VALUE, interval));
        }
        save.run();
        pending.clear();
        minutes = null;
        seconds = null;
        lowThreshold = null;
        criticalThreshold = null;
    }
}
