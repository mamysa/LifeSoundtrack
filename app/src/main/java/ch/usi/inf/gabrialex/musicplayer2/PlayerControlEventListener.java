package ch.usi.inf.gabrialex.musicplayer2;

/**
 * Created by alex on 17.11.17.
 */

public interface PlayerControlEventListener {
    void onPlayPressed();
    void onPrevButtonPressed();
    void onNextButtonPressed();
    void onSeekBarChanged(int param);
}
