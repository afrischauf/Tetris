package logic;

import config.Constants;

import communication.MatchSendHelper;
import logic.pieces.Tetromino;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import com.google.common.base.Strings;
import screens.AsciiPanel;
import screens.PlayOfflineScreen;
import screens.PlayOnlineScreen;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static helper.TerminalHelper.*;


/**
 * The TetrisField class represents the playing field for the Tetris game. It contains methods
 * for adding grids to the field, swapping the hold piece, checking for cleared lines, and handling
 * garbage pieces. It also manages the active and hold pieces, score, level, and combo count.
 */
@Slf4j
@Getter
@Setter
public class TetrisField {

    /**
     * The Height of the Tetris Field
     */
    public static final int SCREEN_HEIGHT = 20;

    /**
     * The Width of the Tetris Field
     */
    public static final int SCREEN_WIDTH = 10;

    /**
     * On which X Coordinate to begin printing the Tetris Field
     */

    private final int startX;

    /**
     * On which Y Coordinate to begin printing the Tetris Field
     */
    private final int startY;

    /**
     * This defines how many lines have to be cleared to get to the next level
     */
    private static final int LINE_THRESHOLD = 10;

    /**
     * This 2D {@link Point} array stores the Tetris Field
     */
    private final Point[][] points = new Point[50][10];

    /**
     * A class variable that represents a random Tetromino generator.
     */
    private final RandomGenerator generator;

    /**
     * A private variable representing the play offline screen.
     */
    private PlayOfflineScreen offlineScreen;

    /**
     * Represents the online screen of a play session.
     * The online screen is used for online multiplayer gameplay.
     */
    private PlayOnlineScreen onlineScreen;

    /**
     * The Active {@link Tetromino} which is also the piece which is moved
     */
    private Tetromino activePiece;

    /**
     * The {@link Tetromino} which is currently being held
     */
    private Tetromino holdPiece;

    /**
     * The Helper Piece which is displayed at the bottom of the board to better display where the Tetromino will fall
     */
    private Tetromino helperPiece;

    /**
     * This Variable keeps track of the Score
     */
    private long score;

    /**
     * This Variable keeps track of the Level
     */
    private int level;

    /**
     * This Variable keeps track of how many lines have to be cleared to get to the next level
     */
    private int numberofLinesToClear;

    /**
     * This displays the Current Speed of a {@link TetrisField#gameTick()}
     */
    private int currentMillis = 1000;

    /**
     * This Variable stores if it is allowed to swap currently
     */
    private boolean allowSwap;

    /**
     * This Variable stores the combo counter which is important for Score Tracking and Garbage Calculation
     */
    private int combo = -1;

    /**
     * This Variable stores if the Tetris/ T-Spin is Back 2 Back and thus would send more lines to an Opponent
     */
    private boolean storeB2B;

    /**
     * This Variable stores if the last dropped Piece had a T-Spin in it. Currently only double T-Spins are tracked, and not single T-Spins nor triple T-Spins
     */
    private boolean isTspin;

    /**
     * This Executor executes {@link TetrisField#gameTick()} depending on {@link TetrisField#currentMillis}
     */
    private ScheduledExecutorService exec;

    /**
     * This Variable stores the next four Pieces so that they can be displayed as part of the output
     */
    private java.util.List<Tetromino> nextPieces;

