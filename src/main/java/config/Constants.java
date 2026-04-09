package config;

import java.awt.*;

/**
 * This Helper Class is used for storing different Constants that make it easier to change permanent Configurations for testing
 */
public class Constants {

    /**
     * Which Character the default block should have
     */
    public static final char BLOCK = 219;

    /**
     * Which Character a Block Chain should be
     */
    public static final String BLOCKCHAIN = "" + BLOCK + BLOCK + BLOCK + BLOCK;

    /**
     * Which Character should be the background of the Tetrisboard
     */
    public static final char BACKGROUND = 1;

    /**
     * Which Color the selected {@link components.Component} should be
     */
    public static final Color selectedColor = Color.GREEN;

    /**
     * Which Color the characters of a {@link components.Component} should be
     */
    public static final Color characterColor = Color.LIGHT_GRAY;

    /**
     * Which Color the characters of a selected {@link components.TextInput} should be
     */
    public static final Color inputColor = Color.CYAN;

    /**
     * Which Color the Background Color should be
     */
    public static final Color backgroundColor = Color.BLACK;

    /**
     * Which Color the wall Color should be
     */
    public static final Color wallColor = Color.WHITE;

    /**
     * Which Color the Garbage Indicator should be
     */
    public static final Color garbageColor = Color.MAGENTA;

    /**
     * Which Color the first Player in the Scoreboard should be displayed as
     */
    public static final Color firstPlayer = Color.YELLOW;

    /**
     * Which Color the second Player in the Scoreboard should be displayed as
     */
    public static final Color secondPlayer = Color.GRAY;

    /**
     * Which Color the third Player in the Scoreboard should be displayed as
     */
    public static final Color thirdPlayer = Color.ORANGE;

    /**
     * Which Color the Tetris Hold Piece gets once a Swap is locked. Also used for displaying which color Garbage Lines should be
     */
    public static final Color disabledColor = Color.GRAY;

    /**
     * Which Color Important Text, such as Level and Score are
     */
    public static final Color importantText = Color.YELLOW;

    /**
     * This Variable defines the delay in ms that the KeyListener Thread, which is run from the two PlayScreens, has
     */
    public static final int KEYLISTENERTIMER = 4;

    /**
     * This Variable defines the delay between each Rotation in milliseconds
     */
    public static final int ROTATIONDELAY = 250 / KEYLISTENERTIMER;


}
