package ch.usi.inf.gabrialex.service;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.Period;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import ch.usi.inf.gabrialex.datastructures.EnvironmentContext;
import ch.usi.inf.gabrialex.datastructures.Playlist;
import ch.usi.inf.gabrialex.datastructures.RankWeightPreferences;
import ch.usi.inf.gabrialex.datastructures.RankingReason;
import ch.usi.inf.gabrialex.db.DBHelper;
import ch.usi.inf.gabrialex.db.DBTableAudio;
import ch.usi.inf.gabrialex.db.dbRankableEntry;

/**
 * Created by alex on 01.12.17.
 */

public class PlaylistRankingTask implements Runnable{

    Context context;

    final static public String LOG_FILE_NAME = "rankingDebugLog.txt";
    private boolean LOG_TO_FILE = false; // FIXME all this file logging stuff must go somewhere else!
    private PlaylistUpdateEventListener eventListener;
    FileWriter logFileWriter = null;

    public PlaylistRankingTask(Context context) {
       this.context = context;
    }

    /**
     * Set event listener.
     * @param listener
     */
    public void setEventListener(PlaylistUpdateEventListener listener) {
        this.eventListener = listener;
    }

    @Override
    public void run() {
        if (this.LOG_TO_FILE) {
            this.logFileWriter = this.openDebugLogFile();
        }

        EnvironmentContext envContext = EnvironmentContext.copy();
        DBHelper helper = DBHelper.getInstance(this.context);

        String query = String.format(
            " SELECT * FROM %s t1 INNER JOIN %s t2 ON t1.%s=t2.%s ORDER BY t1.%s ASC;",
            dbRankableEntry.TABLE_NAME, DBTableAudio.TABLE_NAME,
            dbRankableEntry.AUDIO_ID,   DBTableAudio.ID,  dbRankableEntry.AUDIO_ID);

        Cursor cursor = helper.getReadableDatabase().rawQuery(query, null);

        synchronized (Playlist.class) {
            Playlist playlist = Playlist.getInstance();
            playlist.clear();

            if (cursor != null) {
                cursor.moveToFirst();

                Audio audio = null;
                double rank = 0.0d;
                while (!cursor.isAfterLast()) {
                    RankingReason reason = new RankingReason();
                    reason.setEnvironmentContext(envContext);
                    String a = cursor.getString(cursor.getColumnIndex(DBTableAudio.DATA));
                    String b = cursor.getString(cursor.getColumnIndex(DBTableAudio.TRACK));
                    String c = cursor.getString(cursor.getColumnIndex(DBTableAudio.TITLE));
                    String d = cursor.getString(cursor.getColumnIndex(DBTableAudio.ALBUM));
                    String e = cursor.getString(cursor.getColumnIndex(DBTableAudio.ARTIST));
                    String f = cursor.getString(cursor.getColumnIndex(DBTableAudio.DURATION));
                    long g = cursor.getLong(cursor.getColumnIndex(DBTableAudio.ID));

                    if (audio == null || audio.getId() != g) {
                        if (audio != null) {
                            audio.setRank(rank);
                            rank = 0.0d;
                            playlist.addEntry(audio);
                        }

                        audio = new Audio(a,b,c,d,e,Integer.parseInt(f),(int)g);
                    }

                    rank += this.rank(cursor, envContext, reason);
                    if (reason.isSuperImportant()) {
                        audio.addReason(reason);
                    }

                    cursor.moveToNext();
                }
                cursor.close();
            }

            playlist.onPlaylistUpdated();
        }

        if (this.LOG_TO_FILE) {
            this.closeDebugLogFile();
        }

        if (this.eventListener != null) {
            this.eventListener.onPlaylistUpdated();
        }
    }

