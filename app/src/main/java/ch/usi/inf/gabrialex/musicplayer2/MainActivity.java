package ch.usi.inf.gabrialex.musicplayer2;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;

import ch.usi.inf.gabrialex.datastructures.Playlist;
import ch.usi.inf.gabrialex.protocol.Protocol;
import ch.usi.inf.gabrialex.service.Audio;
import ch.usi.inf.gabrialex.service.EventHandler;
import ch.usi.inf.gabrialex.service.MusicPlayerService;

public class MainActivity extends AppCompatActivity implements PlayerControlEventListener {

    private LocalBroadcastManager broadcastManager;
    private ArrayList<Audio> playlist;
    private ViewPager viewPager;
    private PagerAdapter pagerAdapter;


    private PlayerControlFragment playerControlFragment;
    private PlaylistFragment playlistFragment;
    private HashMap<String, EventHandler> eventHandlers;


    /**
     * onCreate
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setup event handlers.
        this.eventHandlers = new HashMap<>();
        this.eventHandlers.put(Protocol.RESPONSE_SONG_LISTING, this.PlaylistUpdate);


        ArrayList<Fragment> fragments = new ArrayList<>();
        this.playlistFragment = new PlaylistFragment();
        this.playlistFragment.setEventListener(this);
        fragments.add(this.playlistFragment);

        this.playerControlFragment = new PlayerControlFragment();
        this.playerControlFragment.setEventListener(this);
        fragments.add(this.playerControlFragment);

        this.viewPager = (ViewPager)findViewById(R.id.fragment_container_pager);
        this.pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager(), fragments);
        this.viewPager.setAdapter(this.pagerAdapter);


        Intent intent = new Intent(this, MusicPlayerService.class);
        bindService(intent, this.musicServiceConnection, Context.BIND_AUTO_CREATE);
        this.broadcastManager = LocalBroadcastManager.getInstance(this);

        this.requestUserForPermissions();
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
     @Override
    public void onPlayPressed() {
        Intent intent = new Intent();
        intent.setAction(Protocol.PLAYER_TOGGLE);
        this.broadcastManager.sendBroadcast(intent);
    }

    //============================================================
    // Various responses activity needs to get
    //============================================================

    /**
     * Update ListView component when the playlist is updated.
     */
    private EventHandler PlaylistUpdate = new EventHandler() {
        @Override
        public void handleEvent(Intent intent) {
            Log.d("PlaylistUpdate", "Updating playlist UI");

            playlist = intent.getParcelableArrayListExtra(Protocol.RESPONSE_SONG_LISTING);
            synchronized (Playlist.class) {
                ArrayList<Audio> playlist = Playlist.getInstance().getPlaylist();
                playlistFragment.update(playlist);
            }
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

    private void requestUserForPermissions() {
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

            String[] req = {Manifest.permission.READ_EXTERNAL_STORAGE};
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


    /**
     * Adapter for PageView.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        ArrayList<Fragment> fragments;

        public ScreenSlidePagerAdapter(FragmentManager fm, ArrayList<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;

        }

        @Override
        public int getCount() {
            return this.fragments.size();
        }

        @Override
        public Fragment getItem(int position) {
            position = Math.min(this.fragments.size()-1, position);
            position = Math.max(0, position);
            return this.fragments.get(position);
        }
    }
}
