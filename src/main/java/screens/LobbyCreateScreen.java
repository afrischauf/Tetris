package screens;

import helper.TerminalHelper;

import com.heroiclabs.nakama.api.Group;
import com.heroiclabs.nakama.api.Rpc;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Slf4j
public class LobbyCreateScreen implements Screen {

    /**
     * The Name of the Group that should be created
     */
    private String groupName = "";

    /**
     * Clears the terminal, writes the tetris logo, and displays the lobby name.
     *
     * @param terminal The AsciiPanel terminal
     */
    @Override
    public void displayOutput(AsciiPanel terminal) {
        terminal.clear();
        TerminalHelper.writeTetrisLogo(terminal);
        int y = 10;
        terminal.write("LobbyName: " + groupName, 5, y);
    }

    /**
     * Responds to the user input.
     *
     * @param key      The KeyEvent object representing the user input key
     * @param terminal The AsciiPanel terminal object
     * @return The next screen to be displayed
     */
    @Override
    public Screen respondToUserInput(KeyEvent key, AsciiPanel terminal) {
        String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_1234567890";
        if (key.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            if (groupName.length() > 0) {
                groupName = groupName.substring(0, groupName.length() - 1);
            }
        } else if (key.getKeyCode() == KeyEvent.VK_ENTER) {
            String desc = "Will be ignored anyways";
            String avatarURL = "";
            String langTag = "";
            boolean open = true;
            int maxSize = 2;
            try {
                Group group = MainClass.aClass.client.createGroup(MainClass.aClass.session, groupName, desc, avatarURL, langTag, open, maxSize).get();
                MainClass.aClass.group_id = group.getId();
                MainClass.aClass.createdGroup = true;
                MainClass.aClass.match = MainClass.aClass.socket.createMatch().get();
                Map<String, Object> payload = new HashMap<>();
                payload.put("GroupId", group.getId());
                payload.put("MatchID", MainClass.aClass.match.getMatchId());
                Rpc rpcResult = MainClass.aClass.client.rpc(MainClass.aClass.session, "UpdateGroupMetadata", new Gson().toJson(payload, payload.getClass())).get();
                log.debug(rpcResult.toString());
                LobbyWaitingScreen waitingScreen = new LobbyWaitingScreen(group.getId(), groupName, true);
                waitingScreen.displayOutput(terminal);
                return waitingScreen;
            } catch (InterruptedException | ExecutionException e) {
                log.error(e.getMessage());
            }
        } else {
            char c = key.getKeyChar();
            if (letters.indexOf(c) > -1) {
                groupName += c;
            }
            if (key.getKeyChar() == ' ') {
                groupName += '_';
            }

        }
        return this;
    }

    /**
     * Determines if the user's input is currently inside an input field.
     *
     * @return {@code true} if the user's input is inside an input field, {@code false} otherwise
     */
    @Override
    public boolean isInsideInputField() {
        return true;
    }

    /**
     * Indicates that the input has been finished.
     *
     * @return true if the input has been successfully finished, false otherwise
     */
    @Override
    public boolean finishInput() {
        return false;
    }
}
