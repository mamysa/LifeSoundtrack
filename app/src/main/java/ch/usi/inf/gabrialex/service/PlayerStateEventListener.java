package ch.usi.inf.gabrialex.service;

import ch.usi.inf.gabrialex.protocol.MediaPlayerState;

/**
 * Created by alex on 23.11.17.
 */

public interface PlayerStateEventListener {
    void onPlaybackPositionChanged(int position, int duration);
    void onTrackSelected(Audio param);
    void onStateChanged(MediaPlayerState param);
}
