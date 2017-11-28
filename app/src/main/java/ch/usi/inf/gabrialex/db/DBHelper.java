package ch.usi.inf.gabrialex.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by alex on 28.11.17.
 */

public class DBHelper extends SQLiteOpenHelper {

    private static DBHelper instance;
    public static synchronized  DBHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DBHelper(context.getApplicationContext());
        }
        return instance;
    }

    protected static final String DB_NAME = "LIFESOUNDTRACK_DB";
    protected static final int DB_VERSION = 1;



    public DBHelper(Context content) {
        super(content, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTracksTable = String.format("CREATE TABLE IF NOT EXISTS %s(" +
                        "%s INTEGER PRIMARY KEY, " +
                        "%s TEXT, " +
                        "%s TEXT, " +
                        "%s TEXT, " +
                        "%s TEXT, " +
                        "%s TEXT, " +
                        "%s TEXT)",
            DBTableAudio.TABLE_NAME, DBTableAudio.ID,
            DBTableAudio.DATA, DBTableAudio.TRACK,
            DBTableAudio.TITLE, DBTableAudio.ALBUM,
            DBTableAudio.ARTIST, DBTableAudio.DURATION);
        sqLiteDatabase.execSQL(createTracksTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
