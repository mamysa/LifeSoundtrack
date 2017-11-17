package ch.usi.inf.gabrialex.musicplayer2;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;

import ch.usi.inf.gabrialex.protocol.Protocol;
import ch.usi.inf.gabrialex.service.Audio;
import ch.usi.inf.gabrialex.service.MusicPlayerService;

public class MainActivity extends AppCompatActivity {

    private LocalBroadcastManager broadcastManager;
    private MusicPlayerService musicService;
    private boolean musicServiceBound = false;
    private ArrayList<Audio> playlist;
    private int cursor = 0;
    private boolean playing = false;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("Fragment", "Receiving " + action);
            if ( action.equals(Protocol.RESPONSE_SONG_LISTING) ) {
                ArrayList<Audio> audio = intent.getParcelableArrayListExtra(Protocol.RESPONSE_SONG_LISTING);
                updateSongListing(audio);
            }
        }
    };

    private ServiceConnection musicServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicPlayerService.MusicPlayerBinder b = (MusicPlayerService.MusicPlayerBinder)iBinder;
            musicService = b.getService();
            musicServiceBound = true;
            // this is where we'd update stuff
            Log.d("NOTE", "service running!");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            musicServiceBound = false;
            Log.d("NOTE", "service stopped!!");
        }
    };

    private void updateSongListing(ArrayList<Audio> trackList) {
        //Collections.sort(trackList);
        ListView view = (ListView) findViewById(R.id.TrackListView);
        ArrayAdapter<Audio> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, trackList);
        this.playlist = trackList;
        view.setAdapter(adapter);


        if (trackList.size() != 0) {
            this.cursor = 0;

        }



    }

    private void requestUserForPermissions() {
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

            String[] req = {Manifest.permission.READ_EXTERNAL_STORAGE};
            requestPermissions(req, 10);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView view = (ListView) findViewById(R.id.TrackListView);
        view.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        this.requestUserForPermissions();

        Intent intent = new Intent(this, MusicPlayerService.class);
        bindService(intent, this.musicServiceConnection, Context.BIND_AUTO_CREATE);
        this.broadcastManager = LocalBroadcastManager.getInstance(this);


        // FIXME this is fugly!
        final Button play = (Button) findViewById(R.id.PlayButton);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d("pasing", ""+playing);
                if (cursor >= 0 && cursor < playlist.size()) {
                    Intent in = new Intent();
                    if (playing)
                        in.setAction(Protocol.PLAYER_PAUSE);
                    else
                        in.setAction(Protocol.PLAYER_RESUME);

                    playing = !playing;
                    broadcastManager.sendBroadcast(in);
                }

            }
        });

        final Button next = (Button) findViewById(R.id.NextTrack);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (cursor >= 0 && cursor < playlist.size()-1) {
                    cursor += 1;
                    Intent in = new Intent();
                    in.setAction(Protocol.PLAYER_NEXT);
                    broadcastManager.sendBroadcast(in);
                    Log.d("setting next", ""+cursor);
                }
            }
        });

        final Button prev = (Button) findViewById(R.id.PrevTrack);
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cursor > 0 && cursor < playlist.size()) {
                    cursor -= 1;
                    Intent in = new Intent();
                    in.setAction(Protocol.PLAYER_PREV);
                    broadcastManager.sendBroadcast(in);
                }
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter inf = new IntentFilter();
        inf.addAction(Protocol.RESPONSE_SONG_LISTING);
        this.broadcastManager.registerReceiver(this.broadcastReceiver, inf);

        Intent intent = new Intent();
        intent.setAction(Protocol.REQUEST_SONG_LISTING);
        this.broadcastManager.sendBroadcast(intent);
        Log.e("Calling onResume", "");


    }

    @Override
    protected void onPause() {
        super.onPause();
        this.broadcastManager.unregisterReceiver(this.broadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unbindService(musicServiceConnection);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        moveTaskToBack(true);
    }
}
