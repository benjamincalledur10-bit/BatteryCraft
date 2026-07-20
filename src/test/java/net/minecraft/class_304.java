package net.minecraft;

public final class class_304 {
    public final String translationKey;
    public final int keyCode;
    public final String category;
    private int presses;

    public class_304(String translationKey, int keyCode, String category) {
        this.translationKey = translationKey;
        this.keyCode = keyCode;
        this.category = category;
    }

    public boolean method_1436() {
        if (presses == 0) return false;
        presses--;
        return true;
    }

    public void press() { presses++; }
}
