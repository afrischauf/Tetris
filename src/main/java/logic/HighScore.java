package logic;

/**
 * This Class stores a Highscore for easier writing/displaying of the Score
 * It also implements Comparable so that it can be compared to other HighScore variables
 */
public class HighScore implements Comparable<HighScore> {

    /**
     * The name of the Player
     */
    private final String name;

    /**
     * The Level that the Player achieved
     */
    private final int level;

    /**
     * The Score that the Player achieved
     */
    private final long score;

    /**
     * How long the Game was
     */
    private final String time;

    /**
     * Creates a new HighScore object with the given parameters.
     *
     * @param name  the name of the player
     * @param level the level achieved by the player
     * @param score the score achieved by the player
     * @param time  the duration of the game
     */
    public HighScore(String name, int level, long score, String time) {
        this.name = name;
        this.level = level;
        this.score = score;
        this.time = time;
    }

    @Override
    public int compareTo(HighScore o) {
        if (score != o.score)
            return Long.compare(score, o.score);
        if (level != o.level)
            return Integer.compare(level, o.level);
        if (!time.equals(o.time))
            return time.compareTo(o.time);
        return name.compareTo(o.name);
    }

    @Override
    public String toString() {
        return String.format("%16s|   %2d  |%07d| %s ", StringUtils.center(name, 16, ' '), level, score, time);
    }
}