    /**
     * The garbagePieceHandler variable is an instance of the GarbagePieceHandler class,
     * which is responsible for handling garbage pieces in a software system.
     * <p>
     * GarbagePieceHandler is a static variable, meaning it is associated with the class itself,
     * rather than with any specific instance of the class. This allows access to the same
     * garbagePieceHandler instance from anywhere in the code.
     * <p>
     * GarbagePieceHandler is used for managing and manipulating garbage pieces,
     * and provides various methods and functionality for this purpose.
     * <p>
     * This variable can be accessed directly as follows:
     * <p>
     * GarbagePieceHandler garbagePieceHandler = GarbagePieceHandler.garbagePieceHandler;
     * <p>
     * It is recommended to use this variable instead of creating new instances of GarbagePieceHandler,
     * as it ensures consistency and avoids unnecessary resource consumption.
     * <p>
     * To use GarbagePieceHandler, you can invoke its methods, such as:
     * <p>
     * garbagePieceHandler.methodName(parameters);
     * <p>
     * Please refer to the documentation of the GarbagePieceHandler class for more information on
     * the available methods and their usage.
     * <p>
     * Note: This documentation assumes that the GarbagePieceHandler class has been properly implemented
     * and is accessible from the current codebase.
     */
    public static GarbagePieceHandler garbagePieceHandler;

    /**
     * This Variable stores whether the current Tetris Game is a 1v1 or played offline
     */
    private final boolean isOnline;


    /**
     * Initializes a new instance of the TetrisField class.
     *
     * @param level  The level of the game.
     * @param screen The PlayOfflineScreen object.
     * @param startX The starting x-coordinate of the playfield.
     * @param startY The starting y-coordinate of the playfield.
     */
    public TetrisField(int level, PlayOfflineScreen screen, int startX, int startY) {
        this.startX = startX;
        this.startY = startY;
        for (Point[] point : points) {
            Arrays.fill(point, new Point(true, Constants.backgroundColor));
        }
        generator = new RandomGenerator(this);
        activePiece = generator.getNext();
        calculateNewHelperPiecePosition();
        nextPieces = generator.peek(4);
        holdPiece = null;
        allowSwap = true;
        score = 0;
        this.level = level;
        for (int i = 0; i < level - 1; i++) {
            currentMillis -= this.currentMillis / 10;
        }
        exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(this::gameTick, 0, currentMillis, TimeUnit.MILLISECONDS);
        numberofLinesToClear = LINE_THRESHOLD;
        this.offlineScreen = screen;
        garbagePieceHandler = new GarbagePieceHandler();
        this.isOnline = false;
    }

    /**
     * TetrisField is a class that represents the game field for a Tetris game.
     *
     * @param level  The level of the game.
     * @param screen The PlayOnlineScreen where the game is being played.
     * @param startX The starting X-coordinate for the game field.
     * @param startY The starting Y-coordinate for the game field.
     */
    public TetrisField(int level, PlayOnlineScreen screen, int startX, int startY) {
        this.startX = startX;
        this.startY = startY;
        for (Point[] point : points) {
            Arrays.fill(point, new Point(true, Constants.backgroundColor));
        }
        generator = new RandomGenerator(this);
        activePiece = generator.getNext();
        calculateNewHelperPiecePosition();
        nextPieces = generator.peek(4);
        holdPiece = null;
        allowSwap = true;
        score = 0;
        this.level = level;
        for (int i = 0; i < level - 1; i++) {
            currentMillis -= this.currentMillis / 10;
        }
        exec = Executors.newSingleThreadScheduledExecutor();
        isOnline = true;
        numberofLinesToClear = LINE_THRESHOLD;
        this.onlineScreen = screen;
        garbagePieceHandler = new GarbagePieceHandler();
    }


    /**
     * This Method is called once a piece has been dropped so that the piece can be added to the {@link TetrisField#points} array
     *
     * @param grid The grid which is added to the points array
     */
    public void addGrid(Grid grid) {
        isTspin();
        Boolean[][] gridPoints = grid.getSetPoints();
        for (int y = 0; y < gridPoints.length; y++) {
            for (int x = 0; x < gridPoints[y].length; x++) {
                if (gridPoints[y][x]) {
                    points[y + grid.y][x + grid.x] = new Point(false, grid.getColor());
                }
            }
        }
        checkForClearedLines();
    }

