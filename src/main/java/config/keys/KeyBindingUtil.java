package config.keys;

import java.awt.event.KeyEvent;
import java.util.Locale;

/**
 * Normalizes key names from config files and runtime key events to stable identifiers.
 */
public final class KeyBindingUtil {

    private KeyBindingUtil() {
    }

    /**
     * Converts a configured key binding string into its canonical representation.
     *
     * @param configuredKey the key string from config
     * @return canonical key identifier or null when the binding is empty
     */
    public static String normalizeConfiguredKey(String configuredKey) {
        if (configuredKey == null) {
            return null;
        }
        String normalized = configuredKey.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() == 1 && normalized.charAt(0) != ' ') {
            return normalized;
        }
        switch (normalized) {
            case " ":
            case "space":
            case "spacebar":
                return "space";
            case "\\n":
            case "\n":
            case "\r":
            case "enter":
            case "return":
                return "enter";
            case "ctrl":
            case "control":
                return "control";
            case "cmd":
            case "command":
            case "meta":
                return "meta";
            case "alt":
            case "altgraph":
            case "option":
                return "alt";
            case "esc":
            case "escape":
                return "escape";
            case "del":
            case "delete":
                return "delete";
            case "backspace":
            case "back_space":
                return "backspace";
            case "left":
            case "right":
            case "up":
            case "down":
            case "shift":
            case "tab":
                return normalized;
            default:
                return normalized;
        }
    }

    /**
     * Converts a runtime key event into its canonical representation.
     *
     * @param keyEvent the incoming key event
     * @return canonical key identifier
     */
    public static String normalizeKeyEvent(KeyEvent keyEvent) {
        int keyCode = keyEvent.getKeyCode();
        if (keyCode >= KeyEvent.VK_A && keyCode <= KeyEvent.VK_Z) {
            return String.valueOf((char) ('a' + (keyCode - KeyEvent.VK_A)));
        }
        if (keyCode >= KeyEvent.VK_0 && keyCode <= KeyEvent.VK_9) {
            return String.valueOf((char) ('0' + (keyCode - KeyEvent.VK_0)));
        }
        switch (keyCode) {
            case KeyEvent.VK_LEFT:
                return "left";
            case KeyEvent.VK_RIGHT:
                return "right";
            case KeyEvent.VK_UP:
                return "up";
            case KeyEvent.VK_DOWN:
                return "down";
            case KeyEvent.VK_ENTER:
                return "enter";
            case KeyEvent.VK_SPACE:
                return "space";
            case KeyEvent.VK_SHIFT:
                return "shift";
            case KeyEvent.VK_CONTROL:
                return "control";
            case KeyEvent.VK_META:
                return "meta";
            case KeyEvent.VK_ALT:
            case KeyEvent.VK_ALT_GRAPH:
                return "alt";
            case KeyEvent.VK_TAB:
                return "tab";
            case KeyEvent.VK_ESCAPE:
                return "escape";
            case KeyEvent.VK_BACK_SPACE:
                return "backspace";
            case KeyEvent.VK_DELETE:
                return "delete";
            default:
                break;
        }

        char keyChar = keyEvent.getKeyChar();
        if (keyChar != KeyEvent.CHAR_UNDEFINED) {
            return normalizeConfiguredKey(String.valueOf(keyChar));
        }
        return normalizeConfiguredKey(KeyEvent.getKeyText(keyCode));
    }
}
