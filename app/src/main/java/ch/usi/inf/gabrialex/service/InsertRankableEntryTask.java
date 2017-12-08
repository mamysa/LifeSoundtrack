package ch.usi.inf.gabrialex.service;

import android.content.ContentValues;
import android.location.Location;

import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.ArrayList;

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

        double durationRatio = computePlaytimeRatio();
        DateTime firstResumed = dates.get(0);
        DateTime lastPaused = dates.get(dates.size()-1);

        ContentValues contentValues = new ContentValues();
        contentValues.put(dbRankableEntry.AUDIO_ID, trackID);
        contentValues.put(dbRankableEntry.DATE_FIRST_RESUME, firstResumed.toString());
        contentValues.put(dbRankableEntry.DATE_LAST_PAUSE, lastPaused.toString());
        contentValues.put(dbRankableEntry.DURATION_FRAC, durationRatio);
        contentValues.put(dbRankableEntry.LOCATION_LON, location.getLongitude());
        contentValues.put(dbRankableEntry.LOCATION_LAT, location.getLatitude());
        contentValues.put(dbRankableEntry.BIAS, 0);

        DBHelper helper = DBHelper.getInstance(null);
        helper.getWritableDatabase().insert(dbRankableEntry.TABLE_NAME, null, contentValues);
    }

    public double computePlaytimeRatio() {
        double duration = ((double)this.musicContext.getActiveMedia().getDuration() / 1000.0d); // to seconds

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

        System.out.println(timeListened + " " + duration);
        return timeListened / duration;
    }
}
