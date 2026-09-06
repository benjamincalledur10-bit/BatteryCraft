package dev.batterycraft.ui;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StatusMessageTimerTest {
    @Test void waitsFullIntervalAndDoesNotRepeatOrCatchUp() {
        StatusMessageTimer timer = new StatusMessageTimer();
        assertFalse(timer.due(0, true, 900));
        assertFalse(timer.due(899_000_000_000L, true, 900));
        assertTrue(timer.due(900_000_000_000L, true, 900));
        assertFalse(timer.due(901_000_000_000L, true, 900));
        assertTrue(timer.due(9_000_000_000_000L, true, 900));
        assertFalse(timer.due(9_001_000_000_000L, true, 900));
    }
    @Test void changesAndReenablingStartNewInterval() {
        StatusMessageTimer timer = new StatusMessageTimer();
        assertFalse(timer.due(0, true, 900));
        assertFalse(timer.due(900_000_000_000L, true, 1800));
        assertTrue(timer.due(2_700_000_000_000L, true, 1800));
        assertFalse(timer.due(3_000_000_000_000L, false, 1800));
        assertFalse(timer.due(6_000_000_000_000L, true, 1800));
        timer.reset();
        assertFalse(timer.due(9_000_000_000_000L, true, 1800));
    }
}
