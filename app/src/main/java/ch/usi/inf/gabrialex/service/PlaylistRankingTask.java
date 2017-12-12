package ch.usi.inf.gabrialex.service;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.IntegerRes;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.Period;

import ch.usi.inf.gabrialex.datastructures.Playlist;
import ch.usi.inf.gabrialex.db.DBHelper;
import ch.usi.inf.gabrialex.db.DBTableAudio;
import ch.usi.inf.gabrialex.db.dbRankableEntry;

/**
 * Created by alex on 01.12.17.
 */

public class PlaylistRankingTask implements Runnable{

    Context context;

    public PlaylistRankingTask(Context context) {
       this.context = context;
    }


    @Override
    public void run() {
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
                while (!cursor.isAfterLast()) {
                    String a = cursor.getString(cursor.getColumnIndex(DBTableAudio.DATA));
                    String b = cursor.getString(cursor.getColumnIndex(DBTableAudio.TRACK));
                    String c = cursor.getString(cursor.getColumnIndex(DBTableAudio.TITLE));
                    String d = cursor.getString(cursor.getColumnIndex(DBTableAudio.ALBUM));
                    String e = cursor.getString(cursor.getColumnIndex(DBTableAudio.ARTIST));
                    String f = cursor.getString(cursor.getColumnIndex(DBTableAudio.DURATION));
                    long g = cursor.getLong(cursor.getColumnIndex(DBTableAudio.ID));

                    //TODO ranking here
                    //FIXME fix duplicate audio entries!
                    this.rank(cursor);
                    Audio audio = new Audio(a,b,c,d,e,Integer.parseInt(f),(int)g);
                    playlist.addEntry(audio);
                    cursor.moveToNext();


                    // FIXME maybe cache the playlist in the db so that user is not presented with
                    // empty view when application starts every time?
                }
                cursor.close();
            }
        }


    }

    private void rank(Cursor cursor) {
        String a = cursor.getString(cursor.getColumnIndex(DBTableAudio.DATA));
        double playtimeRatio = this.computePlaytimeRatio(cursor);
        double realPlaytimeRatio = this.computeRealPlaytimeRatio(cursor);
        System.out.println(a + " " + playtimeRatio + " " + realPlaytimeRatio);
    }

    final static int OUT_FRAME_HOUR = 1;

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
        LocalTime beforeStart = start.minus(new Period(-OUT_FRAME_HOUR, 0, 0, 0));
        LocalTime afterEnd = end.plus(new Period(+OUT_FRAME_HOUR, 0, 0, 0));

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
    private double computePlaytimeRatio(Cursor cursor) {
        String durationStr = cursor.getString(cursor.getColumnIndex(DBTableAudio.DURATION));
        double duration = (double)Integer.parseInt(durationStr) / 1000.0d;
        double playtime = cursor.getDouble(cursor.getColumnIndex(dbRankableEntry.LISTENING_DURATION));
        return playtime/duration;
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
        String switchFrom = cursor.getString(cursor.getColumnIndex(dbRankableEntry.DATE_PLAYER_SWITCH_TO));
        String switchTo = cursor.getString(cursor.getColumnIndex(dbRankableEntry.DATE_PLAYER_SWITCH_FROM));

        DateTime s = DateTime.parse(switchFrom);
        DateTime e = DateTime.parse(switchTo);
        Period diff = new Period(s,e);
        if (diff.toStandardSeconds().getSeconds() == 0 || Math.abs(diff.toStandardHours().getHours()) >= rankingCutoffThreshold) {
            return 0.0d;
        }
        // maybe non-linear function here?
        return playtime / (double)diff.toStandardSeconds().getSeconds();
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
