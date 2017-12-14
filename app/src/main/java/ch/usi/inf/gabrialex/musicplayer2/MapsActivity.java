package ch.usi.inf.gabrialex.musicplayer2;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MapsActivity extends AppCompatActivity {

    private MapsFragment mapsFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if (savedInstanceState == null) {
            FragmentManager fm = getSupportFragmentManager();
            this.mapsFragment = new MapsFragment();
            if (fm.findFragmentById(R.id.mapsfragment) == null) {
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.add(R.id.mapsfragment, this.mapsFragment);
                transaction.commitNow();
            }
        }
    }
}
