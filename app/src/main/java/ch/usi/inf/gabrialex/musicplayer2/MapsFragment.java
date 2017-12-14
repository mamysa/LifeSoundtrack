package ch.usi.inf.gabrialex.musicplayer2;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by usi on 14.12.17.
 */

public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private static View view;
    private GoogleMap googleMapInstance;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMapInstance = googleMap;
        googleMap.clear();

    }
}
