package ch.usi.inf.gabrialex.musicplayer2;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import ch.usi.inf.gabrialex.service.Audio;

/**
 * Created by alex on 17.11.17.
 */

public class PlaylistFragment extends Fragment {

    PlayerControlEventListener eventListener;

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
        return inflater.inflate(R.layout.fragment_playlist, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void update(ArrayList<Audio> playlist) {
        ArrayAdapter<Audio> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, playlist);
        ListView view = getView().findViewById(R.id.playlist_view);
        view.setAdapter(adapter);
    }
}

