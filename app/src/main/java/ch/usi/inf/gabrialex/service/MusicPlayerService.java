package ch.usi.inf.gabrialex.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import ch.usi.inf.gabrialex.datastructures.Playlist;
import ch.usi.inf.gabrialex.db.DBHelper;
import ch.usi.inf.gabrialex.db.DBTableAudio;
import ch.usi.inf.gabrialex.protocol.MediaPlayerState;
import ch.usi.inf.gabrialex.protocol.Protocol;

public class MusicPlayerService extends Service implements PlayerStateEventListener {

    private Binder binder = new MusicPlayerBinder();
    private MediaPlayerAdapter mediaPlayer;
    private LocalBroadcastManager broadcastManager;

    private HashMap<String, EventHandler> requestHandlers;

    @Override
    public void onCreate() {
        super.onCreate();
        this.getMusicListing();

        // initialize request handlers
        this.requestHandlers = new HashMap<>();
        this.requestHandlers.put(Protocol.REQUEST_SONG_LISTING, this.RequestSongListing);
        this.requestHandlers.put(Protocol.PLAYER_TOGGLE, this.ToggleTrack);
        this.requestHandlers.put(Protocol.PLAYER_NEXT, this.NextTrack);
        this.requestHandlers.put(Protocol.PLAYER_PREV, this.PreviousTrack);
        this.requestHandlers.put(Protocol.PLAYER_SET_POSITION, this.SetPlaybackPosition);
        this.requestHandlers.put(Protocol.PLAYER_SETTRACK, this.SetTrack);

        // initialize broadcast manager
        IntentFilter inf = new IntentFilter();
        for (String t: this.requestHandlers.keySet()) {
            inf.addAction(t);
        }
        this.broadcastManager = LocalBroadcastManager.getInstance(this);
        this.broadcastManager.registerReceiver(this.broadcastReceiver, inf);

        // initialize media player
        this.mediaPlayer = new MediaPlayerAdapter();
        this.mediaPlayer.setEventListener(this);
        this.mediaPlayer.playlistChanged();

        //FIXME temp fix for first track not having info
        //Intent intent = new Intent();
        //intent.setAction(Protocol.PLAYER_NEWTRACK_SELECTED);
        //intent.putExtra(Protocol.PLAYER_NEWTRACK_SELECTED, mediaPlayer.getActiveMedia());
        //this.broadcastManager.sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.mediaPlayer.release();
        this.broadcastManager.unregisterReceiver(this.broadcastReceiver);
    }

    /**
     * Request handler for song listing. Playlists are treated slightly differently as opposed
     * to other player state - actual playlist is stored in a singleton object. This way, we
     * avoid passing a lot of date using intents.
     */
    private final EventHandler RequestSongListing = new EventHandler() {
        @Override
        public void handleEvent(Intent intent) {
            Intent in = new Intent();
            in.setAction(Protocol.RESPONSE_SONG_LISTING);
            broadcastManager.sendBroadcast(in);
        }
    };

    /**
     * Play the track handler
     */
    private final EventHandler ToggleTrack = new EventHandler() {
        @Override
        public void handleEvent(Intent intent) {
            mediaPlayer.toggle();
        }
    };

    /**
     * Play next track and notify activity of a new track being chosen.
     */
    private final EventHandler NextTrack = new EventHandler() {
        @Override
        public void handleEvent(Intent requestIntent) {
            mediaPlayer.playNext();
        }
    };

    /**
     * Play previous track and notify activity of a new track being chosen.
     */
    private final EventHandler PreviousTrack = new EventHandler() {
        @Override
        public void handleEvent(Intent requestIntent) {
            mediaPlayer.playPrevious();
        }
    };

    /**
     * Triggers when user manipulates seekbar.
     */
    private final EventHandler SetPlaybackPosition = new EventHandler() {
        @Override
        public void handleEvent(Intent intent) {
            int position =  intent.getIntExtra(Protocol.PLAYER_SET_POSITION, 0);
            mediaPlayer.setPlaybackPosition(position);
        }
    };

    /**
     * Triggers when user selects a song from the list view.
     */
    private final EventHandler SetTrack = new EventHandler() {
        @Override
        public void handleEvent(Intent requestIntent) {
            Audio audio = requestIntent.getParcelableExtra(Protocol.PLAYER_SETTRACK);
            mediaPlayer.setTrack(audio);
        }
    };

    /**
     * Triggers when playback position of the track is changed.
     * @param position
     * @param duration
     */
    @Override
    public void onPlaybackPositionChanged(int position, int duration) {
        Intent intent = new Intent();
        intent.setAction(Protocol.PLAYER_PLAYBACK_POSITION_UPDATE);
        intent.putExtra(Protocol.PLAYER_PLAYBACK_POSITION_DATA, position);
        intent.putExtra(Protocol.PLAYER_PLAYBACK_DURATION_DATA, duration);
        broadcastManager.sendBroadcast(intent);
    }

