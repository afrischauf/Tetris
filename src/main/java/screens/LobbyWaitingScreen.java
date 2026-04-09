package screens;

import config.Constants;
import helper.TerminalHelper;

import com.heroiclabs.nakama.api.Group;
import com.heroiclabs.nakama.api.GroupUserList;
import com.heroiclabs.nakama.api.UserGroupList;
import communication.MatchSendHelper;
import communication.Player;
import lombok.extern.slf4j.Slf4j;
import nakama.com.google.common.reflect.TypeToken;
import nakama.com.google.gson.Gson;

import java.awt.event.KeyEvent;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This class represents the Lobby Waiting Screen.
 */
@Slf4j
public class LobbyWaitingScreen implements Screen, Runnable {

    /**
     * The {@code exec} variable represents a private final ScheduledExecutorService instance in this class.
     * <p>
     * A ScheduledExecutorService is an interface representing an ExecutorService that can schedule tasks to run either periodically or after a delay.
     * Being marked as final, the reference to the ScheduledExecutorService cannot be changed once initialized.
     */
    private final ScheduledExecutorService exec;

    /**
     * This class represents a unique identifier for a group.
     * The groupID is a string value and is marked as final to ensure its immutability.
     */
    private final String groupID;

    /**
     * The name of the lobby.
     */
    private final String lobbyName;

    /**
     * Represents the current player.
     *
     * <p>
     * The 'me' variable is a private static variable of type 'Player' that stores the current player object.
     * This variable is used to access and modify the properties and behavior of the player within the application.
     * </p>
     *
     * @see Player
     */
    private static Player me;
    /**
     * The opponent variable represents the player's opponent in a game.
     * It is a static variable, meaning that it belongs to the class and not to an instance of the class.
     * Therefore, it can be accessed and modified by any method or object of the class.
     */
    private static Player opponent;

    /**
     * Indicates whether the game should start or not.
     * By default, it is set to false.
     */
    private static boolean startGame = false;

