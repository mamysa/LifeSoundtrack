package ch.usi.inf.gabrialex.service;

import android.content.ContentValues;
import android.location.Location;

import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.List;

import ch.usi.inf.gabrialex.datastructures.EnvironmentContext;
import ch.usi.inf.gabrialex.datastructures.MusicContext;
import ch.usi.inf.gabrialex.db.DBHelper;
import ch.usi.inf.gabrialex.db.dbRankableEntry;

/**
 * Created by alex on 08.12.17.
 */

public class InsertRankableEntryTask implements Runnable {

    private MusicContext musicContext;

    public InsertRankableEntryTask(MusicContext context) {
        this.musicContext = context;
    }

    @Override
    public void run() {
        ArrayList<DateTime> dates = this.musicContext.getDates();
        Location location = this.musicContext.getLocations().get(0);
        int trackID = this.musicContext.getActiveMedia().getId();

        double playtime = computePlaytime();
        double durationRatio = computePlaytimeRatio(playtime);
        double realDurationRatio = computeRealPlaytimeRatio(playtime);
        System.out.println(durationRatio+ " "+realDurationRatio);

        DateTime firstResumed = dates.get(0);
        DateTime lastPaused = dates.get(dates.size()-1);

        ContentValues contentValues = new ContentValues();
        contentValues.put(dbRankableEntry.AUDIO_ID, trackID);
        contentValues.put(dbRankableEntry.DATE_FIRST_RESUME, firstResumed.toString());
        contentValues.put(dbRankableEntry.DATE_LAST_PAUSE, lastPaused.toString());
        contentValues.put(dbRankableEntry.DURATION_FRAC, durationRatio);
        contentValues.put(dbRankableEntry.REAL_DURATION_FRAC, realDurationRatio);
        contentValues.put(dbRankableEntry.LOCATION_LON, location.getLongitude());
        contentValues.put(dbRankableEntry.LOCATION_LAT, location.getLatitude());
        contentValues.put(dbRankableEntry.BIAS, 0);

        DBHelper helper = DBHelper.getInstance(null);
        helper.getWritableDatabase().insert(dbRankableEntry.TABLE_NAME, null, contentValues);
    }

    /**
     * Compute total playtime in seconds.
     * @return
     */
    public double computePlaytime() {

        ArrayList<DateTime> dates = this.musicContext.getDates();
        if (dates.size() % 2 != 0) {
            throw new AssertionError("dates: odd number of elements. This is not supposed to happen!");
        }

        double timeListened = 0;
        for (int i = 1; i < dates.size(); i+=2) {
            DateTime start = dates.get(i-1);
            DateTime end = dates.get(i);
            Period period = new Period(start, end);
            timeListened += (double)period.toStandardSeconds().getSeconds();
        }

        return timeListened;
    }

    /**
     * Compute playtime / track_duration
     * @param playtime
     * @return
     */
    public double computePlaytimeRatio(double playtime) {
        double duration = ((double)this.musicContext.getActiveMedia().getDuration() / 1000.0d); // to seconds
        return playtime/duration;
    }

    /**
     * Compute real listening ratio, i.e listening time / (end time - start time)
     * @param playtime in seconds
     * @return
     */
    public double computeRealPlaytimeRatio(double playtime) {
        // if song has been on pause for longer than 6 hours, disable time ranking for it.
        // fixme timezones?
        final int rankingCutoffThreshold = 6;

        DateTime s = this.musicContext.getStartTimestamp();
        DateTime e = this.musicContext.getEndTimestamp();
        Period diff = new Period(s,e);
        if (diff.toStandardSeconds().getSeconds() == 0 || Math.abs(diff.toStandardHours().getHours()) >= rankingCutoffThreshold) {
            return 0.0d;
        }
        // maybe non-linear function here?
        System.out.println(diff.toStandardSeconds().getSeconds());
        return playtime / (double)diff.toStandardSeconds().getSeconds();
    }
}