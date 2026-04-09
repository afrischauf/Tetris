package screens;

import helper.ClasspathResourceLoader;
import helper.OsUtil;
import asciiPanel.AsciiFont;
import com.heroiclabs.nakama.*;
import communication.MatchSendHelper;
import config.keys.KeyMenuConfig;
import config.keys.KeyPlay;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.plexus.util.IOUtil;

import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.nio.file.Files;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


//TODO fix issue that skill issue player has to restart the game
//TODO fix issue that losing Player doesn't display the game correctly
//TODO go back the menu...
//TODO Add Config so that player don't have to input everything all the time
//TODO There is still one Misconfiguration of the Drops
//TODO also fix the way how loosing is implemented

/**
 * This class is the Main Class from which the complete Code is being run
 * It uses AsciiPanel which depends on Java Swing to display the Output and it uses Nakama for Communication between multiple Clients
 */
@Slf4j
public class MainClass extends JFrame implements KeyListener {

    /**
     * This is used as a Reference so that the Screens are able to get the session, screen, etc.
     */
    public static MainClass aClass;

    /**
     * This Variable stores the current Screen
     */
    public Screen screen;

    /**
     * This Variable stores the current Session
     */
    public Session session;

    /**
     * This Variable stores the current Client
     */
    public Client client;

    /**
     * This Variable stores the current Socket
     */
    public SocketClient socket;

    /**
     * This Variable stores the group_id, which is used to join a group
     */
    public String group_id = "";

    /**
     * This Variable stores the user_id, which is used to check if the Player is the Host of a Lobby
     */
    public String user_id = "";

    /**
     * This Variable stores if the Player is currently in a Match
     */
    public Match match;

    /**
     * This Variable stores if the Player created the Group/Lobby
     */
    public boolean createdGroup;

    /**
     * This Variable stores the terminal which is used by the Screens to display the output
     */
    private final AsciiPanel terminal;
    private final ScheduledExecutorService repaintExecutor;

