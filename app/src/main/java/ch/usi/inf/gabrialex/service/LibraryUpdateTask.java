package ch.usi.inf.gabrialex.service;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.method.DateTimeKeyListener;
import android.util.Log;

import org.joda.time.DateTime;

import java.util.ArrayList;

import ch.usi.inf.gabrialex.datastructures.EnvironmentContext;
import ch.usi.inf.gabrialex.db.DBHelper;
import ch.usi.inf.gabrialex.db.DBTableAudio;
import ch.usi.inf.gabrialex.db.dbRankableEntry;
import ch.usi.inf.gabrialex.musicplayer2.LibraryUpdateEventListener;

/**
 * Created by alex on 01.12.17.
 */

public class LibraryUpdateTask implements Runnable {
    private Context context;
    private ContentResolver contentResolver;
    private LibraryUpdateEventListener eventListener;

    public LibraryUpdateTask(Context context, ContentResolver resolver) {
        this.context = context;
        this.contentResolver = resolver;
    }

    public void setEventListener(LibraryUpdateEventListener listener) {
        this.eventListener = listener;
    }

    /**
     * Locates music in MediaStore and stores into our local database table Tracks. We need to do
     * this in order to detect newly added tracks and detect removed tracks that are no longer
     * useful for ranking. This method has the following behaviour:
     * (1) Get current library version. Initially it is set to 0. Each row in Tracks table has
     * this property.
     * (2) Iterate over media store.
     *  -> If file is not present in Tracks table, add it to table.
     *  -> If file is present in Tracks table, update its library version to latest.
     * (3) Any file with older library version is no longer in MediaStore and they
     * should be deleted.
     * (4) Newly added tracks should have an entry added to dbRankableEntry to be able to
     * compute initial rankings.
     */
    @Override
    public void run() {
        System.out.println("RUNNING THREAD!");
        //FIXME do we check permissions here?
        long libraryVersion = this.getLibraryVersion();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = this.contentResolver.query(uri, null, selection, null, sortOrder);


        ArrayList<String> tracksAdded = new ArrayList<>();
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                ContentValues values = this.convertMediaStoreCursor(cursor, libraryVersion);
                if (this.addOrUpdateEntry(values, libraryVersion))  {
                    tracksAdded.add((String)values.get(DBTableAudio.DATA));
                }
                cursor.moveToNext();
            }
            cursor.close();

            for (String track: tracksAdded) {
                addRankedEntries(track);
            }