    /**
     * Creates a new instance of the LobbyWaitingScreen class.
     *
     * @param groupID      the group ID associated with the lobby
     * @param lobbyName    the name of the lobby
     * @param createdLobby indicates whether the lobby was already created or not
     */
    public LobbyWaitingScreen(String groupID, String lobbyName, boolean createdLobby) {
        if (!createdLobby) {
            MainClass.aClass.group_id = groupID;
            try {
                MainClass.aClass.createdGroup = false;
                UserGroupList userGroups = MainClass.aClass.client.listUserGroups(MainClass.aClass.session, MainClass.aClass.session.getUserId()).get();
                Group group = userGroups.getUserGroups(0).getGroup();
                Gson gson = new Gson();
                String s = group.getMetadata();
                Type type = new TypeToken<HashMap<String, String>>() {
                }.getType();
                HashMap<String, String> map = gson.fromJson(s, type);
                MainClass.aClass.match = MainClass.aClass.socket.joinMatch(map.get("MatchID")).get();
            } catch (Exception e) {
                try {
                    throw e;
                } catch (InterruptedException | ExecutionException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        this.groupID = groupID;
        this.lobbyName = lobbyName;
        this.exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(this, 0, 1, TimeUnit.SECONDS);
        me = null;
        opponent = null;
    }

    /**
     * Marks the start of the game.
     * <p>
     * Once called, this method will set the game flag to indicate that the game has started.
     */
    public static void startGame() {
        startGame = true;
    }

    /**
     * Runs the method to retrieve the group users and set the player objects.
     */
    @Override
    public void run() {
        try {
            GroupUserList groupUserList = MainClass.aClass.client.listGroupUsers(MainClass.aClass.session, this.groupID).get();
            for (GroupUserList.GroupUser groupUser : groupUserList.getGroupUsersList()) {
                String userId = groupUser.getUser().getId();
                if (userId.equals(MainClass.aClass.user_id) && (me == null || !me.getPlayerId().equals(userId))) {
                    me = new Player(groupUser.getUser().getId(), groupUser.getUser().getUsername(), MainClass.aClass.createdGroup);
                } else {
                    if (userId.equals(MainClass.aClass.user_id)) {
                        continue;
                    }
                    if (opponent == null || !opponent.getPlayerId().equals(userId))
                        opponent = new Player(groupUser.getUser().getId(), groupUser.getUser().getUsername(), !MainClass.aClass.createdGroup);
                }
            }
            if (groupUserList.getGroupUsersList().size() == 1) {
                opponent = null;
            }
        } catch (Exception ignored) {
        }

    }

    /**
     * Displays the output on the given AsciiPanel terminal.
     *
     * @param terminal the AsciiPanel object to display the output on
     */
    @Override
    public void displayOutput(AsciiPanel terminal) {
        if (startGame) {
            MainClass.aClass.setScreen(new PlayOnlineScreen(terminal, me.isHost()));
            MainClass.aClass.repaint();
            return;
        }
        terminal.clear();
        TerminalHelper.writeTetrisLogo(terminal);
        int y = 10;
        terminal.writeCenter(String.format("<--- LobbyName: %s --->", lobbyName), y++);
        terminal.write("Current Players waiting in the Lobby", 5, y++);
        y++;
        if (me != null && me.isHost()) {
            if (me.isReady()) {
                terminal.write(me.getDisplayName(), 5, y++, Constants.selectedColor);
            } else {
                terminal.write(me.getDisplayName(), 5, y++);
            }
            if (opponent != null) {
                if (opponent.isReady()) {
                    terminal.write(opponent.getDisplayName(), 5, y, Constants.selectedColor);
                } else {
                    terminal.write(opponent.getDisplayName(), 5, y);
                }
            }
        } else {
            if (opponent != null) {
                if (opponent.isReady()) {
                    terminal.write(opponent.getDisplayName(), 5, y++, Constants.selectedColor);
                } else {
                    terminal.write(opponent.getDisplayName(), 5, y++);
                }
            }
            if (me != null) {
                if (me.isReady()) {
                    terminal.write(me.getDisplayName(), 5, y, Constants.selectedColor);
                } else {
                    terminal.write(me.getDisplayName(), 5, y);
                }
            }
        }
        if (bothPlayersAreReady()) {
            if (me.isHost())
                terminal.writeCenter("-- Press [Enter] to start the game --", terminal.getHeightInCharacters() - 1);
            else
                terminal.writeCenter("-- Wait for the Host to start the game --", terminal.getHeightInCharacters() - 1);

        } else {
            terminal.writeCenter("-- Press [r] to get ready --", terminal.getHeightInCharacters() - 1);
        }
    }

    /**
     * Responds to the user input by updating the player's readiness and sending updates.
     * If both players are ready and the current player is the host, the online match starts
     * and returns a new PlayOnlineScreen object.
     * Otherwise, returns the current object.
     *
     * @param key the KeyEvent object representing the key pressed by the user
     * @param terminal the AsciiPanel object representing the terminal
     * @return the Screen object to be displayed after user input is processed
     */
    @Override
    public Screen respondToUserInput(KeyEvent key, AsciiPanel terminal) {
        if (Character.toLowerCase(key.getKeyChar()) == 'r') {
            me.setReady(!me.isReady());
            MatchSendHelper.READY.sendUpdate(me.getPlayerId(), me.isReady());
        }
        if (key.getKeyCode() == KeyEvent.VK_ENTER) {
            if (bothPlayersAreReady() && me.isHost()) {
                MatchSendHelper.START.sendUpdate();
                PlayOnlineScreen screen = new PlayOnlineScreen(terminal, me.isHost());
                screen.displayOutput(terminal);
                return screen;
            }
        }
        return this;
    }

    /**
     * Checks if both players are ready.
     *
     * @return true if both players are ready, false otherwise.
     */
    private boolean bothPlayersAreReady() {
        if (me != null && opponent != null)
            return me.isReady() && opponent.isReady();
        return false;
    }

    /**
     * Finishes the input process and returns a boolean value indicating the status.
     *
     * @return {@code true} if the input process is successfully finished, {@code false} otherwise.
     */
    @Override
    public boolean finishInput() {
        return false;
    }

    /**
     * Updates the state of a player identified by their playerId.
     *
     * @param playerId The unique identifier of the player.
     * @param state The new state of the player.
     *              - If true, indicates the player is ready.
     *              - If false, indicates the player is not ready.
     */
    public static void updatePlayerState(String playerId, boolean state) {
        if (playerId.equals(me.getPlayerId())) {
            me.setReady(state);
        } else if (playerId.equals(opponent.getPlayerId())) {
            opponent.setReady(state);
        }
    }

    @Override
    public void close() {
        exec.shutdownNow();
        me = null;
        opponent = null;
        startGame = false;
    }

}
