package ch.usi.inf.gabrialex.protocol;

/**
 * Created by alex on 17.11.17.
 */

public class Protocol {
    /**
     * Request playlist from the service
     */
    public final static String REQUEST_SONG_LISTING = "REQUEST_SONG_LISTING";

    /**
     * Playlist response from the service
     */
    public final static String RESPONSE_SONG_LISTING = "RESPONSE_SONG_LISTING";

    /**
     * Move to the next track.
     */
    public final static String PLAYER_NEXT = "PLAYER_NEXT";

    /**
     * Move to the previous track.
     */
    public final static String PLAYER_PREV = "PLAYER_PREV";

    /**
     * Pick track from playlist
     */
    public final static String PLAYER_SETTRACK = "PLAYER_SETTRACK";

    /**
     * Start the player.
     */
    public final static String PLAYER_RESUME  = "PLAYER_RESUME";


    /**
     * Toggle player
     */
    public final static String PLAYER_TOGGLE = "PLAYER_TOGGLE";

    /**
     * Pause the player.
     */
    public final static String PLAYER_PAUSE = "PLAYER_PAUSE";

    /**
     * Media player picks new track for playback.
     */
    public final static String PLAYER_NEWTRACK_SELECTED = "PLAYER_NEWTRACKSELECTED";

    /**
     * PLAYER_PLAYBACK_POSITION
     */
    public final static String PLAYER_PLAYBACK_POSITION_UPDATE = "PLAYER_PLAYBACK_POSITION_UPDATE";
    public final static String PLAYER_PLAYBACK_POSITION_DATA = "PLAYER_PLAYBACK_POSITION_DATA";
    public final static String PLAYER_PLAYBACK_DURATION_DATA = "PLAYER_PLAYBACK_DURATION_DATA";
}
