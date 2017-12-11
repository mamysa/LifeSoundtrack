package ch.usi.inf.gabrialex.service;

import android.content.Context;
import android.database.Cursor;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.Period;

import ch.usi.inf.gabrialex.datastructures.Playlist;
import ch.usi.inf.gabrialex.db.DBHelper;
import ch.usi.inf.gabrialex.db.DBTableAudio;

/**
 * Created by alex on 01.12.17.
 */

public class PlaylistRankingTask implements Runnable{

    Context context;

    public PlaylistRankingTask(Context context) {
       this.context = context;
    }

    /**
     * Don't do any ranking for now, just return the playlist.
     */
    @Override
    public void run() {
        DBHelper helper = DBHelper.getInstance(this.context);
        Cursor cursor = helper.getReadableDatabase()
                              .rawQuery("SELECT * FROM Tracks;", null);
        synchronized (Playlist.class) {
            Playlist playlist = Playlist.getInstance();
            if (cursor != null) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    String a = cursor.getString(cursor.getColumnIndex(DBTableAudio.DATA));
                    String b = cursor.getString(cursor.getColumnIndex(DBTableAudio.TRACK));
                    String c = cursor.getString(cursor.getColumnIndex(DBTableAudio.TITLE));
                    String d = cursor.getString(cursor.getColumnIndex(DBTableAudio.ALBUM));
                    String e = cursor.getString(cursor.getColumnIndex(DBTableAudio.ARTIST));
                    String f = cursor.getString(cursor.getColumnIndex(DBTableAudio.DURATION));
                    long g = cursor.getLong(cursor.getColumnIndex(DBTableAudio.ID));

                    Audio audio = new Audio(a,b,c,d,e,Integer.parseInt(f),(int)g);
                    playlist.addEntry(audio);

                    cursor.moveToNext();
                }
                cursor.close();
            }
        }
    }


    /**
     * (1) realPlaytimeFrac (which is track_playtime / (player_switch_from_time - player_switch_to_time))
     * determines weighting of time. Tracks that have been paused for longer periods of times should
     * be ranked lower.
     * (i.e. realPlaytimeFrac * rankTime())
     */
    private void  rank() {


    }


    /**
     * For now we are going to disregard timezones and compare local times directly - in order to avoid
     * surprizes when moving across timezones...
     * (1) if (end time - start time) < threshold -> return 0
     * (2) otherwise, find out if current time is inside (start, end) interval -> return 1
     * (3)
     * @param start time when user starts listening to the song
     * @param end time when user stops listening to the song.
     * @param now now
     */
    private void rankTime(LocalTime start, LocalTime end, LocalTime now) {

    }

    /**
     * Compute "freshness" of the song. Older entries should contribute less to the rank of the track.
     * This is done by counting days between now and the date the track was played on.
     * @param start
     * @param now
     */
    private double computeFreshness(DateTime start, DateTime now) {
        Period period = new Period(start, now);
        int days = period.toStandardDays().getDays();
        return (days == 0) ? 1.0 : 1.0/(double)days;
    }
}
