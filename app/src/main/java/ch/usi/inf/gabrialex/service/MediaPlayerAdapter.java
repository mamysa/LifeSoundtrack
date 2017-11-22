package ch.usi.inf.gabrialex.service;

import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ch.usi.inf.gabrialex.datastructures.Playlist;

/**
 * Created by alex on 17.11.17.
 */

enum State {
    PLAYING,
    PAUSED,
}

public class MediaPlayerAdapter implements MediaPlayer.OnCompletionListener {
    private MediaPlayer mediaPlayer;
    private State currentState;


    public MediaPlayerAdapter() {
        this.currentState = State.PAUSED;
        this.mediaPlayer = new MediaPlayer();
        this.mediaPlayer.setOnCompletionListener(this);
    }

    public void resume() {
        /// FIXME we need to check if media is still loaded here
        this.mediaPlayer.start();
        this.currentState = State.PLAYING;
    }

    public void toggle() {
        if (this.currentState == State.PLAYING)  this.pause();
        else if (this.currentState == State.PAUSED)   this.resume();
    }


    public void setTrack(Audio track) {
        if (this.currentState == State.PLAYING) {
            this.reset();
        }

        try {
            this.mediaPlayer.setDataSource(track.getData());
            this.mediaPlayer.prepare();
        }
        catch (IOException ex) {
            this.reset();
        }
    }

    // FIXME this should be in playlist class
    public void playNext() {
        Log.e("Player", "playNext");
        this.reset();

        Audio track;
        synchronized (Playlist.class) {
            track = Playlist.getInstance().playNext();
        }

        if (track != null) {
            this.setTrack(track);
            this.resume();
            Log.e("playNext", "not null");
        }
    }

    public void playPrevious() {
        Log.e("Player", "playPrevious");
        this.reset();

        Audio track;
        synchronized (Playlist.class) {
            track = Playlist.getInstance().playPrevious();
        }

        if (track != null) {
            this.setTrack(track);
            this.resume();
            Log.e("playPrevious", "not null");
        }
    }

    public void reset() {
        this.currentState = State.PAUSED;
        this.mediaPlayer.reset();
    }

    public void pause() {
        this.currentState = State.PAUSED;
        this.mediaPlayer.pause();
    }

    public void release() {
        this.mediaPlayer.release();
        this.mediaPlayer = null;
    }

    public void playlistChanged() {
       // get the beginning of the playlist
        Audio track;
        synchronized (Playlist.class) {
            track = Playlist.getInstance().playCurrent();
        }

        this.setTrack(track);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

    }
}
