package dev.batterycraft.ui;

import dev.batterycraft.profile.PowerProfile;

import java.util.Locale;

public final class LocalizedText {
    private static final boolean SPANISH = Locale.getDefault().getLanguage().equalsIgnoreCase("es");

    private LocalizedText() { }

    public static String value(String english, String spanish) {
        return SPANISH ? spanish : english;
    }

    public static String profile(PowerProfile profile) {
        if (profile == null) return value("Starting", "Iniciando");
        return switch (profile) {
            case PLUGGED_IN -> value("Plugged in", "Conectado");
            case BATTERY -> value("Battery saver", "Ahorro");
            case LOW_BATTERY -> value("Intensive saver", "Ahorro intenso");
            case CRITICAL_BATTERY -> value("Emergency", "Emergencia");
        };
    }
}