    /**
     * Triggers when player selects a track to play.
     * @param param audio being played.
     */
    @Override
    public void onTrackSelected(Audio param) {
        Intent intent = new Intent();
        intent.setAction(Protocol.PLAYER_NEWTRACK_SELECTED);
        intent.putExtra(Protocol.PLAYER_NEWTRACK_SELECTED, param);
        broadcastManager.sendBroadcast(intent);
    }


    /**
     * Triggers when media player either pauses or resumes
     * @param param
     */
    @Override
    public void onStateChanged(MediaPlayerState param) {
        Intent intent = new Intent();
        intent.setAction(Protocol.PLAYER_STATE_CHANGE);
        intent.putExtra(Protocol.PLAYER_STATE_CHANGE, param);
        broadcastManager.sendBroadcast(intent);
    }

    public class MusicPlayerBinder extends Binder {
        public MusicPlayerService getService() { return MusicPlayerService.this; }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return this.binder;
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            EventHandler handler = requestHandlers.get(action);
            if (handler != null) {
                handler.handleEvent(intent);
            }
        }
    };

    /**
     * Locates music in external storage and returns the list.
     * In our app, we have to update the list every time the service is started, in case if
     * user adds some new music while service is not running and stuff.
     * @return FIXME THIS IS NOT SUPPOSED TO BE HERE
     */
    public void getMusicListing() {
        DBHelper helper = DBHelper.getInstance(this);
        ArrayList<Audio> audioList = new ArrayList<>();
        // TODO @refactor put permission checking logic into seperate method!
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            //return audioList;
            return;
        }
        // TODO @refactor this has to go into MusicObserver class.
        ContentResolver resolver = this.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = resolver.query(uri, null, selection, null, sortOrder);
        Log.d("getMusicListing()", "getting music");
        Cursor oldId;


        long libraryVersion = 0;
        // first we need to fetch previous update number. Our current update number will be
        // prev_update_num + 1. All tracks that are still present in the media store will have
        // their lifetime incremented. Entries that did not have their lifetime updated will be purged!
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor lifetimeCursor = db.rawQuery("SELECT " + DBTableAudio.LIFETIME + " FROM " + DBTableAudio.TABLE_NAME, null);
        if (lifetimeCursor != null) {
            if (lifetimeCursor.getCount() != 0) {
                System.out.println("wat");
                lifetimeCursor.moveToFirst();
                libraryVersion = lifetimeCursor.getLong(lifetimeCursor.getColumnIndex(DBTableAudio.LIFETIME)) + 1;
            }

            lifetimeCursor.close();
        }

        if (cursor != null) {
            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                String a = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String b = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TRACK));
                String c = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String d = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String e = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String f = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));


                // TODO @refactor maybe goes on its own method?
                /*
                If the music file is not found in the DB, then add it
                 */
                oldId = helper.getReadableDatabase().rawQuery("SELECT _id FROM Tracks WHERE data=?", new String[]{ a });
                oldId.moveToFirst();
                int id;
                //File not found in the DB-> add the file
                if (oldId.getCount() == 0){
                    ContentValues values = new ContentValues();
                    values.put("data", a);
                    values.put("track", b);
                    values.put("title", c);
                    values.put("album", d);
                    values.put("artist", e);
                    values.put("duration", f);
                    values.put(DBTableAudio.LIFETIME, libraryVersion);
                    helper.getWritableDatabase().insert("Tracks", null, values);
                    Cursor newId;
                    newId = helper.getReadableDatabase().rawQuery("SELECT _id FROM Tracks WHERE data =?", new String[]{ a });
                    newId.moveToFirst();
                    id = Integer.parseInt(newId.getString(0));
                    Audio audio = new Audio(a,b,c,d,e, Integer.parseInt(f), id);
                    audioList.add(audio);
                    System.out.println(audio);
                }
                else {//File already in the DB-> do nothing
                    Log.d("getMusicListing()", "cannot add  "+ a);
                    id = Integer.parseInt(oldId.getString(0));
                    Audio audio = new Audio(a,b,c,d,e, Integer.parseInt(f), id);
                    audioList.add(audio);
                    helper.getWritableDatabase()
                          .execSQL("UPDATE " + DBTableAudio.TABLE_NAME + " SET "
                                              + DBTableAudio.LIFETIME + "=? WHERE "
                                              + DBTableAudio.DATA + "=?;", new String[]{ ""+libraryVersion, a });
                }
                
                cursor.moveToNext();
            }
            cursor.close();
        }

        // remove all old entries.
        helper.getWritableDatabase()
              .execSQL("DELETE FROM " + DBTableAudio.TABLE_NAME + " WHERE "
                                      + DBTableAudio.LIFETIME + "<?;", new String[] { ""+libraryVersion });

        synchronized (Playlist.class) {
            Playlist playlist = Playlist.getInstance();
            for (Audio audio: audioList) {
                playlist.addEntry(audio);
            }
        }
    }
}