    /**
     * Swaps the current active piece with the hold piece, if swapping is allowed.
     * If a hold piece is already in place, it is swapped with the active piece. Otherwise,
     * the active piece is placed in the hold and a new active piece is generated.
     * The hold piece and the active piece are synchronized to ensure thread safety.
     * After swapping, the hold piece is reset to its original position,
     * the helper piece is set to the new active piece, and the allowSwap flag is set to false.
     * The color of the hold piece is changed to grey.
     */
    public void swapHold() {
        if (!allowSwap) {
            return;
        }
        if (holdPiece != null) {
            synchronized (holdPiece) {
                Tetromino temp = holdPiece;
                holdPiece = activePiece;
                activePiece = temp;
            }
        } else {
            synchronized (activePiece) {
                holdPiece = activePiece;
                activePiece = generator.getNext();
                nextPieces = generator.peek(4);
            }
        }
        synchronized (holdPiece) {
            holdPiece.resetPosition();
        }
        helperPiece = activePiece;
        calculateNewHelperPiecePosition();

        allowSwap = false;
        holdPiece.changeColorGrey();
    }

    /**
     * Receives Garbage pieces and updates the game grid accordingly.
     * The method retrieves ready Garbage pieces from the garbagePieceHandler.
     * For each Garbage piece, it randomly determines a position on the game grid.
     * It then shifts the existing points on the game grid by the number of lines in the Garbage piece.
     * Next, it updates the game grid with the new Garbage piece by setting the corresponding points to true and the rest to false.
     * Finally, it sends an update notification to match, indicating the number of lines and the position of the Garbage piece.
     */
    private void receiveGarbage() {
        List<GarbagePiece> garbages = garbagePieceHandler.getReadyGarbage();
        for (GarbagePiece garbage : garbages) {
            int garbagePosition = (int) (Math.random() * 9);
            int line = garbage.getLines();
            for (int y = line; y < 50; y++) {
                System.arraycopy(points[y], 0, points[y - line], 0, 10);
            }
            for (int y = 0; y < line; y++) {
                for (int x = 0; x < 10; x++) {
                    if (x == garbagePosition) {
                        points[y + 50 - line][x] = new Point(true, Constants.backgroundColor);
                    } else {
                        points[y + 50 - line][x] = new Point(false, Constants.disabledColor);
                    }
                }
            }
            MatchSendHelper.UPDATEGARBAGE.sendUpdate(line, garbagePosition);
        }
    }

    /**
     * Checks for cleared lines on the game board and performs necessary actions.
     * If a line is cleared, it clears the line, moves the rest of the lines down,
     * updates the score, and handles other game logic.
     */
    private void checkForClearedLines() {
        int numberOfClearedLines = 0;
        a:
        for (int y = 0; y < points.length; y++) {
            for (int x = 0; x < points[y].length; x++) {
                if (points[y][x].isFree()) {
                    continue a;
                }
                if (x + 1 == points[y].length) {
                    clearLine(y);
                    numberOfClearedLines++;
                    moveRestDown(y);
                }
            }
        }

        if (numberOfClearedLines != 0) {
            combo++;
            int rawSendCapacity = sentLinesTotal(numberOfClearedLines);
            log.debug("Lines Sent: " + rawSendCapacity);
            score += (long) (10 * Math.pow(rawSendCapacity, 2) * Math.pow(level, 2));

            numberofLinesToClear -= numberOfClearedLines;
            if (numberofLinesToClear < 1) {
                level++;
                currentMillis -= currentMillis / 10;
                if (!isOnline)
                    rescheduleScheduler();
                numberofLinesToClear = LINE_THRESHOLD;
            }
            int garbageToSend = garbagePieceHandler.removeGarbageLines(sentLinesTotal(numberOfClearedLines));
            if (garbageToSend > 0 && isOnline) {
                MatchSendHelper.SENDGARBAGE.sendUpdate(garbageToSend);
            }
        } else {
            combo = -1;
            receiveGarbage();
        }

    }

