package ch.usi.inf.gabrialex.musicplayer2;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.SeekBar;

import ch.usi.inf.gabrialex.datastructures.RankWeightPreferences;

/**
 * Created by alex on 17.11.17.
 */

public class SettingsActivity extends AppCompatActivity {

    private boolean settingsChanged = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
    }

    /**
     * Setup seekbar listeners and read weights.
     */
    @Override
    protected void onResume() {
        super.onResume();
        RankWeightPreferences.readPreferences(this);

        SeekBar importanceTime = (SeekBar)findViewById(R.id.time_ranking_importance);
        importanceTime.setOnSeekBarChangeListener(seekbarListener);

        SeekBar importanceLocation = (SeekBar) findViewById(R.id.location_ranking_importance);
        importanceLocation.setOnSeekBarChangeListener(seekbarListener);

        this.doubleToSeekbar(importanceTime, RankWeightPreferences.IMPORTANCE_TIME, RankWeightPreferences.IMPORTANCE_MIN, RankWeightPreferences.IMPORTANCE_MAX);
        this.doubleToSeekbar(importanceLocation, RankWeightPreferences.IMPORTANCE_LOCATION, RankWeightPreferences.IMPORTANCE_MIN, RankWeightPreferences.IMPORTANCE_MAX);
    }

    /**
     * onPause. Write rank weights to file if any of the settings have changed.
     */
    @Override
    protected void onPause() {
        super.onPause();

        if (settingsChanged) {
            SeekBar importanceTime = (SeekBar)findViewById(R.id.time_ranking_importance);
            SeekBar importanceLocation = (SeekBar) findViewById(R.id.location_ranking_importance);

            RankWeightPreferences.IMPORTANCE_TIME = this.seekbarToDouble(importanceTime, RankWeightPreferences.IMPORTANCE_MIN, RankWeightPreferences.IMPORTANCE_MAX);
            RankWeightPreferences.IMPORTANCE_LOCATION = this.seekbarToDouble(importanceLocation, RankWeightPreferences.IMPORTANCE_MIN, RankWeightPreferences.IMPORTANCE_MAX);


            RankWeightPreferences.writePreferences(this);
        }
    }

    /**
     * Maps seekbar value to a value in range [min,max].
     * @param seekBar
     * @param min
     * @param max
     * @return
     */
    private double seekbarToDouble(SeekBar seekBar, double min, double max) {
        double frac = (double)seekBar.getProgress() / (double)seekBar.getMax();
        return (1 - frac) * min + frac * max;
    }

    private void doubleToSeekbar(SeekBar seekBar, double value, double min, double max) {
        double frac = (value - min) / (max - min);
        seekBar.setMax(100);
        seekBar.setProgress((int)(frac * 100));
    }

    private SeekBar.OnSeekBarChangeListener seekbarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            settingsChanged = true;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            settingsChanged = true;
        }
    };
}
