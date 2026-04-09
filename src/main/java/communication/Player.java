package communication;

import lombok.Getter;
import lombok.Setter;

/**
 * A Class which stores all relevant Data of a Player
 */
@Getter
@Setter
public class Player implements Comparable<Player> {

    /**
     * This stores the DisplayName of the Player
     */
    private String displayName;

    /**
     * This stores if the Player is the Host of the Lobby
     */
    private boolean isHost;

    /**
     * This stores if the Player is ready
     */
    private boolean isReady;

    /**
     * This stores the Player ID of the Player
     */
    private String playerId;

    /**
     * The Constructor for the {@link Player} Class
     * @param playerId {@link Player#playerId}
     * @param displayName {@link Player#displayName}
     * @param isHost {@link Player#isHost}
     */
    public Player(String playerId, String displayName, boolean isHost) {
        this.displayName = displayName;
        this.isHost = isHost;
        isReady = false;
        this.playerId = playerId;
    }

    @Override
    public int compareTo(Player other) {
        return playerId.compareTo(other.getPlayerId());
    }
}