    /**
     * Calculates the total number of lines sent based on the number of lines cleared.
     *
     * @param linesCleared The number of lines cleared in a single move.
     * @return The total number of lines sent.
     */
    private int sentLinesTotal(int linesCleared) {
        //TODO Tspin Mini maybe? aber kein bock eig.

        int comboLines = sentLinesByCombo();
        int allClearLines = isAllClear();

        if (linesCleared == 4) {
            int backToBackBonus = evaluateBackToBack();
            storeB2B = true;
//            System.out.println("Tetris + Combo " + combo);
            return 4 + comboLines + backToBackBonus + allClearLines;
        }
        if (isTspin) {
            int backToBackBonus = evaluateBackToBack();
            storeB2B = true;
//            System.out.println("T-Spin + Combo " + combo);
            return linesCleared * 2 + comboLines + backToBackBonus + allClearLines;
        }
        storeB2B = false;
//        System.out.println("Combo " + combo);
        return linesCleared - 1 + comboLines + allClearLines;
    }

    /**
     * Calculates the number of sent lines based on the value of the combo variable.
     * <p>
     * The combo variable represents a combo count or score. This method determines how many lines of text
     * should be sent based on the combo value. The combo value can be one of the following:
     * <p>
     * -1: No combo (returns 0)
     * 0: No combo (returns 0)
     * 1 or 2: One line should be sent (returns 1)
     * 3 or 4: Two lines should be sent (returns 2)
     * 5 or 6: Three lines should be sent (returns 3)
     * 7, 8, or 9: Four lines should be sent (returns 4)
     * <p>
     * If the combo value does not match any of the above cases, the method returns 5.
     *
     * @return the number of lines to be sent based on the combo value
     */
    private int sentLinesByCombo() {
        switch (combo) {
            case -1:
            case 0:
                return 0;
            case 1:
            case 2:
                return 1;
            case 3:
            case 4:
                return 2;
            case 5:
            case 6:
                return 3;
            case 7:
            case 8:
            case 9:
                return 4;
        }
        return 5;
    }

    /**
     * Evaluates whether storeB2B is true or false and returns 1 or 0 respectively.
     *
     * @return 1 if storeB2B is true, 0 if storeB2B is false.
     */
    private int evaluateBackToBack() {
        return storeB2B ? 1 : 0;
    }

    /**
     * Determines if the current active piece is in a T-spin position.
     * <p>
     * A T-spin occurs when a T-piece is rotated into a position where it occupies three
     * corners of a 3x3 square (the "T-spin mini"). The fourth corner of the square must
     * be blocked or unoccupied in order for the T-spin to be valid.
     * <p>
     * The method checks if the active piece is a T-piece and if its current rotation
     * is in a
     */
    private void isTspin() {
        Grid current = activePiece.getGrid()[activePiece.getCurrentRotation()];
        isTspin = getActivePiece().toString().equals("T-Piece") && !current.isValidPosition(current.x, current.y - 1);
    }

    /**
     * Moves the rest of the points array down starting from the specified index.
     *
     * @param y the starting index from which to move the elements down
     */
    private void moveRestDown(int y) {
        for (int i = y; i > 0; i--) {
            System.arraycopy(points[i - 1], 0, points[i], 0, points[y].length);
        }
    }

    /**
     * Clears all the points on a specific line.
     *
     * @param y the y-coordinate of the line to be cleared
     */
    private void clearLine(int y) {
        for (Point point : points[y]) {
            point.resetPoint();
        }
    }

    /**
     * Checks if all points in the last row of the points array have the same color as the background color.
     *
     * @return 0 if any point's color is not equal to the background color, 10 if all points have the same color as the background color
     */
    private int isAllClear() {
        for (Point point : points[points.length - 1]) {
            if (point.getColor() != Constants.backgroundColor) {
                return 0;
            }
        }
        return 10;
    }


