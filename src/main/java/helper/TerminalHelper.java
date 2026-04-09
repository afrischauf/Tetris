package helper;


import config.Constants;
import logic.GarbagePieceHandler;
import com.google.common.base.Strings;
import screens.AsciiPanel;


/**
 * This Utility Class is used for the Terminal to display the tetris logo and Boxes
 */
public class TerminalHelper {

    /**
     * This String Array saves the tetris Logo
     */
    private static final String[] tetrisLogo = (
            " _________  ________  _________  _______     _____   ______  \n" +
                    "|  _   _  ||_   __  ||  _   _  ||_   __ \\   |_   _|.' ____ \\ \n" +
                    "|_/ | | \\_|  | |_ \\_||_/ | | \\_|  | |__) |    | |  | (___ \\_|\n" +
                    "    | |      |  _| _     | |      |  __ /     | |   _.____`. \n" +
                    "   _| |_    _| |__/ |   _| |_    _| |  \\ \\_  _| |_ | \\____) |\n" +
                    "  |_____|  |________|  |_____|  |____| |___||_____| \\______.'\n").split("\n");


    /**
     * Writes the Tetris logo to the given AsciiPanel terminal.
     *
     * @param terminal the AsciiPanel terminal
     */
    public static void writeTetrisLogo(AsciiPanel terminal) {
        for (int i = 0; i < tetrisLogo.length; i++) {
            terminal.writeCenter(tetrisLogo[i], i + 1);
        }
    }

    /**
     * Writes a box at the specified position in the AsciiPanel terminal.
     *
     * @param terminal The AsciiPanel terminal to write the box on.
     * @param x        The x-coordinate of the top-left corner of the box.
     * @param y        The y-coordinate of the top-left corner of the box.
     * @param width    The width of the box.
     * @param height   The height of the box.
     */
    public static void writeBoxAt(AsciiPanel terminal, int x, int y, int width, int height) {
        char down = (char) 186;
        char topright = (char) 187;
        char bottomright = (char) 188;
        char bottomleft = (char) 200;
        char topleft = (char) 201;
        char across = (char) 205;
        String topLines = topleft + Strings.repeat(String.valueOf(across), width - 2) + topright;
        String boxMiddleLines = down + Strings.repeat(" ", width - 2) + down;
        String bottomLines = bottomleft + Strings.repeat(String.valueOf(across), width - 2) + bottomright;
        terminal.write(topLines, x, y++);
        for (int i = 0; i < height - 2; i++) {
            terminal.write(boxMiddleLines, x, y++);
        }
        terminal.write(bottomLines, x, y);
    }

    /**
     * Displays whether the Player has received Garbage or not.
     *
     * @param terminal            The AsciiPanel terminal to write the garbage on.
     * @param x                   The x-coordinate of the top-left corner of the first character of the garbage line.
     * @param y                   The y-coordinate of the top-left corner of the first character of the garbage line.
     * @param height              The height of the garbage line.
     * @param garbagePieceHandler The handler that determines if a specific character in the garbage line should be garbage or wall color.
     */
    public static void writeGarbageLine(AsciiPanel terminal, int x, int y, int height, GarbagePieceHandler garbagePieceHandler) {
        for (int i = height - 2; i >= 0; i--) {
            terminal.write("#", x, y++, garbagePieceHandler.shouldBeGarbageIndicator(i) ? Constants.garbageColor : Constants.wallColor);
        }
    }
}
