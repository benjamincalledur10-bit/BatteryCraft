package dev.batterycraft.profile;

public record ProfileSettings(
        int maxFps,
        int renderDistance,
        int simulationDistance,
        int particleLevel,
        boolean clouds,
        boolean entityShadows,
        int biomeBlendRadius,
        double entityDistanceScale
) { }
