package screens;

import config.Constants;
import helper.TerminalHelper;

import logic.HighScore;
import lombok.extern.slf4j.Slf4j;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import static helper.TerminalHelper.writeBoxAt;

/**
 * The StartScreen class represents the initial screen of the game.
 * It displays the logo, high scores, and options to start the game.
 * It also handles user input to navigate to different screens.
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@Slf4j
public class StartScreen implements Screen {
    /**
     * This variable represents a set of high scores.
     */
    public final Set<HighScore> scores;

    /**
     * The StartScreen class is responsible for initializing the start screen of the application.
     * It creates a new instance of StartScreen and sets up the necessary data for the screen.
     */
    public StartScreen() {
        scores = new TreeSet<>();
        readHighScores();
    }

    /**
     * Reads the high scores from a file and populates the scores list.
     * If the file does not exist, it creates a new file.
     * If an exception occurs while reading or creating the file, a RuntimeException is thrown.
     */
    private void readHighScores() {
        try {
            List<String> lines = Files.readAllLines(Paths.get("highscores.txt"));
            for (String line : lines) {
                String[] lineSplit = line.split(";");
                String name = lineSplit[0];
                int level = Integer.parseInt(lineSplit[1]);
                long score = Long.parseLong(lineSplit[2]);
                int timePassed = Integer.parseInt(lineSplit[3]);
                scores.add(new HighScore(name, level, score, String.format("%d:%d", TimeUnit.MILLISECONDS.toMinutes(timePassed), (TimeUnit.MILLISECONDS.toSeconds(timePassed) % 60))));
            }
        } catch (IOException e) {
            File file = new File("highscores.txt");
            try {
                file.createNewFile();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * Clears the terminal and displays the output for the Tetris game.
     *
     * @param terminal the AsciiPanel object representing the terminal
     */
    @Override
    public void displayOutput(AsciiPanel terminal) {
        terminal.clear();
        TerminalHelper.writeTetrisLogo(terminal);
        terminal.write("Press 'o' to play offline Tetris", 1, 12);
        terminal.writeCenter("-- press [enter] to start --", terminal.getHeightInCharacters() - 1);
        int maximum = terminal.getHeightInCharacters() - 30 > 1 ? 10 : terminal.getHeightInCharacters() - 22;
        if (maximum > -1) {
            writeBoxAt(terminal, terminal.getWidthInCharacters() / 2 - 20, 15, 41, maximum + 5);
            terminal.writeCenter("      Name      | Level | Score | Time ", 16);
            terminal.writeCenter("---------------------------------------", 17);
            int i = 0;
            for (HighScore score : scores) {
                if (i == maximum + 1) {
                    break;
                }
                switch (i) {
                    case 0:
                        terminal.writeCenter(score.toString(), 18 + i, Constants.firstPlayer);
                        break;
                    case 1:
                        terminal.writeCenter(score.toString(), 18 + i, Constants.secondPlayer);
                        break;
                    case 2:
                        terminal.writeCenter(score.toString(), 18 + i, Constants.thirdPlayer);
                        break;
                    default:
                        terminal.writeCenter(score.toString(), 18 + i);
                }
                i++;
            }
        }
    }

    /**
     * Responds to user input by checking the key pressed and returning the appropriate screen.
     *
     * @param key      the KeyEvent object representing the key pressed by the user
     * @param terminal the AsciiPanel object representing the terminal
     * @return the next screen to be displayed
     */
    @Override
    public Screen respondToUserInput(KeyEvent key, AsciiPanel terminal) {
        if (key.getKeyCode() == KeyEvent.VK_ENTER) {
            LoginScreen screen = new LoginScreen();
            screen.displayOutput(terminal);
            return screen;
        }
        if (key.getKeyChar() != KeyEvent.CHAR_UNDEFINED && Character.toLowerCase(key.getKeyChar()) == 'o') {
            PlayOfflineScreen screen = new PlayOfflineScreen(terminal);
            screen.displayOutput(terminal);
            return screen;
        }
        if (key.getKeyChar() != KeyEvent.CHAR_UNDEFINED && Character.toLowerCase(key.getKeyChar()) == 'e') {
            PlayOnlineScreen screen = new PlayOnlineScreen(terminal, false);
            screen.displayOutput(terminal);
            return screen;
        }

        return this;
    }

    /**
     * This method finishes the input process and sets the screen to a new LoginScreen instance.
     *
     * @return true if the input process is finished successfully, false otherwise.
     */
    @Override
    public boolean finishInput() {
        MainClass.aClass.setScreen(new LoginScreen());
        return true;
    }
}
