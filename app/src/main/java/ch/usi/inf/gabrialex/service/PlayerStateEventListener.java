package ch.usi.inf.gabrialex.service;

/**
 * Created by alex on 23.11.17.
 */

public interface PlayerStateEventListener {
    void onPlaybackPositionChanged(int position, int duration);
}
