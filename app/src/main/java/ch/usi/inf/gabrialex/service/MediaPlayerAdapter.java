package ch.usi.inf.gabrialex.service;

import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    private ArrayList<Audio> playlist;
    private int cursor;


    public MediaPlayerAdapter() {
        this.mediaPlayer = new MediaPlayer();
        this.mediaPlayer.setOnCompletionListener(this);
        this.cursor = 0;
    }

    public void resume() {
        this.mediaPlayer.start();
        this.currentState = State.PLAYING;

    }

    public void setTrack(Audio track) {
        if (this.currentState == State.PLAYING) {
            this.reset();
        }

        // ensure track is in playlist TODO how to handle this?
        this.cursor = this.playlist.indexOf(track);
        if (this.cursor == -1) {
            this.reset();
            return;
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
        this.reset();
        this.cursor += 1;
        this.cursor = Math.max(this.cursor, 0);
        this.cursor = Math.min(this.cursor, this.playlist.size() - 1);
        Log.e("Player", "playNext " + this.cursor);
        Audio track = this.playlist.get(this.cursor);
        this.setTrack(track);
        this.resume();
    }

    public void playPrevious() {
        this.reset();
        this.cursor -= 1;
        this.cursor = Math.max(this.cursor, 0);
        this.cursor = Math.min(this.cursor, this.playlist.size() - 1);

        Log.e("Player", "playPrevious " + this.cursor);
        Audio track = this.playlist.get(this.cursor);
        this.setTrack(track);
        this.resume();
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

    public void setPlaylist(ArrayList<Audio> playlist) {
        this.playlist = playlist;
        if (this.playlist.size() != 0) {
            this.setTrack(this.playlist.get(0));
        }
    }


    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

    }
}
