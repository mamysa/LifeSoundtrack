package ch.usi.inf.gabrialex.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import ch.usi.inf.gabrialex.datastructures.Playlist;
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

        // initialize media player
        this.mediaPlayer = new MediaPlayerAdapter();
        this.mediaPlayer.setEventListener(this);
        this.mediaPlayer.playlistChanged();

        // initialize broadcast manager
        IntentFilter inf = new IntentFilter();
        for (String t: this.requestHandlers.keySet()) {
            inf.addAction(t);
        }
        this.broadcastManager = LocalBroadcastManager.getInstance(this);
        this.broadcastManager.registerReceiver(this.broadcastReceiver, inf);
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

            Intent intent = new Intent();
            intent.setAction(Protocol.PLAYER_NEWTRACK_SELECTED);
            intent.putExtra(Protocol.PLAYER_NEWTRACK_SELECTED, mediaPlayer.getActiveMedia());
            broadcastManager.sendBroadcast(intent);
        }
    };

    /**
     * Play previous track and notify activity of a new track being chosen.
     */
    private final EventHandler PreviousTrack = new EventHandler() {
        @Override
        public void handleEvent(Intent requestIntent) {
            mediaPlayer.playPrevious();

            Intent intent = new Intent();
            intent.setAction(Protocol.PLAYER_NEWTRACK_SELECTED);
            intent.putExtra(Protocol.PLAYER_NEWTRACK_SELECTED, mediaPlayer.getActiveMedia());
            broadcastManager.sendBroadcast(intent);
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

            Intent intent = new Intent();
            intent.setAction(Protocol.PLAYER_NEWTRACK_SELECTED);
            intent.putExtra(Protocol.PLAYER_NEWTRACK_SELECTED, mediaPlayer.getActiveMedia());
            broadcastManager.sendBroadcast(intent);
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

        if (cursor != null) {
            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                String a = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String b = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TRACK));
                String c = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String d = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String e = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String f = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                Audio audio = new Audio(a,b,c,d,e, Integer.parseInt(f));
                audioList.add(audio);

                cursor.moveToNext();
            }
            cursor.close();
        }

        synchronized (Playlist.class) {
            Playlist playlist = Playlist.getInstance();
            for (Audio audio: audioList) {
                playlist.addEntry(audio);
            }
        }
    }
}
