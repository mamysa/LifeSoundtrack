package ch.usi.inf.gabrialex.service;

import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
    private Audio activeMedia;
    private PlayerStateEventListener eventListener;
    private Timer timer;

    public void setEventListener(PlayerStateEventListener listener) {
        this.eventListener = listener;
    }

    public MediaPlayerAdapter() {
        this.currentState = State.PAUSED;
        this.mediaPlayer = new MediaPlayer();
        this.mediaPlayer.setOnCompletionListener(this);
        this.activeMedia = null;

        this.timer = new Timer();

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
               PlaybackPositionUpdateTask t = new PlaybackPositionUpdateTask();
                t.execute();
            }
        };

        this.timer.schedule(timerTask, 0, 1000);
    }

    public void resume() {
        // contextResume(Time.getTime, this.activeMedia)
        this.mediaPlayer.start();
        this.currentState = State.PLAYING;
        System.out.println("Resume: " + this.activeMedia);
    }

    public void pause() {
        // contextPause(Time.getTime, this.activeMedia)
        this.currentState = State.PAUSED;
        this.mediaPlayer.pause();
    }

    public void toggle() {
        if (this.emptyPlaylist()) {
            return;
        }

        if (this.currentState == State.PLAYING) {
            this.pause();
        }
        else if (this.currentState == State.PAUSED) {
            this.resume();
        }

        eventListener.onPlaybackPositionChanged(this.getPlaybackPosition(), this.activeMedia.getDuration());
    }


    public void loadResource(Audio track) {
        this.mediaPlayer.reset();
        this.activeMedia = track;
        try {
            this.mediaPlayer.setDataSource(this.activeMedia.getData());
            this.mediaPlayer.prepare();
            this.mediaPlayer.seekTo(0);
            // FIXME prepare doesn't always prepare and in some cases plays chunks of previously loaded tracks?
            // FIXME seekTo(0) seems to fix it though. INVESTIGATE!
        }
        catch (IOException ex) {
            throw new AssertionError("IOException caught: Error loading resource " + track.getData());
        }
    }

    /**
     * Logic for playNext.
     * (1) In case of playlist is empty we return immediately! Otherwise, playlist is not empty and
     * we proceed. Store context.
     * (2) If playlist.getNext returns null, then that means that we have reached the end of the
     * playlist. Default behaviour is to still have current track as active media but the playback
     * is paused and rewind track to the beginning.
     * (3) Otherwise we select the next track in the playlist for playback. If mediaplayer was playing
     * before, we start playing new media, too.
     */
    public void playNext() {
        synchronized (Playlist.class) {
            Playlist instance = Playlist.getInstance();

            if (instance.playlistEmpty()) {
                return;
            }

            // contextEnd(Time.now(), this.activeMedia);
            Audio audio = instance.getNext(this.activeMedia);
            if (audio == null) {
                this.mediaPlayer.pause();
                this.currentState = State.PAUSED;
                this.mediaPlayer.seekTo(0);
            }
            else {
                if (this.currentState == State.PLAYING) {
                    this.mediaPlayer.pause();
                }
                this.loadResource(audio);
                if (this.currentState == State.PLAYING) {
                    this.mediaPlayer.start();
                    // contextNew(Time.now(), this.activeMedia)
                }
            }


            eventListener.onPlaybackPositionChanged(0, this.activeMedia.getDuration());
        }
    }

    /**
     * (1) If playlist is empty, bail right away!
     * (2) Check current playtime of the track. If we have played it for longer than 2 seconds,
     * then we do not change active media and rewind the track to the beginning. In this case we
     * do not store into the context (maybe?)
     * (3) If we are listening to the first track in the playlist, behaviour is the same as (2).
     * (4) Otherwise, switch tracks and store context.
     */
    public void playPrevious() {
        final int skipThresholdMs = 2000;

        synchronized (Playlist.class) {
            Playlist instance = Playlist.getInstance();

            if (instance.playlistEmpty()) {
                return;
            }

            Audio audio = instance.getPrevious(this.activeMedia);
            if (audio == null || this.mediaPlayer.getCurrentPosition() > skipThresholdMs) {
                this.mediaPlayer.seekTo(0);
            }
            else {
                // contextEnd(Time.now(), this.activeMedia);
                if (this.currentState == State.PLAYING) {
                    this.mediaPlayer.pause();
                }

                this.loadResource(audio);
                if (this.currentState == State.PLAYING) {
                    //contextNew(Time.now(), this.activeMedia);
                    this.mediaPlayer.start();
                }
            }

            System.out.println("Prev play: " + this.activeMedia + " " + this.currentState.toString());
            eventListener.onPlaybackPositionChanged(0, this.activeMedia.getDuration());
        }
    }

    public void setPlaybackPosition(int position) {
        if (this.activeMedia == null) {
            return;
        }

        this.mediaPlayer.seekTo(position);
        eventListener.onPlaybackPositionChanged(this.mediaPlayer.getCurrentPosition(), this.activeMedia.getDuration());
    }

    public void release() {
        this.mediaPlayer.release();
        this.mediaPlayer = null;
    }

    public void playlistChanged() {

        synchronized (Playlist.class) {
            Playlist instance = Playlist.getInstance();

            if (instance.playlistEmpty()) {
                return;
            }
            Audio track = Playlist.getInstance().getFirst();
            this.loadResource(track);
        }
    }

    public boolean emptyPlaylist() {
        synchronized (Playlist.class) {
            return Playlist.getInstance().playlistEmpty();
        }
    }

    public Audio getActiveMedia() {
        return this.activeMedia;
    }

    public State getState() {
        return this.currentState;
    }

    public int getPlaybackPosition() {
        return this.mediaPlayer.getCurrentPosition();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

    }


    private class PlaybackPositionUpdateTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            if (currentState == State.PLAYING) {
                int position = mediaPlayer.getCurrentPosition();
                int duration = activeMedia.getDuration();
                eventListener.onPlaybackPositionChanged(position, duration);
            }
            return null;
        }
    }
}
