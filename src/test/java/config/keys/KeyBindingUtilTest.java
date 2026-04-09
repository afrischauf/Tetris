package config.keys;

import org.junit.jupiter.api.Test;

import java.awt.Canvas;
import java.awt.event.KeyEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KeyBindingUtilTest {

    @Test
    void normalizesConfiguredAliasesToCanonicalNames() {
        assertEquals("space", KeyBindingUtil.normalizeConfiguredKey("spacebar"));
        assertEquals("enter", KeyBindingUtil.normalizeConfiguredKey("return"));
        assertEquals("control", KeyBindingUtil.normalizeConfiguredKey("ctrl"));
        assertEquals("meta", KeyBindingUtil.normalizeConfiguredKey("command"));
        assertEquals("alt", KeyBindingUtil.normalizeConfiguredKey("option"));
    }

    @Test
    void normalizesKeyEventsAcrossPlatforms() {
        Canvas source = new Canvas();

        KeyEvent enter = new KeyEvent(source, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ENTER, '\n');
        KeyEvent meta = new KeyEvent(source, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_META, KeyEvent.CHAR_UNDEFINED);
        KeyEvent alpha = new KeyEvent(source, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_A, 'a');

        assertEquals("enter", KeyBindingUtil.normalizeKeyEvent(enter));
        assertEquals("meta", KeyBindingUtil.normalizeKeyEvent(meta));
        assertEquals("a", KeyBindingUtil.normalizeKeyEvent(alpha));
    }
}
