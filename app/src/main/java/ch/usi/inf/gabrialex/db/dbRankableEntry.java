package ch.usi.inf.gabrialex.db;

/**
 * Created by alex on 08.12.17.
 */

public class dbRankableEntry {
    public static final String TABLE_NAME = "RankableEntries";
    public static final String AUDIO_ID = "audioId";
    public static final String DATE_FIRST_RESUME = "dateLirstResume";
    public static final String DATE_LAST_PAUSE = "dateLastPause";
    public static final String DURATION_FRAC = "durationFrac";
    public static final String REAL_DURATION_FRAC = "realDurationFrac";
    public static final String LOCATION_LON = "locationLon";
    public static final String LOCATION_LAT = "locationLat";
    public static final String BIAS = "bias";

    static final String CREATE_TABLE_STMT = String.format(
        "CREATE TABLE IF NOT EXISTS %s(" +
        "%s INTEGER, " + // audio id
        "%s TEXT, " + // date 1st resume
        "%s TEXT, " + // date last pause
        "%s REAL, " + // listening duration divided by track length, in seconds
        "%s REAL, " + // listening duration divided by (DATE_LAST_PAUSE- DATE_FIRST_RESUME), in seconds
        "%s REAL, " + // longitude
        "%s REAL, " + // latitude
        "%s REAL, " + // bias, used for to give some ranking to newly inserted tracks.
        "FOREIGN KEY(%s) REFERENCES %s(%s));",  // AUDIO_ID;
        TABLE_NAME,
        AUDIO_ID,
        DATE_FIRST_RESUME,
        DATE_LAST_PAUSE,
        DURATION_FRAC,
        REAL_DURATION_FRAC,
        LOCATION_LON,
        LOCATION_LAT,
        BIAS,
        AUDIO_ID, DBTableAudio.TABLE_NAME, DBTableAudio.ID
    );
}