    /**
     * Prints the tetris field on the given terminal.
     *
     * @param terminal the AsciiPanel on which to print the tetris field
     */
    public void printTetrisField(AsciiPanel terminal) {
        drawBoard(terminal);
        for (int i = 0; i < SCREEN_HEIGHT; i++) {
            for (int j = 0; j < SCREEN_WIDTH; j++) {
                if (points[i + 30][j].isFree()) {
                    terminal.write(Constants.BACKGROUND, startX + j, startY + i, Constants.disabledColor, Constants.backgroundColor);
                } else {
                    terminal.write(Constants.BLOCK, startX + j, startY + i, points[i + 30][j].getColor());
                }
            }
        }
        printCurrentPiece(terminal);
        if (holdPiece != null) {
            printHold(terminal);
        }
        printQueue(terminal);
        printScoreAndStuff(terminal);
    }

    /**
     * Draws the game board with various boxes and displays for hold piece, scoreboard, next four pieces, and incoming garbage.
     *
     * @param terminal the AsciiPanel terminal object
     */
    private void drawBoard(AsciiPanel terminal) {
        int height = startY - 1;
        writeBoxAt(terminal, startX - 1, height, 12, 22);

        height = 15;
        // This is the Box which displays the Hold Piece
        writeBoxAt(terminal, startX - 9, height, 9, 7);

        // This is the Box which displays the scoreboard
        writeBoxAt(terminal, startX - 9, height + 15, 9, 7);

        // These are the boxes drawn which show the next four pieces
        writeBoxAt(terminal, startX + 10, height, 8, 6);
        writeBoxAt(terminal, startX + 10, height + 5, 8, 6);
        writeBoxAt(terminal, startX + 10, height + 10, 8, 6);
        writeBoxAt(terminal, startX + 10, height + 15, 8, 6);
        terminal.write(" ", startX + 11, height + 21);
        // This displays incoming Garbage
        if (isOnline) {
            writeGarbageLine(terminal, startX - 1, height, 22, garbagePieceHandler);
        }
    }

    /**
     * Prints the score and the level on the terminal.
     *
     * @param terminal the AsciiPanel on which to print the score and stuff.
     */
    private void printScoreAndStuff(AsciiPanel terminal) {
        int y = startY + 15;
        terminal.write("LEVEL", startX - 7, y++, Constants.importantText);
        terminal.write(String.format("  %03d", level), startX - 8, y++, Constants.characterColor);
        terminal.write(Strings.repeat(Character.toString('-'), 7), startX - 8, y++, Constants.wallColor);
        terminal.write(" SCORE ", startX - 8, y++, Constants.importantText);
        terminal.write(String.format("%07d", score), startX - 8, y, Constants.characterColor);
    }

    /**
     * Prints the current piece on the ASCII panel.
     *
     * @param terminal the ASCII panel on which the piece will be printed
     */
    private void printCurrentPiece(AsciiPanel terminal) {
        Grid activePieceGrid = activePiece.returnPiece();
        Boolean[][] gridPoints = activePieceGrid.getSetPoints();
        Grid helperPieceGrid = helperPiece.returnPiece();

        for (int y = 0; y < gridPoints.length; y++) {
            for (int x = 0; x < gridPoints[y].length; x++) {
                if (gridPoints[y][x]) {
                    terminal.write(Constants.BLOCK, startX + x + helperPieceGrid.x, startY + y + helperPieceGrid.y - 30, Constants.disabledColor, Constants.backgroundColor);
                    terminal.write(Constants.BLOCK, startX + x + activePieceGrid.x, startY + y + activePieceGrid.y - 30, activePieceGrid.getColor());
                }
            }
        }
    }

    /**
     * Prints the hold piece on the terminal.
     *
     * @param terminal the AsciiPanel object representing the terminal.
     */
    private synchronized void printHold(AsciiPanel terminal) {
        synchronized (holdPiece) {
            Grid holdPieceGrid = holdPiece.getGrid()[0];
            Boolean[][] gridPoints = holdPieceGrid.getSetPoints();
            terminal.write(Constants.BLOCKCHAIN, startX - 7, startY + 1, Constants.backgroundColor);
            terminal.write(Constants.BLOCKCHAIN, startX - 7, startY + 2, Constants.backgroundColor);
            terminal.write(Constants.BLOCKCHAIN, startX - 7, startY + 3, Constants.backgroundColor);
            for (int y = 0; y < gridPoints.length; y++) {
                for (int x = 0; x < gridPoints[y].length; x++) {
                    if (gridPoints[y][x]) {
                        terminal.write(Constants.BLOCK, startX - 7 + x, startY + 1 + y, holdPieceGrid.getColor());
                    }
                }
            }
        }
    }

