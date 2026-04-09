package screens;


import java.awt.event.KeyEvent;

/**
 * The Screen interface represents a screen that can be displayed on an AsciiPanel terminal.
 * It provides methods for displaying the screen, responding to user input, and determining
 * if input is finished or if the cursor is inside an input field.
 */
public interface Screen {

    /**
     * Displays the output of the screen on the given AsciiPanel terminal.
     *
     * @param terminal the AsciiPanel object representing the terminal
     */
    void displayOutput(AsciiPanel terminal);

    /**
     * Responds to the user's input by performing various actions depending on the key pressed,
     * and returns the corresponding screen to be displayed.
     *
     * @param key      the KeyEvent object representing the key pressed by the user
     * @param terminal the AsciiPanel object representing the terminal
     * @return the Screen object to be displayed
     */
    Screen respondToUserInput(KeyEvent key, AsciiPanel terminal);

    /**
     * The finishInput method is used to determine if the user has finished providing input.
     * It is typically used in conjunction with the handleKeyDown method in the Screen interface.
     *
     * @return true if the input is finished, false otherwise
     */
    boolean finishInput();

    /**
     * Releases resources owned by the screen before it is replaced.
     */
    default void close() {
    }

    /**
     * Determines whether the cursor is currently inside an input field.
     *
     * @return true if the cursor is inside an input field, false otherwise
     */
    default boolean isInsideInputField() {
        return false;
    }

}
