package communication;

import dto.GarbageDTO;
import dto.ReadyDTO;
import dto.UpdateBoardStateDTO;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import logic.TetrisField;
import logic.pieces.Tetromino;
import lombok.extern.slf4j.Slf4j;
import screens.LobbyWaitingScreen;
import screens.MainClass;
import screens.PlayOnlineScreen;

/**
 * This Enum stores which Events are being sent and is used to sent and receive them <br><br>
 * List of all Enums: <br>
 * {@link MatchSendHelper#READY} <br>
 * {@link MatchSendHelper#START} <br>
 * {@link MatchSendHelper#SENDGARBAGE} <br>
 * {@link MatchSendHelper#LOOSE} <br>
 * {@link MatchSendHelper#UPDATEBOARD} <br>
 * {@link MatchSendHelper#GAMETICK} <br>
 * {@link MatchSendHelper#UPDATEGARBAGE} <br>
 */
@Slf4j
public enum MatchSendHelper {

    /**
     * READY is sent when either of the two Players are currently in the LobbyWaiting and either are getting ready or unreadying
     */
    READY(1) {
        @Override
        public void sendUpdate(Object... o) {
            String playerid = (String) o[0];
            boolean readyState = (boolean) o[1];
            ReadyDTO payload = new ReadyDTO(playerid, readyState);
            String json = new Gson().toJson(payload, payload.getClass());
            MainClass.aClass.socket.sendMatchData(MainClass.aClass.match.getMatchId(), READY.opcode, json.getBytes());
        }

        @Override
        public void receiveUpdate(String json) {
            ReadyDTO payload = new Gson().fromJson(json, new TypeToken<ReadyDTO>() {
            }.getType());
            LobbyWaitingScreen.updatePlayerState(payload.getPlayer_id(), payload.isReadyState());
        }
    },

    // TODO Implement Starting the Game Logic
    // TODO Counting down from three?
    /**
     * START is sent when the Game finally starts. The Host has to send this
     */
    START(2) {
        @Override
        public void sendUpdate(Object... o) {
            MainClass.aClass.socket.sendMatchData(MainClass.aClass.match.getMatchId(), START.opcode, new byte[0]);
        }

        @Override
        public void receiveUpdate(String json) {
            LobbyWaitingScreen.startGame();
        }
    },

    /**
     * SENDGARBAGE is sent when a Player sends Garbage... Who would've guessed
     */
    SENDGARBAGE(3) {
        @Override
        public void sendUpdate(Object... o) {
            Integer lines = (int) o[0];
            String json = new Gson().toJson(lines, lines.getClass());
            MainClass.aClass.socket.sendMatchData(MainClass.aClass.match.getMatchId(), SENDGARBAGE.opcode, json.getBytes());
        }

        @Override
        public void receiveUpdate(String json) {
            Integer lines = new Gson().fromJson(json, new TypeToken<Integer>() {
            }.getType());
            TetrisField.garbagePieceHandler.addGarbage(lines);
        }
    },
    /**
     * LOOSE is sent when the Player looses the Game
     */
    LOOSE(4) {
        @Override
        public void sendUpdate(Object... o) {
            MainClass.aClass.socket.sendMatchData(MainClass.aClass.match.getMatchId(), LOOSE.opcode, new byte[0]);
        }

        @Override
        public void receiveUpdate(String json) {
            PlayOnlineScreen.win = true;
        }
    },
    /**
     * UPDATEBOARD is called when the Player placed a Piece. It is used to update the Boardstate for the Opponent
     */
    UPDATEBOARD(5) {
        @Override
        public void sendUpdate(Object... o) {
            Tetromino tetromino = (Tetromino) o[0];
            int type = tetromino.getPieceId();
            int rotation = tetromino.getCurrentRotation();
            int x = tetromino.getX();
            int y = tetromino.getY();
            UpdateBoardStateDTO boardState = new UpdateBoardStateDTO(type, x, y, rotation);
            String json = new Gson().toJson(boardState, boardState.getClass());
            MainClass.aClass.socket.sendMatchData(MainClass.aClass.match.getMatchId(), UPDATEBOARD.opcode, json.getBytes());
        }

        @Override
        public void receiveUpdate(String json) {
            UpdateBoardStateDTO boardState = new Gson().fromJson(json, new TypeToken<UpdateBoardStateDTO>() {
            }.getType());
            Tetromino t = boardState.getTetrominoFromBoardState();
            PlayOnlineScreen.opponentTetrisField.addPiece(t);
        }
    },

    /**
     * GAMETICK is sent by the Host and it
     */
    GAMETICK(6) {
        @Override
        public void sendUpdate(Object... o) {
            MainClass.aClass.socket.sendMatchData(MainClass.aClass.match.getMatchId(), GAMETICK.opcode, new byte[0]);
        }

        @Override
        public void receiveUpdate(String json) {
            PlayOnlineScreen.gameTick();
        }
    },
    /**
     * UPDATEGARBAGE is used to update the OpponentBoard so that the Garbage is also displayed on it
     */
    UPDATEGARBAGE(7) {
        @Override
        public void sendUpdate(Object... o) {
            int lines = (int) o[0];
            int garbageGap = (int) o[1];
            GarbageDTO packet = new GarbageDTO(lines, garbageGap);
            String json = new Gson().toJson(packet, packet.getClass());
            MainClass.aClass.socket.sendMatchData(MainClass.aClass.match.getMatchId(), UPDATEGARBAGE.opcode, json.getBytes());

        }

        @Override
        public void receiveUpdate(String json) {
            GarbageDTO received = new Gson().fromJson(json, new TypeToken<GarbageDTO>() {
            }.getType());
            int lines = received.getGarbageHeight();
            int garbageGap = received.getGarbageGap();
            PlayOnlineScreen.opponentTetrisField.addGarbage(lines, garbageGap);
        }
    };

    /**
     * The Opcode is used as an ID to distinguish between the different IDs
     */
    private final int opcode;

    /**
     * The constructor for MatchSendHelper
     * @param opcode {@link MatchSendHelper#opcode}
     */

    MatchSendHelper(int opcode) {
        this.opcode = opcode;
    }

    /**
     * This Method is used to send the different Events {@link MatchSendHelper}
     * @param o An Ellipse of Objects so that everything can be sent
     */
    public abstract void sendUpdate(Object... o);

    /**
     * This Method is used to receive the different Events {@link MatchSendHelper} and is called by {@link MatchSendHelper#receiveUpdate(int, String)}
     * @param json
     */
    public abstract void receiveUpdate(String json);

    /**
     * This Method decides which Event was received depending on the Opcode
     * @param opcode The OPCode that was received
     * @param json The Data that was received
     */
    public static void receiveUpdate(int opcode, String json) {
        log.debug(String.format("Received Opcode %d with Message: \n%s", opcode, json));
        switch (opcode) {
            case 1:
                READY.receiveUpdate(json);
                break;
            case 2:
                START.receiveUpdate(json);
                break;
            case 3:
                SENDGARBAGE.receiveUpdate(json);
                break;
            case 4:
                LOOSE.receiveUpdate(json);
                break;
            case 5:
                UPDATEBOARD.receiveUpdate(json);
                break;
            case 6:
                GAMETICK.receiveUpdate(json);
                break;
            case 7:
                UPDATEGARBAGE.receiveUpdate(json);
        }
    }
}