    private double rank(Cursor cursor, EnvironmentContext environmentContext, RankingReason reason) {
        double entryRank = 0.0;

        String a = cursor.getString(cursor.getColumnIndex(DBTableAudio.DATA));

        if (LOG_TO_FILE) {
            String debugStr = String.format("Begin ranking row %s:\n", a);
            this.appendToDebugLogFile(debugStr);
        }

        double playtimeRatio = this.computePlaytimeRatio(cursor, reason);
        double realPlaytimeRatio = this.computeRealPlaytimeRatio(cursor);
        double freshess = this.computeFreshness(cursor, environmentContext, reason);
        double bias = getBias(cursor);
        if (bias != 0.0) {
            playtimeRatio = 1.0;
        }
        entryRank += realPlaytimeRatio * RankWeightPreferences.IMPORTANCE_TIME * rankTime(cursor, environmentContext, reason);
        entryRank += RankWeightPreferences.IMPORTANCE_LOCATION * rankLocation(cursor, environmentContext, reason);
        entryRank += RankWeightPreferences.IMPORTANCE_MOOD * rankMood(cursor, environmentContext, reason);
        entryRank += RankWeightPreferences.IMPORTANCE_WEATHER * rankWeather(cursor, environmentContext, reason);

        entryRank = (playtimeRatio*freshess) * (entryRank + getBias(cursor));

        if (LOG_TO_FILE) {
            String debugStr = String.format("Final row rank: %s, w1=%s, w2=%s, w3=%s, w4=%s\n\n", entryRank,
                    RankWeightPreferences.IMPORTANCE_TIME,
                    RankWeightPreferences.IMPORTANCE_LOCATION,
                    RankWeightPreferences.IMPORTANCE_MOOD,
                    RankWeightPreferences.IMPORTANCE_WEATHER);
            this.appendToDebugLogFile(debugStr);
        }
        return entryRank;
    }

    final static int OUT_FRAME_HOUR = 1;

    /**
     * rankMood.
     * @param cursor
     * @param environmentContext
     * @return
     */
    private double rankMood(Cursor cursor, EnvironmentContext environmentContext, RankingReason reason) {
        String mood = cursor.getString(cursor.getColumnIndex(dbRankableEntry.MOOD));
        String envMood = environmentContext.getMood();
        double moodRank;
        if(mood != null && mood.equals(envMood)){
            moodRank = 1.0;
        }
        else {
            moodRank = 0.0;
        }

        reason.setMoodInfo(mood, moodRank);
        if (LOG_TO_FILE) {
            String debugStr = String.format("rankMood(): environmentMood=%s, entryMood=%s, timeRank=%s\n",
                    envMood, mood, moodRank);
            this.appendToDebugLogFile(debugStr);
        }
        return moodRank;
    }

    /**
     * rankWeather.
     * @param cursor
     * @param environmentContext
     * @return
     */
    private double rankWeather(Cursor cursor, EnvironmentContext environmentContext, RankingReason reason) {
        String weather = cursor.getString(cursor.getColumnIndex(dbRankableEntry.WEATHER));
        String envWeather = environmentContext.getWeather();
        double weatherRank;
        if(weather != null && weather.equals(envWeather)){
            weatherRank = 1.0;
        }
        else {
            weatherRank = 0.0;
        }

        reason.setWeatherInfo(weather, weatherRank);
        if (LOG_TO_FILE) {
            String debugStr = String.format("rankWeather(): environmentWeather=%s, entryWeather=%s, weatherRank=%s\n",
                    envWeather, weather, weatherRank);
            this.appendToDebugLogFile(debugStr);
        }
        return weatherRank;
    }

    final int MAX_RANK_RADIUS = 300;

    /**
     * Rank location.
     * @param cursor
     */
    private double rankLocation(Cursor cursor, EnvironmentContext context, RankingReason reason) {
        final double eps = 0.5;
        double lon = cursor.getDouble(cursor.getColumnIndex(dbRankableEntry.LOCATION_LON));
        double lat = cursor.getDouble(cursor.getColumnIndex(dbRankableEntry.LOCATION_LAT));
        Location location = new Location("");
        location.setLongitude(lon);
        location.setLatitude(lat);

        Location envLocation = context.getLastLocation();

        // do not perform ranking when location info is missing!
        if ((Double.isInfinite(lon) && Double.isInfinite(lat)) ||
            (Double.isInfinite(envLocation.getLongitude()) && Double.isInfinite(envLocation.getLatitude()))) {
            reason.setLocationInfo(location, 0.0);
            if (LOG_TO_FILE) {
                this.appendToDebugLogFile("rankLocation: disabled\n");
            }
            return 0.0;
        }

        int distance = (int)location.distanceTo(envLocation); // in meters
        double rank;
        if (distance == 0 || distance < MAX_RANK_RADIUS) {
            rank = 1.0;
        }
        else {
            rank = 1.0/(double)distance; // todo smoother function?
        }

        reason.setLocationInfo(location, rank);
        if (LOG_TO_FILE){
            String debugStr = String.format("rankLocation: envLon=%s, envLat=%s, lon=%s, lat=%s, dist=%s, locationRank=%s\n",
                envLocation.getLongitude(), envLocation.getLatitude(),
                   location.getLongitude(),    location.getLatitude(), distance, rank);
            this.appendToDebugLogFile(debugStr);
        }
        return rank;
    }

