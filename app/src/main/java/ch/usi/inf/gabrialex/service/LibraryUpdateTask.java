package ch.usi.inf.gabrialex.service;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.MediaStore;

import ch.usi.inf.gabrialex.db.DBHelper;
import ch.usi.inf.gabrialex.db.DBTableAudio;

/**
 * Created by alex on 01.12.17.
 */

public class LibraryUpdateTask implements Runnable {
    private Context context;
    private ContentResolver contentResolver;

    public LibraryUpdateTask(Context context, ContentResolver resolver) {
        this.context = context;
        this.contentResolver = resolver;
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


        long tracksAdded = 0;
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                ContentValues values = this.convertMediaStoreCursor(cursor, libraryVersion);
                if (this.addOrUpdateEntry(values, libraryVersion))  {
                    tracksAdded++;
                }
                cursor.moveToNext();
            }
            cursor.close();
            long tracksRemoved = this.deleteOldEntries(libraryVersion);
        }

        // TODO call service.onLibraryUpdateComplete(tracksAdded, tracksRemoved) should unblock UI.
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
