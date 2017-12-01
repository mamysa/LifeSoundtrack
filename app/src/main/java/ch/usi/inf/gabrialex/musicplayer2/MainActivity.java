package ch.usi.inf.gabrialex.musicplayer2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.EventLog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.HashMap;

import ch.usi.inf.gabrialex.datastructures.Playlist;
import ch.usi.inf.gabrialex.protocol.MediaPlayerState;
import ch.usi.inf.gabrialex.protocol.Protocol;
import ch.usi.inf.gabrialex.service.Audio;
import ch.usi.inf.gabrialex.service.EventHandler;
import ch.usi.inf.gabrialex.service.LocationService;
import ch.usi.inf.gabrialex.service.MusicPlayerService;

public class MainActivity extends AppCompatActivity implements PlayerControlEventListener {

    private LocalBroadcastManager broadcastManager;
    private ArrayList<Audio> playlist;


    private PlayerControlFragment playerControlFragment;
    private PlaylistFragment playlistFragment;
    private HashMap<String, EventHandler> eventHandlers;

    /**
     * onCreate
     * @param savedInstanceState
     */
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        System.out.println("MAINACTIVITY_ONCREATE");

        // setup event handlers.
        this.eventHandlers = new HashMap<>();
        this.eventHandlers.put(Protocol.RESPONSE_SONG_LISTING, this.PlaylistUpdated);
        this.eventHandlers.put(Protocol.PLAYER_NEWTRACK_SELECTED, this.NewTrackSelected);
        this.eventHandlers.put(Protocol.PLAYER_PLAYBACK_POSITION_UPDATE, this.PlaybackPositionUpdated);
        this.eventHandlers.put(Protocol.PLAYER_STATE_CHANGE, this.PlayerStateChanged);


        if (savedInstanceState == null) {
            this.playlistFragment = new PlaylistFragment();
            this.playlistFragment.setEventListener(this);

            this.playerControlFragment = new PlayerControlFragment();
            this.playerControlFragment.setEventListener(this);

            FragmentManager fm = getSupportFragmentManager();

            if (fm.findFragmentById(R.id.playlist) == null) {
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.add(R.id.playlist, this.playlistFragment);
                transaction.commitNow();
            }

            if (fm.findFragmentById(R.id.controll) == null) {
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.add(R.id.controll, this.playerControlFragment);
                transaction.commitNow();
            }
        }



        /*
        FragmentTransaction transaction =
                getSupportFragmentManager().beginTransaction();

        transaction.add(R.id.playlist, playlistFragment);

        transaction.commit();
        */


