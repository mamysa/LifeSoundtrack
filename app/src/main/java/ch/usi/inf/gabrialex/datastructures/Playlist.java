package ch.usi.inf.gabrialex.datastructures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import ch.usi.inf.gabrialex.service.Audio;

/**
 * Created by alex on 21.11.17.
 */

public class Playlist {

    private static Playlist instance = null;

    /**
     * Get playlist instance.
     *
     * @return
     */
    public static Playlist getInstance() {
        if (instance == null) {
            instance = new Playlist();
        }

        return instance;
    }

    private ArrayList<Audio> playlist;

    private Playlist() {
        this.playlist = new ArrayList<>();
    }

    public boolean contains(Audio audio) {
        return this.playlist.indexOf(audio) != -1;
    }

    /**
     * Selects next track to play.
     *
     * @return null when playlist is empty. null is also returned when there are no more songs left
     * in the playlist. Otherwise, pick the next song.
     */
    public Audio getNext(Audio audio) {
        if (this.playlistEmpty()) {
            return null;
        }

        int idx = this.playlist.indexOf(audio);
        if (idx == -1) {
            return this.getFirst();
        }

        idx += 1;
        if (idx == this.playlist.size()) {
            return null;
        }

        return this.playlist.get(idx);
    }

    /**
     * Selects previous track to play.
     *
     * @return null if playlist is empty, otherwise pick previous track.
     */
    public Audio getPrevious(Audio audio) {
        if (this.playlistEmpty()) {
            return null;
        }

        int idx = this.playlist.indexOf(audio);
        if (idx == -1) {
            return this.getFirst();
        }

        idx -= 1;
        if (idx < 0) {
            return null;
        }

        return this.playlist.get(idx);
    }

    public Audio getFirst() {
        if (this.playlist.size() == 0) {
            return null;
        }

        return this.playlist.get(0);
    }

    /**
     * Add track to the playlist.
     *
     * @param audio
     */
    public void addEntry(Audio audio) {
        this.playlist.add(audio);
    }

    /**
     * Returns actual array containing tracks. TODO maybe replace it with iterator?
     *
     * @return
     */
    public ArrayList<Audio> getPlaylist() {
        return this.playlist;
    }

    /**
     * Clear ArrayList containing playlist.
     */
    public void clear() {
        this.playlist.clear();
    }

    public boolean playlistEmpty() {
        return this.playlist.size() == 0;
    }

    public void onPlaylistUpdated() {
        Collections.sort(this.playlist, new Comparator<Audio>() {
            @Override
            public int compare(Audio a, Audio b) {
                return Double.compare(b.getRank(), a.getRank());
            }
        });
    }

    public Audio findTrackById(int id) {
        for (Audio track : this.playlist) {
            if (track.getId() == id)
                return track;
        }
        return null;
    }
}