            deleteOldEntries(libraryVersion);
        }

        Log.e("Ok", "LibraryUpdateTask done!");

        long tracksRemovedQty = this.deleteOldEntries(libraryVersion);
        long trackSAddedQty = tracksAdded.size();

        if (this.eventListener != null) {
            this.eventListener.onLibraryUpdateComplete((int)trackSAddedQty, (int)tracksRemovedQty);
        }
    }

    /**
     * Get library version. If Tracks table is empty, default to 0.
     * @return
     */
    private long getLibraryVersion() {
        long libraryVersion = 0;
        DBHelper helper = DBHelper.getInstance(this.context);
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT " + DBTableAudio.LIFETIME
                                   + " FROM " + DBTableAudio.TABLE_NAME, null);
        if (cursor != null) {
            if (cursor.getCount() != 0) {
                cursor.moveToFirst();
                libraryVersion = cursor.getLong(cursor.getColumnIndex(DBTableAudio.LIFETIME)) + 1;
            }
            cursor.close();
        }

        return libraryVersion;
    }

    /**
     * Add track to Tracks table if it doesn't exist, otherwise update its libraryVersion (i.e. lifetime)
     * @param values
     * @param libraryVersion
     * @return true if given media is new, false otherwise
     */
    private boolean addOrUpdateEntry(ContentValues values, long libraryVersion) {
        boolean newlyAdded = false;
        DBHelper helper = DBHelper.getInstance(this.context);

        String data = (String)values.get(DBTableAudio.DATA);
        Cursor cursor = helper.getReadableDatabase()
                              .rawQuery("SELECT _id FROM Tracks WHERE data=?", new String[]{ data });
        if (cursor != null) {
            if (cursor.getCount() == 0) {
                helper.getWritableDatabase().insert("Tracks", null, values);
                newlyAdded = true;
            }
            else {
                helper.getWritableDatabase()
                      .execSQL("UPDATE " + DBTableAudio.TABLE_NAME + " SET "
                                         + DBTableAudio.LIFETIME + "=? WHERE "
                                         + DBTableAudio.DATA + "=?;", new String[]{ ""+libraryVersion, data });
            }
            cursor.close();
        }
        return newlyAdded;
    }

    /**
     * DELETE statement to remove each row with library version less than current library version
     * @param libraryVersion
     * @return number of rows affected
     */
    private long deleteOldEntries(long libraryVersion) {
        DBHelper helper = DBHelper.getInstance(this.context);
        helper.getWritableDatabase()
              .execSQL("DELETE FROM " + DBTableAudio.TABLE_NAME + " WHERE "
                                      + DBTableAudio.LIFETIME + "<?;", new String[] { ""+libraryVersion });
        return DatabaseUtils.longForQuery(helper.getWritableDatabase(), "SELECT changes()", null);
    }

    /**
     * For each newly inserted track, add entry to RankableEntry table with random bias and
     * default values.
     */
    private void addRankedEntries(String trackData) {
        DBHelper helper = DBHelper.getInstance(this.context);
        Cursor cursor = helper.getReadableDatabase().rawQuery(
                "SELECT " + DBTableAudio.ID
              + " FROM "  + DBTableAudio.TABLE_NAME
              + " WHERE " + DBTableAudio.DATA+"=?;", new String[]{ trackData });
        if (cursor != null) {
            cursor.moveToFirst();

            Location  location = EnvironmentContext.LOCATION_DEFAULT_VALUE;
            String dateTimeStr = EnvironmentContext.DATETIME_DEFAULT_VALUE.toString();

            while (!cursor.isAfterLast()) {
                long trackID = cursor.getLong(cursor.getColumnIndex(DBTableAudio.ID));

                ContentValues contentValues = new ContentValues();
                contentValues.put(dbRankableEntry.AUDIO_ID, trackID);
                contentValues.put(dbRankableEntry.DATE_FIRST_RESUME, dateTimeStr);
                contentValues.put(dbRankableEntry.DATE_LAST_PAUSE, dateTimeStr);
                contentValues.put(dbRankableEntry.DATE_PLAYER_SWITCH_TO, dateTimeStr);
                contentValues.put(dbRankableEntry.DATE_PLAYER_SWITCH_FROM, dateTimeStr);
                contentValues.put(dbRankableEntry.LISTENING_DURATION, 0);
                contentValues.put(dbRankableEntry.LOCATION_LON, location.getLongitude());
                contentValues.put(dbRankableEntry.LOCATION_LAT, location.getLatitude());
                contentValues.put(dbRankableEntry.BIAS, Math.random());
                helper.getWritableDatabase().insert(dbRankableEntry.TABLE_NAME, null, contentValues);

                cursor.moveToNext();
            }

            cursor.close();
        }
    }

    /**
     * Convert cursor to row in mediastore to contentvalues suitable for insertion
     * into Tracks table.
     * @param cursor to the MediaStore
     * @return ContentValues
     */
    private ContentValues convertMediaStoreCursor(Cursor cursor, long libraryVersion) {
        String a = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
        String b = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TRACK));
        String c = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
        String d = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
        String e = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
        String f = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));

        ContentValues values = new ContentValues();
        values.put(DBTableAudio.DATA,  a);
        values.put(DBTableAudio.TRACK, b);
        values.put(DBTableAudio.TITLE, c);
        values.put(DBTableAudio.ALBUM, d);
        values.put(DBTableAudio.ARTIST, e);
        values.put(DBTableAudio.DURATION, f);
        values.put(DBTableAudio.LIFETIME, libraryVersion);
        return values;
    }
}