    /**
     * Prints the queue of next pieces on the specified terminal.
     *
     * @param terminal the AsciiPanel instance to print the queue on
     */
    private synchronized void printQueue(AsciiPanel terminal) {
        for (int i = 0; i < nextPieces.size(); i++) {
            Grid holdPieceGrid = nextPieces.get(i).getGrid()[0];
            Boolean[][] gridPoints = holdPieceGrid.getSetPoints();
            terminal.write(Constants.BLOCKCHAIN, startX + 12, startY + 1 + i * 5, Constants.backgroundColor);
            terminal.write(Constants.BLOCKCHAIN, startX + 12, startY + 2 + i * 5, Constants.backgroundColor);
            terminal.write(Constants.BLOCKCHAIN, startX + 12, startY + 3 + i * 5, Constants.backgroundColor);
            for (int y = 0; y < gridPoints.length; y++) {
                for (int x = 0; x < gridPoints[y].length; x++) {
                    if (gridPoints[y][x]) {
                        terminal.write(Constants.BLOCK, startX + 12 + x, startY + 1 + y + i * 5, holdPieceGrid.getColor());
                    }
                }
            }
        }
    }

    /**
     * Checks if the pixel at the specified coordinates is free.
     *
     * @param x the x-coordinate of the pixel
     * @param y the y-coordinate of the pixel
     * @return true if the pixel is free, false otherwise
     */
    public boolean isFreePixel(int x, int y) {
        if (x < 0 || y < 0 || x >= 10 || y >= 50) {
            return false;
        }
        return points[y][x].isFree();
    }

    /**
     * Moves the active piece to the left.
     * This method calls the {@link Tetromino#movePieceLeft()} method of the active piece to move it one step to the left,
     * and then calculates the new position for the helper piece.
     */
    public void moveLeft() {
        getActivePiece().movePieceLeft();
        calculateNewHelperPiecePosition();
    }

    /**
     * Moves the active piece to the right.
     * This method calls the {@link Tetromino#movePieceRight()} method of the active piece to move it one step to the right,
     * and then calculates the new position for the helper piece.
     */
    public void moveRight() {
        getActivePiece().movePieceRight();
        calculateNewHelperPiecePosition();
    }

    /**
     * Rotates the active piece clockwise and calculates the new position for the helper piece.
     */
    public void rotateClockwise() {
        getActivePiece().rotateClockwise();
        calculateNewHelperPiecePosition();
    }

    /**
     * Rotates the active piece counterclockwise and calculates the new position for the helper piece.
     */
    public void rotateCClockwise() {
        getActivePiece().rotateCClockwise();
        calculateNewHelperPiecePosition();
    }

    /**
     * Advances the game state by one unit of time.
     * If the active piece has reached the bottom of the grid or collided with another piece,
     * a new active piece is generated.
     */
    public void gameTick() {
        if (getActivePiece().gameTick()) {
            addGrid(getActivePiece().returnPiece());
            newActivePiece();
        }
    }

    /**
     * Moves the active piece all the way down in the Tetris field (hard drop).
     * Calculates the number of rows the piece has dropped and adjusts the score accordingly.
     * Creates a new active piece after the hard drop.
     */
    public void hardDrop() {
        int counter = helperPiece.getY() - activePiece.getY();
        addGrid(helperPiece.returnPiece());
        score += (counter) >> 1;
        newActivePiece();
    }


