package config.keys;

import helper.OsUtil;

import config.LdataParser;
import screens.AsciiPanel;
import screens.PlayOfflineScreen;
import screens.PlayOnlineScreen;
import screens.Screen;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Enum KeyMenuConfig contains enums that correspond to user key inputs
 * and their specific mapping to an action to be executed upon user key input.
 * It provides an interface 'Screen.execute' to execute a specific action based on key input
 * after using a configuration file (tty-tetris.conf) to initialize the key mapping.
 * <p>
 * KeyMenuConfig can handle in-game key inputs for moving up/down in menu, select, enter,
 * refresh and createLobby. Its usage is confined within the contexts of
 * PlayOnlineScreen and PlayOfflineScreen only.
 */
public enum KeyMenuConfig {
    /**
     * Represents the Down navigation key input in the Menu.
     */
    MENUDOWN() {
        @Override
        public Screen execute(Screen screen, AsciiPanel terminal) {
            return screen.respondToUserInput(new KeyEvent(terminal, 0, 0L, 0, KeyEvent.VK_DOWN), terminal);
        }
    },
    /**
     * Represents the Up navigation key input in the Menu.
     */
    MENUUP() {
        @Override
        public Screen execute(Screen screen, AsciiPanel terminal) {
            return screen.respondToUserInput(new KeyEvent(terminal, 0, 0L, 0, KeyEvent.VK_UP), terminal);
        }
    },
    /**
     * Represents the Select key input in the Menu.
     */
    SELECT() {
        @Override
        public Screen execute(Screen screen, AsciiPanel terminal) {
            return screen.respondToUserInput(new KeyEvent(terminal, 0, 0L, 0, KeyEvent.VK_SPACE), terminal);
        }
    },
    /**
     * Represents the Enter key input in the Menu.
     */
    ENTER() {
        @Override
        public Screen execute(Screen screen, AsciiPanel terminal) {
            return screen.respondToUserInput(new KeyEvent(terminal, 0, 0L, 0, KeyEvent.VK_ENTER), terminal);
        }
    },
    /**
     * Represents the Refresh key input in the Menu.
     */
    REFRESH() {
        @Override
        public Screen execute(Screen screen, AsciiPanel terminal) {
            return screen.respondToUserInput(new KeyEvent(terminal, 0, 0L, 0, KeyEvent.VK_R), terminal);
        }
    },
    /**
     * Represents the Create Lobby key input in the Menu.
     */
    CREATELOBBY() {
        @Override
        public Screen execute(Screen screen, AsciiPanel terminal) {
            return screen.respondToUserInput(new KeyEvent(terminal, 0, 0L, 0, KeyEvent.VK_C), terminal);
        }
    },
    /**
     * Represents the Offline Play key input in the Menu.
     */
    PLAYOFFLINE() {
        @Override
        public Screen execute(Screen screen, AsciiPanel terminal) {
            return screen.respondToUserInput(new KeyEvent(terminal, 0, 0L, 0, KeyEvent.VK_O), terminal);
        }
    };


    /**
     * This Hashmap is used for mapping between the Enums and the KeyEvents and is filled by {@link KeyMenuConfig#initializeKeymap()}
     */
    private static final Map<String, String> menuMap = new HashMap<>();

    /**
     * To be implemented by each key input to execute an action based on the key press.
     *
     * @param screen   The screen context in which the key input was detected.
     * @param terminal The terminal from which the key input was received.
     * @return A screen context after the key input action has been executed.
     */

    public abstract Screen execute(Screen screen, AsciiPanel terminal);

    /**
     * Initializes the keymap with user specified key bindings from a
     * configuration file using LdataParser.
     */
    public static void initializeKeymap() {
        menuMap.clear();
        Map<String, Object> config = LdataParser.loadFrom(OsUtil.getConfigFile("tty-tetris.conf"));
        Map<String, Object> _menuMap = (Map) ((Map) config.get("keymap")).get("menuMap");
        for (String action : _menuMap.keySet()) {
            List<String> keyStrokes = (List) _menuMap.get(action);
            for (String keyStroke : keyStrokes) {
                String normalizedKey = KeyBindingUtil.normalizeConfiguredKey(keyStroke);
                if (normalizedKey != null) {
                    menuMap.put(normalizedKey, action);
                }
            }
        }
    }

    /**
     * Executes an action linked to the key press if screen is not of given types.
     *
     * @param key The user pressed key input passed as a key event.
     * @param screen The screen context in which the key input was detected.
     * @param terminal The terminal from which the key input was received.
     * @return A screen context after the key input action has been executed.
     */

    public static Screen execute(KeyEvent key, Screen screen, AsciiPanel terminal) {
        if (!(screen instanceof PlayOnlineScreen || screen instanceof PlayOfflineScreen)) {
            String normalizedKey = keyStrokeToString(key);
            if (menuMap.get(normalizedKey) == null) {
                return screen;
            }
            return KeyMenuConfig.valueOf(menuMap.get(normalizedKey).toUpperCase()).execute(screen, terminal);
        }
        return screen;
    }
    /**
     * Utility function to convert a KeyEvent to a lowercase string representation.
     *
     * @param key The KeyEvent to be converted.
     * @return key's text in lowercase form as a String.
     */

    private static String keyStrokeToString(KeyEvent key) {
        return KeyBindingUtil.normalizeKeyEvent(key);
    }
}
