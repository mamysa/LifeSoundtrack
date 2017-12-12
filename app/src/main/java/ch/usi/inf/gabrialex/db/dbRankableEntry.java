package ch.usi.inf.gabrialex.db;

/**
 * Created by alex on 08.12.17.
 */

public class dbRankableEntry {
    public static final String TABLE_NAME = "RankableEntries";
    public static final String AUDIO_ID = "audioId";
    public static final String DATE_FIRST_RESUME = "dateLirstResume";
    public static final String DATE_LAST_PAUSE = "dateLastPause";
    public static final String DATE_PLAYER_SWITCH_TO = "datePlayerSwitchTo";
    public static final String DATE_PLAYER_SWITCH_FROM = "datePlayerSwitchFrom";
    public static final String LISTENING_DURATION = "listeningDuration";
    public static final String LOCATION_LON = "locationLon";
    public static final String LOCATION_LAT = "locationLat";
    public static final String BIAS = "bias";

    static final String CREATE_TABLE_STMT = String.format(
        "CREATE TABLE IF NOT EXISTS %s(" +
        "%s INTEGER, " + // audio id
        "%s TEXT, " + // date 1st resume
        "%s TEXT, " + // date last pause
        "%s TEXT, " + // date switch to
        "%s TEXT, " + // date switch from
        "%s REAL, " + // listening duration
        "%s REAL, " + // longitude
        "%s REAL, " + // latitude
        "%s REAL, " + // bias, used for to give some ranking to newly inserted tracks.
        "FOREIGN KEY(%s) REFERENCES %s(%s));",  // AUDIO_ID;
        TABLE_NAME,
        AUDIO_ID,
        DATE_FIRST_RESUME,
        DATE_LAST_PAUSE,
        DATE_PLAYER_SWITCH_TO,
        DATE_PLAYER_SWITCH_FROM,
        LISTENING_DURATION,
        LOCATION_LON,
        LOCATION_LAT,
        BIAS,
        AUDIO_ID, DBTableAudio.TABLE_NAME, DBTableAudio.ID
    );
}
