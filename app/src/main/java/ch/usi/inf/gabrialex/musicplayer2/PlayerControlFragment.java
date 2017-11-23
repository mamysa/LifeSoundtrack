package ch.usi.inf.gabrialex.musicplayer2;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import ch.usi.inf.gabrialex.service.Audio;

/**
 * Created by alex on 17.11.17.
 */

public class PlayerControlFragment extends Fragment {


    private PlayerControlEventListener eventListener;

    public void setEventListener(PlayerControlEventListener evt) {
        this.eventListener = evt;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_playercontrol, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Button play = getView().findViewById(R.id.play_button);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventListener.onPlayPressed();
            }
        });


        Button prev = getView().findViewById(R.id.previous_button);
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventListener.onPrevButtonPressed();
            }
        });

        Button next = getView().findViewById(R.id.next_button);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventListener.onNextButtonPressed();
            }
        });
    }

    public void updateView(Audio t) {
        TextView view = getView().findViewById(R.id.song_title_box);
        view.setText(t.toString());
    }

    public void updatePlaybackPosition(int position, int duration) {
        TextView playbackText = getView().findViewById(R.id.song_progress_box);


        //int seconds = (duration / 1000) % 60;
        //int minutes = (duration / (1000*60)) % 60;
        //int minutes = (duration / (1000*60*60)) % 24;
        String durationF = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
        );

        String positionF = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(position),
                TimeUnit.MILLISECONDS.toSeconds(position) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(position))
        );

        playbackText.setText(positionF + " " + durationF);

    }
}
