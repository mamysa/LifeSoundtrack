package ch.usi.inf.gabrialex.musicplayer2;

import android.database.Cursor;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import ch.usi.inf.gabrialex.datastructures.MusicContext;
import ch.usi.inf.gabrialex.datastructures.MusicContextManager;
import ch.usi.inf.gabrialex.db.DBHelper;
import ch.usi.inf.gabrialex.db.DBTableAudio;
import ch.usi.inf.gabrialex.db.dbRankableEntry;
import ch.usi.inf.gabrialex.service.Audio;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private int songId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        this.songId = getIntent().getIntExtra("songId", -1);
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
        //MusicContext musicContext = MusicContextManager.getInstance().getMusicContext();
        if (songId ==-1)  {
            Toast.makeText(this, "No Places Found", Toast.LENGTH_LONG).show();
            return;
        }



        doStuff(googleMap);
    }

    private void doStuff(GoogleMap googleMap) {
        DBHelper helper = DBHelper.getInstance(this);
        Log.d("MAPS:", "SONG ID: " + songId);
        String query = String.format(
                " SELECT locationLon, locationLat, listeningDuration FROM %s WHERE %s == %s AND listeningDuration > 90;",
                dbRankableEntry.TABLE_NAME,
                dbRankableEntry.AUDIO_ID, songId);

        Cursor cursor = helper.getReadableDatabase().rawQuery(query, null);

        ArrayList<String> lat = new ArrayList<String>();
        ArrayList<String> lon = new ArrayList<String>();
        if (cursor != null) {
            Log.d("MAPS:", "CURSOR NOT EMPTY");
            cursor.moveToFirst();
            while (cursor != null && !cursor.isAfterLast()) {
                Log.d("MAPS:", "CURSOR LIST DUR: " + cursor.getString(cursor.getColumnIndex(dbRankableEntry.LISTENING_DURATION)));
                lat.add(cursor.getString(cursor.getColumnIndex(dbRankableEntry.LOCATION_LAT)));
                lon.add(cursor.getString(cursor.getColumnIndex(dbRankableEntry.LOCATION_LON)));

                cursor.moveToNext();
            }
            if (!lat.isEmpty()){
                ArrayList<Marker> markers = new ArrayList<Marker>();
                for (int i=0; i<lat.size(); i++) {
                    if (lat.get(i)!= null && lon.get(i)!= null){
                        LatLng latLon = new LatLng(Double.valueOf(lat.get(i)), Double.valueOf(lon.get(i)));
                        markers.add(mMap.addMarker(new MarkerOptions().position(latLon).title("Place "+ i)));
                        //mMap.moveCamera(CameraUpdateFactory.newLatLng());
                    }
                }
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (Marker marker : markers) {
                    builder.include(marker.getPosition());
                }
                LatLngBounds bounds = builder.build();
                int padding = 0; // offset from edges of the map in pixels
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                googleMap.animateCamera(cu);
            }
            else {
                Toast.makeText(this, "No Places Found", Toast.LENGTH_LONG).show();
            }
        }
    }
}
