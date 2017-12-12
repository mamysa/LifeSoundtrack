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
        //this.getMusicListing();

        // just for testing for now, will not do thread.join
        PlaylistRankingTask playlistRankingTask = new PlaylistRankingTask(this);
        Thread thread = new Thread(playlistRankingTask);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException ex) {
            // FIXME wat?
        }

        // initialize request handlers
        this.requestHandlers = new HashMap<>();
        this.requestHandlers.put(Protocol.REQUEST_SONG_LISTING, this.RequestSongListing);
        this.requestHandlers.put(Protocol.PLAYER_TOGGLE, this.ToggleTrack);
        this.requestHandlers.put(Protocol.PLAYER_NEXT, this.NextTrack);
        this.requestHandlers.put(Protocol.PLAYER_PREV, this.PreviousTrack);
        this.requestHandlers.put(Protocol.PLAYER_SET_POSITION, this.SetPlaybackPosition);
        this.requestHandlers.put(Protocol.PLAYER_SETTRACK, this.SetTrack);
        this.requestHandlers.put(Protocol.PLAYER_GET_CURRENT_TRACK, this.GetCurrentTrack);

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
     * Triggers when main activity is back to life and wants to update currently playing track.
     */
    private final EventHandler GetCurrentTrack  = new EventHandler() {
        @Override
        public void handleEvent(Intent intent) {
            mediaPlayer.reportTrack();
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
}
