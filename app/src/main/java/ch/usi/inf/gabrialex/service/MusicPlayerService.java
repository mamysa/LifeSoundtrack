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

import ch.usi.inf.gabrialex.protocol.Protocol;

public class MusicPlayerService extends Service {

    private Binder binder = new MusicPlayerBinder();
    private MediaPlayerAdapter mediaPlayer;
    private LocalBroadcastManager broadcastManager;
    private ArrayList<Audio> playlist;

    private HashMap<String, RequestHandler> requestHandlers;

    @Override
    public void onCreate() {
        super.onCreate();
        this.playlist = this.getMusicListing();

        // initialize request handlers
        this.requestHandlers = new HashMap<>();
        this.requestHandlers.put(Protocol.REQUEST_SONG_LISTING, this.RequestSongListing);
        this.requestHandlers.put(Protocol.PLAYER_RESUME, this.ResumeTrack);
        this.requestHandlers.put(Protocol.PLAYER_PAUSE , this.PauseTrack);
        this.requestHandlers.put(Protocol.PLAYER_NEXT, this.NextTrack);
        this.requestHandlers.put(Protocol.PLAYER_PREV, this.PreviousTrack);

        // initialize media player
        this.mediaPlayer = new MediaPlayerAdapter();
        this.mediaPlayer.setPlaylist(this.playlist);

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
     * Request handler for song listing.
     */
    private final RequestHandler RequestSongListing = new RequestHandler() {
        @Override
        public void handleRequest(Intent intent) {
            Intent in = new Intent();
            in.setAction(Protocol.RESPONSE_SONG_LISTING);
            in.putParcelableArrayListExtra(Protocol.RESPONSE_SONG_LISTING, playlist);
            broadcastManager.sendBroadcast(in);
        }
    };

    /**
     * Play the track handler
     */
    private final RequestHandler ResumeTrack = new RequestHandler() {
        @Override
        public void handleRequest(Intent intent) {
            mediaPlayer.resume();
        }
    };

    private final RequestHandler PauseTrack = new RequestHandler() {
        @Override
        public void handleRequest(Intent intent) {
            mediaPlayer.pause();
        }
    };

    private final RequestHandler NextTrack = new RequestHandler() {
        @Override
        public void handleRequest(Intent intent) {
            mediaPlayer.playNext();
        }
    };

    private final RequestHandler PreviousTrack = new RequestHandler() {
        @Override
        public void handleRequest(Intent intent) {
            mediaPlayer.playPrevious();
        }
    };

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
            RequestHandler handler = requestHandlers.get(action);
            if (handler != null) {
                handler.handleRequest(intent);
            }
        }
    };

    /**
     * Locates music in external storage and returns the list.
     * In our app, we have to update the list every time the service is started, in case if
     * user adds some new music while service is not running and stuff.
     * @return FIXME THIS IS NOT SUPPOSED TO BE HERE
     */
    public ArrayList<Audio> getMusicListing() {
        ArrayList<Audio> audioList = new ArrayList<>();
        // TODO @refactor put permission checking logic into seperate method!
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            return audioList;
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
                Audio audio = new Audio(a,b,c,d,e);
                audioList.add(audio);

                cursor.moveToNext();
            }
            cursor.close();
        }

        return audioList;
    }
}
