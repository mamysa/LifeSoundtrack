package ch.usi.inf.gabrialex.service;

import android.content.Context;
import android.database.Cursor;

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
}
