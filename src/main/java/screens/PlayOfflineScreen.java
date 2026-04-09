package screens;

import config.Constants;
import helper.TerminalHelper;
import config.keys.KeyPlay;
import logic.TetrisField;

import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The `PlayOfflineScreen` class represents the screen where the game is played offline.
 * It implements the `Screen` and `Runnable` interfaces.
 */
public class PlayOfflineScreen implements Screen, Runnable {

    /**
     * The `field` variable represents a Tetris field.
     * It is a final variable of type TetrisField.
     */
    private final TetrisField field;

    /**
     * The `startTime` variable represents the time when the screen was initialized.
     * It is a private final variable of type long.
     * <p>
     * The `startTime` variable is used to calculate the duration of gameplay.
     */
    private final long startTime;
    /**
     * The `initScreen` variable represents whether the screen has been displayed or if it is the first time it has been displayed
     */
    public boolean initScreen = true;
    /**
     * The `loseScreen` variable represents whether the game has reached the lose state or not.
     * It is a public boolean variable.
     * <p>
     * When `loseScreen` is true, the game has reached the lose state. When `loseScreen` is set to false,
     * it means the game is not in the lose state.
     */
    public boolean loseScreen = false;

    public static int executeCounter;

    public static long firstExecution;
    private final ScheduledExecutorService keyExecutor;

    /**
     * The `pressedKeys` variable represents the set of keys that are currently being pressed.
     * It is a private final variable of type Set<KeyPlay>.
     * <p>
     * The `pressedKeys` set is used to keep track of the keys that are being pressed during gameplay.
     * Different keys can be added or removed from the set using the addKey() and removeKey() methods respectively.
     * The run() method iterates over the pressedKeys set and executes the corresponding actions for each key.
     */
    private final Set<KeyPlay> pressedKeys = new TreeSet<>();
    private boolean isFirstTime = true;

    /**
     * Constructs a PlayOfflineScreen object with the given AsciiPanel terminal.
     *
     * @param terminal the AsciiPanel terminal used for rendering the screen.
     */

    public PlayOfflineScreen(AsciiPanel terminal) {
        field = new TetrisField(1, this, (terminal.getWidthInCharacters() - 12) / 2, 16);
        startTime = System.currentTimeMillis();
        keyExecutor = Executors.newSingleThreadScheduledExecutor();
        keyExecutor.scheduleAtFixedRate(this, 0, Constants.KEYLISTENERTIMER, TimeUnit.MILLISECONDS);
        executeCounter = 0;
    }


    /**
     * Clears the given AsciiPanel terminal, writes a Tetris logo using TerminalHelper,
     * and prints the Tetris field using the field object.
     *
     * @param terminal the AsciiPanel terminal used for rendering the screen.
     */
    @Override
    public void displayOutput(AsciiPanel terminal) {
        if (loseScreen) {
            MainClass.aClass.setScreen(new LoseScreen(field.getLevel(), field.getScore(), System.currentTimeMillis() - startTime));
            MainClass.aClass.repaint();
            return;
        }
        terminal.clear();
        TerminalHelper.writeTetrisLogo(terminal);
        initScreen = false;
        field.printTetrisField(terminal);
    }

    /**
     * Responds to the user's input by performing various actions depending on the key pressed,
     * and returns the corresponding screen to be displayed.
     *
     * @param key the KeyEvent object representing
     */
    @Override
    public Screen respondToUserInput(KeyEvent key, AsciiPanel terminal) {
        return this;
    }

    /**
     * Finish the input and return a boolean value.
     *
     * @return a boolean value that indicates whether the input is finished or not
     */
    @Override
    public boolean finishInput() {
        return false;
    }


    /**
     * Adds the KeyEvent to the list of pressedKeys if it represents a valid in-game operation.
     *
     * @param keyEvent the KeyEvent object representing the key pressed by the user
     */
    public void addKey(KeyEvent keyEvent) {
        // First Press
        if (KeyPlay.getKey(keyEvent, false) != null) {
            pressedKeys.add(KeyPlay.getKey(keyEvent, false));
        }
    }

    /**
     * Removes the key from the list of pressedKeys if it represents a valid in-game operation.
     *
     * @param keyEvent the KeyEvent object representing the key pressed by the user
     */
    public void removeKey(KeyEvent keyEvent) {
        if (KeyPlay.getKey(keyEvent,false) != null)
            pressedKeys.remove(KeyPlay.getKey(keyEvent, true));
    }

    /**
     * The run method is an implementation of the Runnable interface. It executes the associated game operations for each pressed key.
     * It iterates over the pressedKeys list and calls the execute method on each KeyPlay enum value. The execute method performs
     * the operation on the given TetrisField object. After executing the operation, it increments the counter of the pressed key.
     */
    @Override
    public void run() {
        if (isFirstTime) {
            Thread.currentThread().setName("KeyListener in PlayOfflineScreen");
            isFirstTime = false;
            firstExecution = System.currentTimeMillis();
        }
        synchronized (pressedKeys) {
            for (KeyPlay pressedKey : pressedKeys) {
                pressedKey.execute(field);
                pressedKey.incrementCounter();
            }
        }
        executeCounter++;
    }

    @Override
    public void close() {
        keyExecutor.shutdownNow();
        field.shutdownThread();
        synchronized (pressedKeys) {
            pressedKeys.clear();
        }
        loseScreen = false;
        initScreen = true;
    }
}
