package ch.usi.inf.gabrialex.musicplayer2;

import ch.usi.inf.gabrialex.service.Audio;

/**
 * Created by alex on 17.11.17.
 */

public interface PlayerControlEventListener {
    void onPlayPressed();
    void onPrevButtonPressed();
    void onNextButtonPressed();
    void onSeekBarChanged(int param);
    void onTrackSelected(Audio param);
    void onUpdateCurrentTrack();
}
