package ch.usi.inf.gabrialex.service;

import android.media.MediaPlayer;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import ch.usi.inf.gabrialex.datastructures.MusicContextManager;
import ch.usi.inf.gabrialex.datastructures.Playlist;
import ch.usi.inf.gabrialex.protocol.MediaPlayerState;

/**
 * Created by alex on 17.11.17.
 */

public class MediaPlayerAdapter implements MediaPlayer.OnCompletionListener {
    private MediaPlayer mediaPlayer;
    private MediaPlayerState currentState;
    private Audio activeMedia;
    private PlayerStateEventListener eventListener;
    private Timer timer;

    public void setEventListener(PlayerStateEventListener listener) {
        this.eventListener = listener;
    }

    public MediaPlayerAdapter() {
        this.currentState = MediaPlayerState.PAUSED;
        this.mediaPlayer = new MediaPlayer();
        this.mediaPlayer.setOnCompletionListener(this);
        this.activeMedia = null;

        this.timer = new Timer();

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                updateTimestamp();
            }
        };

        this.timer.schedule(timerTask, 0, 1000);
    }

    private void resume() {
        this.currentState = MediaPlayerState.PLAYING;
        this.mediaPlayer.start();
    }

    private void pause() {
        this.currentState = MediaPlayerState.PAUSED;
        this.mediaPlayer.pause();
    }

    public void toggle() {
        if (this.emptyPlaylist()) {
            return;
        }

        if (this.currentState == MediaPlayerState.PLAYING) {
            this.pause();
        }
        else if (this.currentState == MediaPlayerState.PAUSED) {
            this.resume();
        }

        MusicContextManager.getInstance().timestamp();
        this.eventListener.onStateChanged(this.currentState);
        this.eventListener.onPlaybackPositionChanged(this.getPlaybackPosition(), this.activeMedia.getDuration());
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
     * Set audio track to play. Controlled by list view.
     * @param audio
     */
    public void setTrack(Audio audio) {
        synchronized (Playlist.class) {
            Playlist instance = Playlist.getInstance();
            MusicContextManager context = MusicContextManager.getInstance();

            if (instance.playlistEmpty()) {
                return;
            }

            if (instance.contains(audio)) {
                if (this.activeMedia.equals(audio)) {
                    this.mediaPlayer.seekTo(0);
                    this.eventListener.onPlaybackPositionChanged(0, this.activeMedia.getDuration());
                }
                else {
                    if (this.currentState == MediaPlayerState.PLAYING) {
                        this.mediaPlayer.pause();
                        context.timestamp();
                    }

                    this.loadResource(audio);
                    this.currentState = MediaPlayerState.PLAYING;
                    this.mediaPlayer.start();
                    context.trackChanged(this.activeMedia);
                    context.timestamp();
                    this.eventListener.onPlaybackPositionChanged(0, this.activeMedia.getDuration());
                    this.eventListener.onTrackSelected(this.activeMedia);
                    this.eventListener.onStateChanged(this.currentState);
                }
            }
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
            MusicContextManager context = MusicContextManager.getInstance();

            if (instance.playlistEmpty()) {
                return;
            }

            Audio audio = instance.getNext(this.activeMedia);
            if (audio == null) {
                if (this.currentState == MediaPlayerState.PLAYING) {
                    this.mediaPlayer.pause();
                    context.timestamp();
                }
                context.trackChanged(this.activeMedia);
                this.currentState = MediaPlayerState.PAUSED;
                this.mediaPlayer.seekTo(0);
                this.eventListener.onStateChanged(this.currentState);
            }
            else {
                if (this.currentState == MediaPlayerState.PLAYING) {
                    this.mediaPlayer.pause();
                    context.timestamp();
                }
                this.loadResource(audio);
                context.trackChanged(this.activeMedia);
                if (this.currentState == MediaPlayerState.PLAYING) {
                    this.mediaPlayer.start();
                    context.timestamp();
                }
            }

            this.eventListener.onPlaybackPositionChanged(0, this.activeMedia.getDuration());
            this.eventListener.onTrackSelected(this.activeMedia);
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
            MusicContextManager context = MusicContextManager.getInstance();

            if (instance.playlistEmpty()) {
                return;
            }

            Audio audio = instance.getPrevious(this.activeMedia);
            if (audio == null || this.mediaPlayer.getCurrentPosition() > skipThresholdMs) {
                this.mediaPlayer.seekTo(0);
                if ( this.currentState == MediaPlayerState.PLAYING) {
                    context.timestamp();
                }
                context.trackChanged(this.activeMedia);
                if ( this.currentState == MediaPlayerState.PLAYING) {
                    context.timestamp();
                }
            }
            else {
                if (this.currentState == MediaPlayerState.PLAYING) {
                    this.mediaPlayer.pause();
                    context.timestamp();
                }
                this.loadResource(audio);
                context.trackChanged(this.activeMedia);
                if (this.currentState == MediaPlayerState.PLAYING) {
                    this.mediaPlayer.start();
                    context.timestamp();
                }
            }

            this.eventListener.onPlaybackPositionChanged(0, this.activeMedia.getDuration());
            this.eventListener.onTrackSelected(this.activeMedia);
        }
    }

    public void setPlaybackPosition(int position) {
        if (this.activeMedia == null) {
            return;
        }

        this.mediaPlayer.seekTo(position);
        this.eventListener.onPlaybackPositionChanged(this.mediaPlayer.getCurrentPosition(), this.activeMedia.getDuration());
    }

    public void eject() {
        if (this.activeMedia != null) {
            MusicContextManager context = MusicContextManager.getInstance();
            if ( this.currentState == MediaPlayerState.PLAYING) {
                this.mediaPlayer.pause();
                context.timestamp();
            }
            context.trackChanged(null);
            this.currentState = MediaPlayerState.PAUSED;
            this.eventListener.onTrackSelected(null);
        }
    }

    public void release() {
        this.mediaPlayer.release();
        this.mediaPlayer = null;
    }

    public void playlistChanged() {

        synchronized (Playlist.class) {
            Playlist instance = Playlist.getInstance();
            MusicContextManager context = MusicContextManager.getInstance();

            if (instance.playlistEmpty()) {
                return;
            }
            Audio track = Playlist.getInstance().getFirst();
            this.loadResource(track);
            context.trackChanged(this.activeMedia);
            this.eventListener.onPlaybackPositionChanged(0, this.activeMedia.getDuration());
            this.eventListener.onTrackSelected(this.activeMedia);
        }
    }

    public boolean emptyPlaylist() {
        synchronized (Playlist.class) {
            return Playlist.getInstance().playlistEmpty();
        }
    }

    public int getPlaybackPosition() {
        return this.mediaPlayer.getCurrentPosition();
    }

    public void reportTrack() {
        this.eventListener.onTrackSelected(this.activeMedia);
    }


    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        this.playNext();
    }

    private void updateTimestamp() {
        if (currentState == MediaPlayerState.PLAYING) {
            int position = mediaPlayer.getCurrentPosition();
            int duration = activeMedia.getDuration();
            eventListener.onPlaybackPositionChanged(position, duration);
        }
    }
}
