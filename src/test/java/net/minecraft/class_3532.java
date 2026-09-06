package net.minecraft;

/** MathHelper clamp used by Sodium's config color themes. */
public final class class_3532 {
    public static float method_15363(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