    /**
     * rankTime wrapper.
     * @param cursor
     * @param environmentContext
     * @return
     */
    private double rankTime(Cursor cursor, EnvironmentContext environmentContext, RankingReason reason) {
        String firstResumeStr = cursor.getString(cursor.getColumnIndex(dbRankableEntry.DATE_FIRST_RESUME));
        String lastPauseStr = cursor.getString(cursor.getColumnIndex(dbRankableEntry.DATE_LAST_PAUSE));

        DateTime firstResume = DateTime.parse(firstResumeStr);
        DateTime lastPause = DateTime.parse(lastPauseStr);
        DateTime currentDatetime = environmentContext.getDateTime();
        double timeRank = this.rankTime(new LocalTime(firstResume),
                                        new LocalTime(lastPause),
                                        new LocalTime(currentDatetime));
        reason.setTimeInfo(firstResume, lastPause, timeRank);
        if (LOG_TO_FILE) {
            String debugStr = String.format("rankTime(): start=%s, end=%s, current=%s, timeRank=%s\n",
                    new LocalTime(firstResume), new LocalTime(lastPause), new LocalTime(currentDatetime), timeRank);
            this.appendToDebugLogFile(debugStr);
        }
        return timeRank;
    }

    /**
     * For now we are going to disregard timezones and compare local times directly - in order to avoid
     * surprises when moving across timezones.
     * (1) if inside [start, end] interval, return 1;
     * (2) if not inside, check intervals [start-2h, start] and [end, end+2h].
     * @param start time when user starts listening to the song
     * @param end time when user stops listening to the song.
     * @param now now
     */
    private double rankTime(LocalTime start, LocalTime end, LocalTime now) {
        if (this.insideInterval(start, end, now) != -1.0) {
            return 1.0;
        }

        // otherwise lookup
        LocalTime beforeStart = start.minus(new Period(OUT_FRAME_HOUR, 0, 0, 0));
        LocalTime afterEnd = end.plus(new Period(OUT_FRAME_HOUR, 0, 0, 0));

        double insideStart = this.insideInterval(beforeStart, start, now);
        double insideEnd = this.insideInterval(end, afterEnd, now);

        if (insideStart != -1.0) {
            return insideStart;
        }
        if (insideEnd != -1.0) {
            return 1.0 - insideEnd;
        }
        return 0.0;
    }

    /**
     * (1) if interval contains midnight, check sub-intervals [a, midnight] and [midnight, b]
     * (2) otherwise, check [a, b].
     * @param a start
     * @param b end
     * @param n current time
     * @return how far into [a,b] n is. return number in [0.0,1.0] if n is inside, otherwise -1.0;
     */
    private double insideInterval(LocalTime a, LocalTime b, LocalTime n) {
        if (a.isAfter(b)) {
            LocalTime midnight1 = new LocalTime(23, 59, 59, 999);
            LocalTime midnight2 = new LocalTime(0, 0, 0, 0);

            boolean nea = n.isEqual(a);
            boolean neb = n.isEqual(b);
            boolean nm1 = n.isEqual(midnight1);
            boolean nm2 = n.isEqual(midnight2);

            boolean i1 = (n.isAfter(a) || nea) && (n.isBefore(midnight1) || nm1);
            boolean i2 = (n.isAfter(midnight2) || nm2) && (n.isBefore(b) || neb);

            // compute total interval length
            Period AtoM1 = new Period(a, midnight1);
            Period M2toB = new Period(midnight2, b);
            double AtoM1Mins = (double)(AtoM1.toStandardMinutes().getMinutes());
            double M2toBMins = (double)(M2toB.toStandardMinutes().getMinutes());
            double total = AtoM1Mins + M2toBMins;
            if (i1) {
                // n is in the first sub-interval;
                Period AtoN = new Period(a, n);
                return (double)AtoN.toStandardMinutes().getMinutes() / total;
            }

            if (i2) {
                // n is in the second sub-interval
                Period M2toN = new Period(midnight2, n);
                return (AtoM1Mins + (double)M2toN.toStandardMinutes().getMinutes()) / total;
            }

            return -1.0;
        }

        // inside interval
        if ((n.isAfter(a) || n.isEqual(a)) && (n.isBefore(b) || n.isEqual(b))) {
            Period aTob = new Period(a, b);
            Period aTon = new Period(a, n);
            return (double)aTon.toStandardMinutes().getMinutes() / (double)aTob.toStandardMinutes().getMinutes();
        }

        return -1.0;
    }

