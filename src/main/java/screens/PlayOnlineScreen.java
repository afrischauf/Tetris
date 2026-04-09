package screens;

import config.Constants;
import helper.TerminalHelper;

import communication.MatchSendHelper;
import config.keys.KeyPlay;
import logic.OpponentTetrisField;
import logic.TetrisField;

import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class PlayOnlineScreen implements Screen, Runnable {

    private static TetrisField field;
    private final long startTime;
    public boolean loseScreen = false;
    public static boolean win = false;

    public static OpponentTetrisField opponentTetrisField;

    private final ScheduledExecutorService exec;
    private final ScheduledExecutorService inputExecutor;

    private final boolean isHost;

    private final Set<KeyPlay> pressedKeys = new HashSet<>();
    private boolean matchExited;

    public PlayOnlineScreen(AsciiPanel terminal, boolean isHost) {
        field = new TetrisField(1, this, (terminal.getWidthInCharacters() - 12) / 2, 16);
        startTime = System.currentTimeMillis();
        opponentTetrisField = new OpponentTetrisField((terminal.getWidthInCharacters()) / 2 + 30, 16);
        exec = Executors.newSingleThreadScheduledExecutor();
        inputExecutor = Executors.newSingleThreadScheduledExecutor();
        this.isHost = isHost;
        if (isHost) {
            exec.scheduleAtFixedRate(PlayOnlineScreen::tickMaster, 0, 1, TimeUnit.SECONDS);
        }
        inputExecutor.scheduleAtFixedRate(this, 0, Constants.KEYLISTENERTIMER, TimeUnit.MILLISECONDS);

    }

    public static void gameTick() {
        field.gameTick();
    }

    public static void tickMaster() {
        gameTick();
        MatchSendHelper.GAMETICK.sendUpdate();
    }

    public void exitGroupMatch() {
        if (matchExited) {
            return;
        }
        matchExited = true;
        try {
            if (MainClass.aClass.socket != null && MainClass.aClass.match != null) {
                MainClass.aClass.socket.leaveMatch(MainClass.aClass.match.getMatchId()).get();
            }
        } catch (InterruptedException | ExecutionException e) {
            log.debug("Failed to leave match cleanly", e);
        } finally {
            MainClass.aClass.cleanupLobbyMembership();
        }

    }

    @Override
    public void displayOutput(AsciiPanel terminal) {
        if (win) {
            MainClass.aClass.setScreen(new WinScreen());
            MainClass.aClass.repaint();
            return;
        }
        if (loseScreen) {
            MainClass.aClass.setScreen(new OnlineLoseScreen());
            MainClass.aClass.repaint();
            return;
        }
        terminal.clear();
        TerminalHelper.writeTetrisLogo(terminal);
        field.printTetrisField(terminal);
        opponentTetrisField.printTetrisField(terminal);
    }

    @Override
    public Screen respondToUserInput(KeyEvent key, AsciiPanel terminal) {

        return this;
    }

    @Override
    public boolean finishInput() {
        return false;
    }

    public void addKey(KeyEvent keyEvent) {
        if (KeyPlay.getKey(keyEvent, false) != null) {
            synchronized (pressedKeys) {
                pressedKeys.add(KeyPlay.getKey(keyEvent, false));
            }
        }
    }

    public void removeKey(KeyEvent keyEvent) {
        if (KeyPlay.getKey(keyEvent, false) != null) {
            synchronized (pressedKeys) {
                pressedKeys.remove(KeyPlay.getKey(keyEvent, true));
            }
        }
    }

    @Override
    public void run() {
        synchronized (pressedKeys) {
            for (KeyPlay pressedKey : pressedKeys) {
                pressedKey.execute(field);
                pressedKey.incrementCounter();
            }
        }
    }

    @Override
    public void close() {
        inputExecutor.shutdownNow();
        exec.shutdownNow();
        if (field != null) {
            field.shutdownThread();
            field = null;
        }
        synchronized (pressedKeys) {
            pressedKeys.clear();
        }
        opponentTetrisField = null;
        win = false;
        loseScreen = false;
        exitGroupMatch();
    }
}