    /**
     * Creates a new active piece and updates the game state.
     * If the game is online, it updates the board by sending an update to the server.
     * If the active piece reaches an invalid position at the top of the grid, the game ends.
     * It also updates the next pieces, helper piece position, and allows piece swapping.
     */
    public void newActivePiece() {
        if (isOnline) {
            activePiece.setY(activePiece.returnPiece().getY());
            MatchSendHelper.UPDATEBOARD.sendUpdate(activePiece);
        }
        activePiece = generator.getNext();
        if (!activePiece.getGrid()[0].isValidPosition(3, 30)) {
            if (activePiece.getGrid()[0].isValidPosition(3, 29)) {
                activePiece.setY(29);
            } else {
                exec.shutdownNow();
                if (isOnline) {
                    MatchSendHelper.LOOSE.sendUpdate();
                    onlineScreen.loseScreen = true;
                } else {
                    offlineScreen.loseScreen = true;
                }
            }
        }
        nextPieces = generator.peek(4);
        calculateNewHelperPiecePosition();
        if (!allowSwap) {
            holdPiece.returnNormalColor();
        }
        allowSwap = true;
    }

    /**
     * Calculates the new position of the helper piece.
     * <p>
     * This method creates a clone of the active piece and performs a hard drop action on it,
     * resulting in the helper piece being moved to its new position.
     */
    private void calculateNewHelperPiecePosition() {
        helperPiece = activePiece.clonePiece();
        helperPiece.hardDrop();
    }


    /**
     * Drops the active piece one cell down without locking it.
     * The dropped piece will remain movable until it collides with another piece or the game board boundary.
     */
    public void softDrop() {
        getActivePiece().softDrop();
    }

    /**
     * Reschedules the scheduler by shutting down the current executor and creating a new executor with a single thread.
     * The scheduler will execute the "gameTick" method at a fixed rate based on the "currentMillis" value.
     */
    private void rescheduleScheduler() {
        exec.shutdownNow();
        exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(this::gameTick, 0, currentMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Shuts down the thread.
     * <p>
     * This method interrupts and terminates the currently running thread.
     * </p>
     */
    public void shutdownThread() {
        exec.shutdownNow();
    }

    /**
     * Shifts the active piece to the left until it can no longer move left.
     *
     * <p>
     * This method continuously moves the active piece to the left until it cannot move further in that direction.
     * It is part of a game or puzzle solving algorithm where the objective is to find the leftmost position for the active piece.
     * </p>
     *
     * <p>
     * This method uses a while loop to repeatedly move the active piece to the left.
     * It checks if the location of the piece has changed after each left movement.
     * If the location has not changed, it means that the piece has reached the leftmost possible position and the loop is terminated.
     * </p>
     */
    public void dasLeft() {
        boolean locationChanged = true;
        while (locationChanged) {
            int oldX = activePiece.getX();
            moveLeft();
            if (oldX == activePiece.getX()) {
                locationChanged = false;
            }
        }
    }

    /**
     * Shifts the active piece to the right until it can no longer move right.
     *
     * <p>
     * This method continuously moves the active piece to the right until it cannot move further in that direction.
     * It is part of a game or puzzle solving algorithm where the objective is to find the rightmost position for the active piece.
     * </p>
     *
     * <p>
     * This method uses a while loop to repeatedly move the active piece to the right.
     * It checks if the location of the piece has changed after each left movement.
     * If the location has not changed, it means that the piece has reached the rightmost possible position and the loop is terminated.
     * </p>
     */
    public void dasRight() {
        boolean locationChanged = true;
        while (locationChanged) {
            int oldX = activePiece.getX();
            moveRight();
            if (oldX == activePiece.getX()) {
                locationChanged = false;
            }
        }
    }

    /**
     * Performs an instant drop of the active game piece and updates the score accordingly.
     * The active game piece will be dropped to the lowest possible position in the game grid.
     * The score will be increased based on the number of rows the tetromino was dropped.
     */
    public void instantsdf() {
        int counter = activePiece.instantSDF();
        score += (counter) >> 1;
    }
}
