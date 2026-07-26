# Changelog

## 1.0.0-beta.3 - 2026-07-26

- Added crash-safe recovery for the player's original Minecraft settings.
- Added macOS battery-time estimates to the optional HUD indicator.
- Added configurable profile-transition delays to prevent rapid switching.
- Added a configurable HUD refresh interval.
- Added a bilingual English/Spanish configuration interface.
- Added a diagnostics panel for macOS support, CPU architecture, Sodium, thermal state, battery data, and pending recovery.
- Added safer atomic configuration recovery storage with automatic cleanup after restoration.
- Expanded automated coverage for recovery, transition stabilization, configuration migration, and macOS battery parsing.

## 1.0.0-beta.2 - 2026-07-20

- Replaced macOS-unfriendly F8/F9 polling with native Minecraft key mappings.
- Added a BatteryCraft category under the standard key-bindings screen.
- Added freely reassignable controls for cycling profiles and opening configuration.
- Changed defaults to B and O so MacBook media keys are no longer required.
- Added Spanish and English control names.
- Fabric API is now required for reliable cross-version key registration.

## 1.0.0-beta.1 - 2026-07-20

- Added a visual configuration screen opened with F9.
- Added F8 manual profile switching.
- Added configurable notifications and status indicator.
- Added fully editable power profiles.
- Added Sodium detection and optional integration.
- Added experimental macOS thermal-warning handling.
- Added local session statistics.
- Added the official BatteryCraft icon.
- Preserved separate artifacts for Minecraft 1.21.x, 26.1.x and 26.2.x.

## 0.0.1-alpha - 2026-07-20

- Initial public alpha.
- Added macOS battery and charging-state detection.
- Added four automatic power profiles.
- Added restoration of Minecraft settings when external power returns.
- Added separate Fabric artifacts for Minecraft 1.21.x, 26.1.x, and 26.2.x.