    /**
     * Compute playtime / track_duration
     * @param cursor
     * @return
     */
    private double computePlaytimeRatio(Cursor cursor, RankingReason reason) {
        String durationStr = cursor.getString(cursor.getColumnIndex(DBTableAudio.DURATION));
        double duration = (double)Integer.parseInt(durationStr) / 1000.0d;
        double playtime = cursor.getDouble(cursor.getColumnIndex(dbRankableEntry.LISTENING_DURATION));
        reason.setDuration(playtime);

        double ratio = playtime / duration;

        if (LOG_TO_FILE) {
            String debugStr = String.format("computePlaytimeRatio(): playtime=%s, duration=%s, ratio=%s\n",
                    playtime, duration, ratio);
            this.appendToDebugLogFile(debugStr);
        }
        return ratio;
    }

    /**
     * Compute real listening ratio, i.e listening time / (end time - start time)
     * @param cursor
     */
    private double computeRealPlaytimeRatio(Cursor cursor) {
        // if song has been on pause for longer than 6 hours, disable time ranking for it.
        // fixme timezones?
        final int rankingCutoffThreshold = 6;

        double playtime = cursor.getDouble(cursor.getColumnIndex(dbRankableEntry.LISTENING_DURATION));
        String switchTo = cursor.getString(cursor.getColumnIndex(dbRankableEntry.DATE_PLAYER_SWITCH_TO));
        String switchFrom = cursor.getString(cursor.getColumnIndex(dbRankableEntry.DATE_PLAYER_SWITCH_FROM));

        DateTime s = DateTime.parse(switchTo);
        DateTime e = DateTime.parse(switchFrom);
        Period diff = new Period(s,e);
        // FIXME rankingCutoffThreshold is wrong!
        if (diff.toStandardSeconds().getSeconds() == 0 || Math.abs(diff.toStandardHours().getHours()) >= rankingCutoffThreshold) {
            if (LOG_TO_FILE) {
                String debugStr = String.format("computeRealPlaytimeRatio(): switchTo=%s, switchFrom=%s, diff=%s, ratio=%s\n",
                    switchTo, switchFrom, diff, 0.0);
                this.appendToDebugLogFile(debugStr);
            }
            return 0.0d;
        }

        double ratio = playtime / (double)diff.toStandardSeconds().getSeconds();
        // maybe non-linear function here?
        if (LOG_TO_FILE) {
            String debugStr = String.format("computeRealPlaytimeRatio(): switchTo=%s, switchFrom=%s, diff=%s, ratio=%s\n",
                    switchTo, switchFrom, diff, ratio);
            this.appendToDebugLogFile(debugStr);
        }
        return ratio;
    }

    /**
     * Compute "freshness" of the song. Older entries should contribute less to the rank of the track.
     * This is done by counting days between now and the date the track was played on.
     */
    private double computeFreshness(Cursor cursor, EnvironmentContext context, RankingReason reason) {
        String firstResumeStr = cursor.getString(cursor.getColumnIndex(dbRankableEntry.DATE_FIRST_RESUME));
        DateTime start = DateTime.parse(firstResumeStr);
        DateTime now = context.getDateTime();
        Period period = new Period(start, now);
        int days = period.toStandardDays().getDays();
        double freshness = (days == 0) ? 1.0 : 1.0/(double)days;
        reason.setFreshness(freshness);
        return freshness;
    }

    /**
     * Read initial bias.
     * @param cursor
     * @return
     */
    private double getBias(Cursor cursor) {
        double bias = cursor.getDouble(cursor.getColumnIndex(dbRankableEntry.BIAS));
        if (LOG_TO_FILE) {
            String debugStr = String.format("getBias: bias=%s\n", bias);
            this.appendToDebugLogFile(debugStr);
        }

        return bias;
    }

    /**
     * Helper method for opening log file. If opening log file fails, disable logging.
     * @return
     */
    private FileWriter openDebugLogFile() {
        File file = new File(context.getFilesDir(), LOG_FILE_NAME);
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(file, false);
        }
        catch (IOException ex) {
            this.LOG_TO_FILE = false;
            Log.e("openDebugLogFile", "error opening logfile, disabling logging");
        }

        return fileWriter;
    }

    private void closeDebugLogFile() {
        try {
            logFileWriter.close();
        }
        catch (IOException ex) {
            Log.e("closeDebugLogFile", "error closing logfile");
        }
    }

    private void appendToDebugLogFile(String s) {
        try {
            this.logFileWriter.append(s);
        }
        catch (IOException ex) {
            Log.e("appendToDebugLogFile", "error appending to logfile");
        }

    }
}