        Intent intent = new Intent(this, MusicPlayerService.class);
        bindService(intent, this.musicServiceConnection, Context.BIND_AUTO_CREATE);
        this.broadcastManager = LocalBroadcastManager.getInstance(this);
        Intent locationIntent = new Intent(this, LocationService.class);
        bindService(locationIntent, this.locationServiceConnection, Context.BIND_AUTO_CREATE);

    }

    /**
     * OnResume
     */
    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter inf = new IntentFilter();
        for (String t: this.eventHandlers.keySet()) {
            inf.addAction(t);
        }
        this.broadcastManager.registerReceiver(this.broadcastReceiver, inf);
        onRequestPlaylistListing();
    }

    /**
     * OnPause activity.
     */
    @Override
    protected void onPause() {
        super.onPause();
        this.broadcastManager.unregisterReceiver(this.broadcastReceiver);
    }

    /**
     * onDestroy
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(musicServiceConnection);
    }

    /**
     * Need to prevent back button press from destroying the service.
     */
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    //============================================================
    // Various requests activity can send to the service.
    //============================================================

    /**
     * Request song listing.
     */
    public void onRequestPlaylistListing() {
        Intent intent = new Intent();
        intent.setAction(Protocol.REQUEST_SONG_LISTING);
        broadcastManager.sendBroadcast(intent);
    }

    /**
     * Sends message to service to select previous track.
     */
    @Override
    public void onPrevButtonPressed() {
        Intent intent = new Intent();
        intent.setAction(Protocol.PLAYER_PREV);
        this.broadcastManager.sendBroadcast(intent);
        //this.playerControlFragment.updateView( this.playlist.get(this.cursor) );
    }

    /**
     * Sends message to service to select next track.
     */
    @Override
    public void onNextButtonPressed() {
        Intent intent = new Intent();
        intent.setAction(Protocol.PLAYER_NEXT);
        this.broadcastManager.sendBroadcast(intent);
        //this.playerControlFragment.updateView( this.playlist.get(this.cursor) );
    }


    /**
     * Sends message to the server to pause / resume current track.
     */
     @SuppressLint("MissingPermission")
     @Override
    public void onPlayPressed() {
        Intent intent = new Intent();
        intent.setAction(Protocol.PLAYER_TOGGLE);
        this.broadcastManager.sendBroadcast(intent);
    }

    @Override
    public void onSeekBarChanged(int param) {
        Intent intent = new Intent();
        intent.setAction(Protocol.PLAYER_SET_POSITION);
        intent.putExtra(Protocol.PLAYER_SET_POSITION, param);
        this.broadcastManager.sendBroadcast(intent);
    }

    /**
     * Fires up when user selects a track to play from list view.
     * @param param
     */
    @Override
    public void onTrackSelected(Audio param) {
        Intent intent = new Intent();
        intent.setAction(Protocol.PLAYER_SETTRACK);
        intent.putExtra(Protocol.PLAYER_SETTRACK, param);
        this.broadcastManager.sendBroadcast(intent);
    }

    //============================================================
    // Various responses activity needs to get
    //============================================================

    /**
     * Update ListView component when the playlist is updated.
     */
    private EventHandler PlaylistUpdated = new EventHandler() {
        @Override
        public void handleEvent(Intent intent) {
            //Log.d("PlaylistUpdate", "Updating playlist UI");
            playlist = intent.getParcelableArrayListExtra(Protocol.RESPONSE_SONG_LISTING);
            synchronized (Playlist.class) {
                ArrayList<Audio> playlist = Playlist.getInstance().getPlaylist();
                playlistFragment.update(playlist);
            }
        }
    };

    /**
     * Fires when the player selects a song to play.
     */
    private EventHandler NewTrackSelected = new EventHandler() {
        @Override
        public void handleEvent(Intent intent) {
            Audio audio = intent.getParcelableExtra(Protocol.PLAYER_NEWTRACK_SELECTED);
            playerControlFragment.updateView(audio);

        }
    };

    /**
     * Fires when media player makes some progress playing some song.
     */
    private EventHandler PlaybackPositionUpdated = new EventHandler() {
        @Override
        public void handleEvent(Intent intent) {
            //Log.d("PlaybackPositionUpdated", "Updating timestamp");
            int position = intent.getIntExtra(Protocol.PLAYER_PLAYBACK_POSITION_DATA, 0);
            int duration = intent.getIntExtra(Protocol.PLAYER_PLAYBACK_DURATION_DATA, 0);
            playerControlFragment.updatePlaybackPosition(position, duration);
        }
    };

    private EventHandler PlayerStateChanged = new EventHandler() {
        @Override
        public void handleEvent(Intent intent) {
            MediaPlayerState state = (MediaPlayerState)intent.getSerializableExtra(Protocol.PLAYER_STATE_CHANGE);
            playerControlFragment.updatePlayerState(state);
            //System.out.println("Current state " + state.toString());
        }
    };

    /**
     * Broadcast receiver event handler.
     */
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            EventHandler handler = eventHandlers.get(action);
            if (handler != null) {
                handler.handleEvent(intent);
            }
        }
    };

    private ServiceConnection musicServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicPlayerService.MusicPlayerBinder b = (MusicPlayerService.MusicPlayerBinder)iBinder;
            onRequestPlaylistListing();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) { }
    };

            private ServiceConnection locationServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    LocationService.LocationServiceBinder b = (LocationService.LocationServiceBinder)iBinder;
                }
                @Override
                public void onServiceDisconnected(ComponentName componentName) { }
            };

    private void requestUserForPermissions() {
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED || checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            String[] req = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
            requestPermissions(req, 10);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.trigger_options_activity) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}
