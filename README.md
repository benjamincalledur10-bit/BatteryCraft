# BatteryCraft

BatteryCraft is a lightweight, client-side Fabric mod that automatically reduces Minecraft's power usage when a MacBook is running on battery.

## 0.0.1-alpha

This first public alpha:

- reads battery percentage and charging state through macOS `pmset`;
- switches between plugged-in, battery, low-battery, and critical-battery profiles;
- adjusts FPS, render distance, simulation distance, and particles;
- restores the player's original values after reconnecting power;
- performs battery checks on a background daemon thread;
- does not require administrator privileges or an Internet connection.

Default thresholds are 30% for low battery and 15% for critical battery. Configuration is created at `config/batterycraft.json`.

## Downloads

- `batterycraft-0.0.1-alpha-mc1.21.jar`: Minecraft 1.21.x
- `batterycraft-0.0.1-alpha-mc26.1.jar`: Minecraft 26.1.x
- `batterycraft-0.0.1-alpha-mc26.2.jar`: Minecraft 26.2.x

Fabric API is not required. Minecraft 1.21.x needs Java 21; Minecraft 26.1.x and 26.2.x need Java 25.

> This is an alpha release. Back up your Minecraft configuration and report compatibility problems through GitHub Issues.

## Build

```bash
./gradlew clean releaseJars
```

The distributable files are written to `build/libs`.

## License

MIT
