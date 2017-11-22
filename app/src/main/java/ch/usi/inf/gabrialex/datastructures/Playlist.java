package ch.usi.inf.gabrialex.datastructures;

import java.util.ArrayList;

import ch.usi.inf.gabrialex.service.Audio;

/**
 * Created by alex on 21.11.17.
 */

public class Playlist {

    private static Playlist instance = null;

    /**
     * Get playlist instance.
     * @return
     */
    public static Playlist getInstance() {
        if (instance == null) {
            instance = new Playlist();
        }

        return instance;
    }

    private ArrayList<Audio> playlist;
    private int cursor;

    private Playlist() {
        this.playlist = new ArrayList<>();
        this.cursor = 0;
    }

    /**
     * Selects next track to play.
     * @return null when playlist is empty. null is also returned when there are no more songs left
     * in the playlist. Otherwise, pick the next song.
     */
    public Audio playNext() {
        if (this.playlist.size() == 0) {
            return null;
        }

        this.cursor += 1;
        if (this.cursor == this.playlist.size()) {
            // reached end of the playlist, should stop playing. We could also set cursor to the
            // last track ??
            this.cursor = this.playlist.size() - 1;
            return null;
        }

        this.cursor = Math.max(this.cursor, 0);
        this.cursor = Math.min(this.cursor, this.playlist.size() - 1);
        return this.playlist.get(this.cursor);
    }

    /**
     * Selects previous track to play.
     * @return null if playlist is empty, otherwise pick previous track.
     */
    public Audio playPrevious() {
        if (this.playlist.size() == 0) {
            return null;
        }

        this.cursor -= 1;
        this.cursor = Math.max(this.cursor, 0);
        this.cursor = Math.min(this.cursor, this.playlist.size() - 1);
        return this.playlist.get(this.cursor);
    }

    public Audio playCurrent() {
        if (this.playlist.size() == 0) {
            return null;
        }

        return this.playlist.get(this.cursor);
    }

    /**
     * Add track to the playlist.
     * @param audio
     */
    public void addEntry(Audio audio) {
        this.playlist.add(audio);
    }

    /**
     * Returns actual array containing tracks. TODO maybe replace it with iterator?
     * @return
     */
    public ArrayList<Audio> getPlaylist() {
        return this.playlist;
    }
}
