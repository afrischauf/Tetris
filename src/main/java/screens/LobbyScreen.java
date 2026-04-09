package screens;

import config.Constants;
import helper.TerminalHelper;

import com.heroiclabs.nakama.api.Group;
import com.heroiclabs.nakama.api.GroupList;
import lombok.extern.slf4j.Slf4j;

import java.awt.event.KeyEvent;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * LobbyScreen is a class that represents the lobby screen of a game. It displays a list of available lobbies
 * and allows the user to select and join a lobby.
 */
@Slf4j
public class LobbyScreen implements Screen, Runnable {

    /**
     * A private static final map that represents the lobbies.
     *
     * <p>
     * The lobbies map is used to store the name and description of the lobbies.
     * Each lobby is represented by a key-value pair, where the key is the lobby name
     * and the value is the lobby description.
     * </p>
     *
     * <p>
     * The ordering of the lobbies is maintained by using the LinkedHashMap implementation.
     * </p>
     */
    private static final Map<String, String> lobbies = new LinkedHashMap<>();

    /**
     * This Scheduled Executor is used to fetch the lobbies once every second
     */
    private final ScheduledExecutorService exec;

    /**
     * A volatile AtomicBoolean variable used to control the execution of a task or loop.
     */
    public volatile AtomicBoolean runnable;

    /**
     * Represents a scheduled future result.
     * <p>
     * The result is a reference to a future task that is scheduled for execution in a thread pool.
     */
    ScheduledFuture<?> result;

    /**
     * Represents the index of the currently selected item.
     * <p>
     * The value of the selected index represents the position of the selected item
     * in a list or array. A value of -1 indicates that no item is currently selected.
     */
    private int selected = -1;

    /**
     * Represents the lobby screen of the game.
     */
    public LobbyScreen() {
        runnable = new AtomicBoolean();
        runnable.set(true);
        exec = Executors.newSingleThreadScheduledExecutor();
    }


    /**
     * Starts a new thread and schedules the execution of this object at fixed rate.
     * The execution will begin immediately and will repeat every second.
     */
    public void startThread() {
        result = exec.scheduleAtFixedRate(this, 0, 1, TimeUnit.SECONDS);
    }

    /**
     * Fetches the lobbies from the server and populates the 'lobbies' map.
     * This method is responsible for synchronizing access to the 'lobbies' map.
     */
    private void fetchLobbies() {
        synchronized (lobbies) {
            lobbies.clear();
            try {
                GroupList list = MainClass.aClass.client.listGroups(MainClass.aClass.session, "%").get();
                for (Group group : list.getGroupsList()) {
                    lobbies.put(group.getId(), group.getName());
                }
            } catch (Exception e) {
                log.error(e.toString());
            }
        }
    }

    /**
     * Displays the output in the given AsciiPanel terminal.
     *
     * @param terminal the AsciiPanel terminal to display the output in
     */
    @Override
    public void displayOutput(AsciiPanel terminal) {
        terminal.clear();
        TerminalHelper.writeTetrisLogo(terminal);
        terminal.write(new Date().toString(), 5, 20);
        int y = 10;
        synchronized (lobbies) {
            for (Map.Entry<String, String> value : lobbies.entrySet()) {
                if (selected == y - 10) {
                    terminal.write("Lobby: " + value.getValue(), 5, y++, Constants.selectedColor);
                } else {
                    terminal.write("Lobby: " + value.getValue(), 5, y++);
                }
            }
        }
        terminal.write("To refresh the lobby screen press [r]", 5, ++y);
        terminal.write("To create your own Lobby press [c]", 5, ++y);
    }

    /**
     * Responds to the user's input by performing various actions depending on the key pressed,
     * and returns the corresponding screen to be displayed.
     *
     * @param key      the KeyEvent object representing the key pressed by the user
     * @param terminal the AsciiPanel object representing the terminal
     * @return the Screen object to be displayed
     */
    @Override
    public Screen respondToUserInput(KeyEvent key, AsciiPanel terminal) {
        if (Character.toLowerCase(key.getKeyChar()) == 'c') {
            MainClass.aClass.createSocket();
            synchronized (runnable) {
                runnable.set(false);
            }
            LobbyCreateScreen screen = new LobbyCreateScreen();
            screen.displayOutput(terminal);
            return screen;
        }
        if (Character.toLowerCase(key.getKeyCode()) == 'r') {
            this.fetchLobbies();
        }
        if (key.getKeyCode() == KeyEvent.VK_UP || Character.toLowerCase(key.getKeyChar()) == 'w') {
            this.selectAbove();
        } else if (key.getKeyCode() == KeyEvent.VK_DOWN || Character.toLowerCase(key.getKeyChar()) == 's') {
            this.selectBelow();
        }
        try {
            if (key.getKeyCode() == KeyEvent.VK_ENTER || key.getKeyChar() == ' ') {
                if (selected == -1) {
                    return this;
                }
                MainClass.aClass.createSocket();
                int c = 0;
                for (String group_id : lobbies.keySet()) {
                    if (c == selected) {
                        try {
                            MainClass.aClass.client.joinGroup(MainClass.aClass.session, group_id).get();
                            synchronized (runnable) {
                                runnable.set(false);
                            }
                            LobbyWaitingScreen waitingScreen = new LobbyWaitingScreen(group_id, lobbies.get(group_id), false);
                            waitingScreen.displayOutput(terminal);
                            return waitingScreen;
                        } catch (InterruptedException | ExecutionException e) {
                            log.error(e.toString());
                            // TODO Handle if Group is full. Maybe just display Groups that aren't full
                        }
                    }
                    c++;
                }
            }
        } catch (NullPointerException ignored) {

        }
        return this;
    }

    /**
     * Completes the input process.
     *
     * @return true if the input is finished, false otherwise.
     */
    @Override
    public boolean finishInput() {
        return false;
    }

    /**
     * Executes the run method in a synchronized block to ensure
     * thread safety. If the runnable flag is set to true, it calls
     * the fetchLobbies method. If the flag is set to false, it
     * shuts down the executor service and throws a RuntimeException.
     */
    @Override
    public void run() {
        synchronized (runnable) {
            if (runnable.get()) {
                fetchLobbies();
            } else {
                exec.shutdownNow();
            }
        }
    }

    /**
     * Selects the item above the currently selected item.
     * If the currently selected item is the first item, it wraps around and selects the last item.
     * Otherwise, it selects the item above the currently selected item.
     */
    public void selectAbove() {
        if (selected == 0) {
            selected = lobbies.size() - 1;
        } else {
            selected--;
        }
    }

    /**
     * Selects the next item below the currently selected item.
     * If the currently selected item is the last item in the list,
     * the selection will wrap around to the first item.
     */
    public void selectBelow() {
        if (selected == lobbies.size() - 1) {
            selected = 0;
        } else {
            selected++;
        }
    }

    @Override
    public void close() {
        synchronized (runnable) {
            runnable.set(false);
        }
        if (result != null) {
            result.cancel(true);
        }
        exec.shutdownNow();
    }

}
