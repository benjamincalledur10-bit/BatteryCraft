package dev.batterycraft.compat;

import dev.batterycraft.profile.PowerProfile;
import dev.batterycraft.profile.ProfileSettings;
import net.minecraft.class_310;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinecraftSettingsAdapterTest {
    @Test
    void appliesAndRestoresIntermediaryMinecraftOptions() {
        MinecraftSettingsAdapter adapter = new MinecraftSettingsAdapter();
        class_310 client = class_310.method_1551();
        reset(client);

        ProfileSettings critical = new ProfileSettings(30, 6, 5, 2, false, false, 0, 0.50);
        assertTrue(adapter.apply(PowerProfile.CRITICAL_BATTERY, critical, true));
        assertEquals(30, client.field_1690.field_1909.method_41753());
        assertEquals(6, client.field_1690.field_1870.method_41753());
        assertEquals(5, client.field_1690.field_34959.method_41753());
        assertEquals(net.minecraft.class_315.Particle.MINIMAL, client.field_1690.field_1882.method_41753());
        assertEquals(false, client.field_1690.field_1888.method_41753());
        assertEquals(0.50, client.field_1690.field_24214.method_41753());

        assertTrue(adapter.apply(PowerProfile.PLUGGED_IN, null, true));
        assertEquals(144, client.field_1690.field_1909.method_41753());
        assertEquals(16, client.field_1690.field_1870.method_41753());
        assertEquals(12, client.field_1690.field_34959.method_41753());
        assertEquals(net.minecraft.class_315.Particle.ALL, client.field_1690.field_1882.method_41753());
        assertEquals(true, client.field_1690.field_1888.method_41753());
        assertEquals(1.0, client.field_1690.field_24214.method_41753());
    }

    @Test
    void restoresOriginalSettingsAfterAdapterRestart(@TempDir Path directory) {
        Path recovery = directory.resolve("batterycraft-recovery.properties");
        class_310 client = class_310.method_1551();
        reset(client);
        client.field_1690.field_1909.method_41748(165);

        MinecraftSettingsAdapter first = new MinecraftSettingsAdapter();
        first.configureRecovery(recovery);
        ProfileSettings battery = new ProfileSettings(60, 10, 8, 1, true, true, 2, 0.80);
        assertTrue(first.apply(PowerProfile.BATTERY, battery, false));
        assertEquals(60, client.field_1690.field_1909.method_41753());

        MinecraftSettingsAdapter restarted = new MinecraftSettingsAdapter();
        restarted.configureRecovery(recovery);
        assertTrue(restarted.hasPendingRecovery());
        assertTrue(restarted.apply(PowerProfile.PLUGGED_IN, null, false));
        assertEquals(165, client.field_1690.field_1909.method_41753());
    }

    @Test
    void preservesLighterVisualSettingsAndCanReleaseFpsCap() {
        class_310 client = class_310.method_1551();
        reset(client);
        client.field_1690.field_1870.method_41748(4);
        client.field_1690.field_1882.method_41748(net.minecraft.class_315.Particle.MINIMAL);
        client.field_1690.field_1814.method_41748(net.minecraft.class_315.Cloud.OFF);
        MinecraftSettingsAdapter adapter = new MinecraftSettingsAdapter();
        adapter.apply(PowerProfile.BATTERY, new ProfileSettings(120, 8, 5, 1, false, false, 0, 0.65), true);
        assertEquals(4, client.field_1690.field_1870.method_41753());
        assertEquals(120, client.field_1690.field_1909.method_41753());
        assertEquals(net.minecraft.class_315.Particle.MINIMAL, client.field_1690.field_1882.method_41753());
        assertEquals(net.minecraft.class_315.Cloud.OFF, client.field_1690.field_1814.method_41753());
        adapter.apply(PowerProfile.BATTERY, new ProfileSettings(0, 8, 5, 1, false, false, 0, 0.65), true);
        assertEquals(144, client.field_1690.field_1909.method_41753());
        adapter.apply(PowerProfile.PLUGGED_IN, null, true);
        assertEquals(4, client.field_1690.field_1870.method_41753());
    }

    private static final ProfileSettings BATTERY = new ProfileSettings(120, 8, 5, 1, false, false, 0, 0.65);
    private static final ProfileSettings CRITICAL = new ProfileSettings(30, 4, 3, 2, false, false, 0, 0.50);

    @Test
    void keepsLowerFpsUnlessPlayerExplicitlyAllowsRaisingIt() {
        class_310 client = class_310.method_1551();
        reset(client);
        client.field_1690.field_1909.method_41748(60);
        MinecraftSettingsAdapter adapter = new MinecraftSettingsAdapter();
        adapter.apply(PowerProfile.BATTERY, BATTERY, true);
        assertEquals(60, client.field_1690.field_1909.method_41753());
        adapter.apply(PowerProfile.CRITICAL_BATTERY, CRITICAL, true);
        assertEquals(30, client.field_1690.field_1909.method_41753());
        adapter.apply(PowerProfile.BATTERY, BATTERY, true);
        assertEquals(60, client.field_1690.field_1909.method_41753());
        adapter.apply(PowerProfile.BATTERY, BATTERY, true, true);
        assertEquals(120, client.field_1690.field_1909.method_41753());
        adapter.apply(PowerProfile.BATTERY, BATTERY, true, false);
        assertEquals(60, client.field_1690.field_1909.method_41753());
        adapter.apply(PowerProfile.PLUGGED_IN, null, true);
        assertEquals(60, client.field_1690.field_1909.method_41753());
    }

    @Test
    void preservesManualEditsAcrossProfilesAndReconnectThenStartsNewSession() {
        class_310 client = class_310.method_1551();
        reset(client);
        MinecraftSettingsAdapter adapter = new MinecraftSettingsAdapter();
        adapter.apply(PowerProfile.BATTERY, BATTERY, true);
        client.field_1690.field_1909.method_41748(75);
        client.field_1690.field_1870.method_41748(12);
        client.field_1690.field_1882.method_41748(net.minecraft.class_315.Particle.ALL);
        client.field_1690.field_1814.method_41748(net.minecraft.class_315.Cloud.FAST);
        client.field_1690.field_1888.method_41748(true);
        client.field_1690.field_24214.method_41748(0.90);
        adapter.apply(PowerProfile.CRITICAL_BATTERY, CRITICAL, true);
        assertEquals(75, client.field_1690.field_1909.method_41753());
        assertEquals(12, client.field_1690.field_1870.method_41753());
        assertEquals(3, client.field_1690.field_34959.method_41753());
        adapter.apply(PowerProfile.PLUGGED_IN, null, true);
        assertEquals(75, client.field_1690.field_1909.method_41753());
        assertEquals(12, client.field_1690.field_1870.method_41753());
        assertEquals(12, client.field_1690.field_34959.method_41753());
        assertEquals(net.minecraft.class_315.Particle.ALL, client.field_1690.field_1882.method_41753());
        assertEquals(net.minecraft.class_315.Cloud.FAST, client.field_1690.field_1814.method_41753());
        assertEquals(true, client.field_1690.field_1888.method_41753());
        assertEquals(0.90, client.field_1690.field_24214.method_41753());
        adapter.apply(PowerProfile.CRITICAL_BATTERY, CRITICAL, true);
        assertEquals(30, client.field_1690.field_1909.method_41753());
        adapter.apply(PowerProfile.PLUGGED_IN, null, true);
        assertEquals(75, client.field_1690.field_1909.method_41753());
    }

    @Test
    void zeroFpsDoesNotOverwriteManualCap() {
        class_310 client = class_310.method_1551();
        reset(client);
        MinecraftSettingsAdapter adapter = new MinecraftSettingsAdapter();
        adapter.apply(PowerProfile.BATTERY, BATTERY, true);
        client.field_1690.field_1909.method_41748(50);
        adapter.apply(PowerProfile.BATTERY, new ProfileSettings(0, 8, 5, 1, false, false, 0, 0.65), true);
        assertEquals(50, client.field_1690.field_1909.method_41753());
        adapter.apply(PowerProfile.PLUGGED_IN, null, true);
        assertEquals(50, client.field_1690.field_1909.method_41753());
    }

    @Test
    void detectsEditsMadeImmediatelyBeforeCrash(@TempDir Path directory) {
        class_310 client = class_310.method_1551();
        reset(client);
        Path recovery = directory.resolve("recovery.properties");
        MinecraftSettingsAdapter first = new MinecraftSettingsAdapter();
        first.configureRecovery(recovery);
        first.apply(PowerProfile.BATTERY, BATTERY, true);
        client.field_1690.field_1909.method_41748(55);
        client.field_1690.field_1870.method_41748(7);
        MinecraftSettingsAdapter restarted = new MinecraftSettingsAdapter();
        restarted.configureRecovery(recovery);
        restarted.apply(PowerProfile.PLUGGED_IN, null, true);
        assertEquals(55, client.field_1690.field_1909.method_41753());
        assertEquals(7, client.field_1690.field_1870.method_41753());
        assertEquals(12, client.field_1690.field_34959.method_41753());
        assertEquals(false, restarted.hasPendingRecovery());
    }

    @Test
    void persistsManualOwnershipAcrossCrashAndFurtherTransitions(@TempDir Path directory) {
        class_310 client = class_310.method_1551();
        reset(client);
        Path recovery = directory.resolve("recovery.properties");
        MinecraftSettingsAdapter first = new MinecraftSettingsAdapter();
        first.configureRecovery(recovery);
        first.apply(PowerProfile.BATTERY, BATTERY, true);
        client.field_1690.field_1870.method_41748(10);
        first.apply(PowerProfile.CRITICAL_BATTERY, CRITICAL, true);
        // Even returning to a former mod value must not revoke manual ownership.
        client.field_1690.field_1870.method_41748(8);
        MinecraftSettingsAdapter restarted = new MinecraftSettingsAdapter();
        restarted.configureRecovery(recovery);
        restarted.apply(PowerProfile.CRITICAL_BATTERY, CRITICAL, true);
        assertEquals(8, client.field_1690.field_1870.method_41753());
        assertEquals(30, client.field_1690.field_1909.method_41753());
        restarted.apply(PowerProfile.PLUGGED_IN, null, true);
        assertEquals(8, client.field_1690.field_1870.method_41753());
        assertEquals(144, client.field_1690.field_1909.method_41753());
        assertEquals(false, restarted.hasPendingRecovery());
    }

    @Test
    void recoversLegacyBeta4Snapshot(@TempDir Path directory) {
        class_310 client = class_310.method_1551();
        reset(client);
        Path recovery = directory.resolve("recovery.properties");
        new RecoveryStore(recovery).save(java.util.Map.of("maxFps", "integer:165", "viewDistance", "integer:20"));
        MinecraftSettingsAdapter adapter = new MinecraftSettingsAdapter();
        adapter.configureRecovery(recovery);
        adapter.apply(PowerProfile.PLUGGED_IN, null, false);
        assertEquals(165, client.field_1690.field_1909.method_41753());
        assertEquals(20, client.field_1690.field_1870.method_41753());
        assertEquals(false, adapter.hasPendingRecovery());
    }

    public static final class NamedOption<T> {
        private T value;
        NamedOption(T value) { this.value = value; }
        public T get() { return value; }
        public void set(T value) { this.value = value; }
    }

    public static final class NamedOptions {
        public final NamedOption<Integer> framerateLimit = new NamedOption<>(60);
        public final NamedOption<Integer> renderDistance = new NamedOption<>(16);
        public final NamedOption<Integer> simulationDistance = new NamedOption<>(12);
        public final NamedOption<net.minecraft.class_315.Particle> particles = new NamedOption<>(net.minecraft.class_315.Particle.ALL);
        public final NamedOption<Boolean> entityShadows = new NamedOption<>(true);
        public final NamedOption<Integer> biomeBlendRadius = new NamedOption<>(5);
        public final NamedOption<net.minecraft.class_315.Cloud> cloudStatus = new NamedOption<>(net.minecraft.class_315.Cloud.FANCY);
        public final NamedOption<Double> entityDistanceScaling = new NamedOption<>(1.0);
    }

    public static final class NamedClient {
        public final NamedOptions options = new NamedOptions();
    }

    @Test
    void appliesSameProtectionWithNamedMinecraftOptions(@TempDir Path directory) throws Exception {
        NamedClient client = new NamedClient();
        MinecraftSettingsAdapter adapter = new MinecraftSettingsAdapter();
        adapter.configureRecovery(directory.resolve("recovery.properties"));
        java.lang.reflect.Method update = MinecraftSettingsAdapter.class.getDeclaredMethod(
                "updateOnClientThread", Object.class, PowerProfile.class, ProfileSettings.class, boolean.class, boolean.class);
        update.setAccessible(true);
        update.invoke(adapter, client, PowerProfile.BATTERY, BATTERY, true, false);
        assertEquals(60, client.options.framerateLimit.get());
        assertEquals(8, client.options.renderDistance.get());
        client.options.renderDistance.set(11);
        update.invoke(adapter, client, PowerProfile.CRITICAL_BATTERY, CRITICAL, true, false);
        assertEquals(11, client.options.renderDistance.get());
        assertEquals(30, client.options.framerateLimit.get());
        update.invoke(adapter, client, PowerProfile.PLUGGED_IN, null, true, false);
        assertEquals(11, client.options.renderDistance.get());
        assertEquals(60, client.options.framerateLimit.get());
        assertEquals(12, client.options.simulationDistance.get());
        assertEquals(false, adapter.hasPendingRecovery());
    }

    private static void reset(class_310 client) {
        client.field_1690.field_1909.method_41748(144);
        client.field_1690.field_1870.method_41748(16);
        client.field_1690.field_34959.method_41748(12);
        client.field_1690.field_1882.method_41748(net.minecraft.class_315.Particle.ALL);
        client.field_1690.field_1888.method_41748(true);
        client.field_1690.field_1878.method_41748(5);
        client.field_1690.field_1814.method_41748(net.minecraft.class_315.Cloud.FANCY);
        client.field_1690.field_24214.method_41748(1.0);
    }
}
