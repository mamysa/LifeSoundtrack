package ch.usi.inf.gabrialex.musicplayer2;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import ch.usi.inf.gabrialex.datastructures.MusicContext;
import ch.usi.inf.gabrialex.datastructures.MusicContextManager;
import ch.usi.inf.gabrialex.datastructures.Playlist;
import ch.usi.inf.gabrialex.datastructures.RankingReason;
import ch.usi.inf.gabrialex.db.DBHelper;
import ch.usi.inf.gabrialex.db.DBTableAudio;
import ch.usi.inf.gabrialex.db.dbRankableEntry;
import ch.usi.inf.gabrialex.service.Audio;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleMap.InfoWindowAdapter {

    private GoogleMap mMap;
    private int songId;
    private TextView header;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        this.songId = getIntent().getIntExtra("songId", -1);
        this.header = (TextView) findViewById(R.id.mapTotalTextView);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (songId ==-1)  {
            Toast.makeText(this, "No Relevant Information Found", Toast.LENGTH_LONG).show();
            return;
        }
        else {
            fillMap(googleMap);
        }

    }

    private void fillMap(GoogleMap googleMap) {
        Audio song = Playlist.getInstance().findTrackById(songId);
        this.header.setText(song.getTitle() + "\nTotal Rank: " + song.getRank());
        ArrayList<Marker> markers = new ArrayList<Marker>();
        for (RankingReason reason : song.getRankingReasons()) {
            if (reason.isSuperImportant()){
                LatLng latLon = new LatLng(reason.getLocation().getLatitude(), reason.getLocation().getLongitude());
                String info = reason.getInfo();
                markers.add(mMap.addMarker(new MarkerOptions().position(latLon).title("Ranking Info").icon(BitmapDescriptorFactory
                        .defaultMarker(this.getMoodColour(reason.getMood()))).snippet(info)));
            }
        }
        if(!markers.isEmpty()) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Marker marker : markers) {
                builder.include(marker.getPosition());
            }
            LatLngBounds bounds = builder.build();
            mMap.setInfoWindowAdapter(this);
            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;
            int padding = (int) (width * 0.12); // offset from edges of the map 12% of screen

            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);

            googleMap.animateCamera(cu);
        }
        else  {
            Toast.makeText(this, "No Relevant Information Found", Toast.LENGTH_LONG).show();
            return;
        }
    }

    private float getMoodColour(String mood) {
        if (mood == null){
            return BitmapDescriptorFactory.HUE_RED;
        }
        if (mood.equals("happy")){
            return BitmapDescriptorFactory.HUE_GREEN;
        }
        else if (mood.equals("neutral")) {
            return BitmapDescriptorFactory.HUE_YELLOW;
        }
        else //mood.equals("sad")
            return BitmapDescriptorFactory.HUE_BLUE;
    }




    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        LinearLayout info = new LinearLayout(getBaseContext());
        info.setOrientation(LinearLayout.VERTICAL);

        TextView title = new TextView(getBaseContext());
        title.setTextColor(Color.BLACK);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, Typeface.BOLD);
        title.setText(marker.getTitle());

        TextView snippet = new TextView(getBaseContext());
        snippet.setTextColor(Color.GRAY);
        snippet.setText(marker.getSnippet());

        info.addView(title);
        info.addView(snippet);

        return info;
    }
}