    /**
     * Constructor for MainClass
     * Creates the AsciiPanel, a Thread, and a Listener
     * One Thread which updates the AsciiPanel and a Listener which handles Resizing of the App
     */
    public MainClass() {
        super();
        terminal = new AsciiPanel(80, 50, new AsciiFont("custom_cp437_20x20.png", 20, 20));
        screen = new StartScreen();
        add(terminal);
        pack();
        repaint();
        addKeyListener(this);
        repaintExecutor = Executors.newSingleThreadScheduledExecutor();
        repaintExecutor.scheduleAtFixedRate(this::repaint, 0, 10, TimeUnit.MILLISECONDS);
        terminal.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                terminal.resize(terminal.getWidth(), terminal.getHeight());
            }
        });
    }

    /**
     * PSVM...
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        MainClass mainClass = new MainClass();
        mainClass.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainClass.setVisible(true);
        createConfig();
        KeyMenuConfig.initializeKeymap();
        KeyPlay.initializeKeymap();
        aClass = mainClass;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            int executions = PlayOfflineScreen.executeCounter;
            long shutdownTime = System.currentTimeMillis();
            long starTime = PlayOfflineScreen.firstExecution;
            long runtimeSeconds = Math.max(1, (shutdownTime - starTime) / 1000);
            long executions_second = executions / runtimeSeconds;
            System.out.printf("Start: %d\nEnd: %d\nExecutions/s: %d\n", starTime, shutdownTime, executions_second);
            if (MainClass.aClass != null) {
                MainClass.aClass.cleanupLobbyMembership();
            }

        }, "Delete all groups"));
    }

    /**
     * If the Config File doesn't exist it creates them in the following directory depending on the OS:
     * Linux: ~/.config/tty-tetris/tty-tetris.conf
     * Windows: %appdata%/tty-tetris/tty-tetris.conf
     * Mac: idk
     */
    @SneakyThrows
    private static void createConfig() {
        File configFile = OsUtil.getConfigFile("tty-tetris.conf");
        if (configFile.exists())
            return;
        InputStream defaultConfig = ClasspathResourceLoader.of("tty-tetris.conf").getInputStream();
        IOUtil.copy(defaultConfig, Files.newOutputStream(configFile.toPath()));
    }


    /**
     * This is called in a Thread and updates the App Display-output
     */
    public void repaint() {
        SwingUtilities.invokeLater(() -> {
            Screen currentScreen = screen;
            currentScreen.displayOutput(terminal);
            super.repaint();
        });
    }

    /**
     * Replaces the current screen and closes the previous one if necessary.
     *
     * @param nextScreen the screen to display next
     * @return the activated screen
     */
    public synchronized Screen setScreen(Screen nextScreen) {
        if (nextScreen == null) {
            return screen;
        }
        Screen previousScreen = screen;
        if (previousScreen == nextScreen) {
            return screen;
        }
        if (previousScreen != null) {
            previousScreen.close();
        }
        screen = nextScreen;
        return screen;
    }

    /**
     * Cleans up any tracked multiplayer group or match membership.
     */
    public synchronized void cleanupLobbyMembership() {
        if (client == null || session == null || group_id == null || group_id.isEmpty()) {
            resetMatchState();
            return;
        }
        try {
            if (createdGroup) {
                client.deleteGroup(session, group_id);
            } else {
                client.leaveGroup(session, group_id);
            }
        } catch (RuntimeException e) {
            log.debug("Failed to clean up lobby membership", e);
        } finally {
            resetMatchState();
        }
    }

    /**
     * Clears tracked per-match state after leaving a multiplayer flow.
     */
    public synchronized void resetMatchState() {
        match = null;
        group_id = "";
        createdGroup = false;
    }

    /**
     * This creates the Socket which is used for Communcation with Nakama
     */
    public void createSocket() {
        try {
            this.socket = this.client.createSocket();
            SocketListener listener = new AbstractSocketListener() {
                @Override
                public void onDisconnect(final Throwable t) {
                    log.debug("Socket disconnected.");
                }

                @Override
                public void onMatchData(MatchData matchData) {
                    if (matchData.getData() != null) {
                        MatchSendHelper.receiveUpdate((int) matchData.getOpCode(), new String(matchData.getData()));
                    } else {
                        MatchSendHelper.receiveUpdate((int) matchData.getOpCode(), null);
                    }

                }
            };
            this.socket.connect(MainClass.aClass.session, listener).get();
            log.debug("Socket connected.");
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Depending on the current Screen, this Method handles KeyEvents differently
     * When the Screen is currently either a {@link PlayOnlineScreen} or a {@link PlayOfflineScreen}, it executes {@link PlayOnlineScreen#addKey(KeyEvent)}
     * When the Screen is not one of these two Screens, and if the Screen is not currently inside an InputField then it sends the KeyEvent to {@link KeyMenuConfig#execute(KeyEvent, Screen, AsciiPanel)}
     * If the Event is currently inside an InputField then it currently forwards the KeyEvent to {@link Screen#respondToUserInput(KeyEvent, AsciiPanel)}
     *
     * @param key the event to be processed
     */
    @Override
    public void keyPressed(KeyEvent key) {
        if (!(screen instanceof PlayOfflineScreen || screen instanceof PlayOnlineScreen)) {
            if (screen.isInsideInputField()) {
                setScreen(screen.respondToUserInput(key, terminal));
            } else {
                setScreen(KeyMenuConfig.execute(key, screen, terminal));
            }
        } else if (screen instanceof PlayOfflineScreen) {
            PlayOfflineScreen screen1 = ((PlayOfflineScreen) screen);
            screen1.addKey(key);
        } else {
            ((PlayOnlineScreen) screen).addKey(key);
        }
    }

    /**
     * Depending on the current Screen, this Method handles KeyEvents differently
     * When the Screen is currently either a {@link PlayOnlineScreen} or a {@link PlayOfflineScreen}, then it executes {@link PlayOnlineScreen#removeKey(KeyEvent)}
     *
     * @param key the event to be processed
     */
    @Override
    public void keyReleased(KeyEvent key) {
        if (screen instanceof PlayOfflineScreen) {
            ((PlayOfflineScreen) screen).removeKey(key);
        } else if (screen instanceof PlayOnlineScreen) {
            ((PlayOnlineScreen) screen).removeKey(key);
        }
    }

    /**
     * Ignored...
     *
     * @param key the event to be processed
     */
    @Override
    public void keyTyped(KeyEvent key) {
    }

}
