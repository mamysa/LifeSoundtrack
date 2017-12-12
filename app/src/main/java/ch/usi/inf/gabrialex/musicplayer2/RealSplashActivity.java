package ch.usi.inf.gabrialex.musicplayer2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import ch.usi.inf.gabrialex.service.LibraryUpdateTask;

public class RealSplashActivity extends AppCompatActivity implements LibraryUpdateEventListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_splash);
        requestUserForPermissions();

        LibraryUpdateTask libraryUpdateTask = new LibraryUpdateTask(this, this.getContentResolver());
        libraryUpdateTask.setEventListener(this);
        Thread thread = new Thread(libraryUpdateTask);
        thread.start();
    }


    /**
     * requestUserForPermission
     */
    private void requestUserForPermissions() {
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {

            String[] req = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET};
            requestPermissions(req, 10);
        }
    }

    /**
     * On library update complete callback. Start main activity.
     */
    @Override
    public void onLibraryUpdateComplete() {
        //Toast.makeText(this, "Library update complete!", Toast.LENGTH_LONG).show();
        System.out.println("onLibraryUpdateComplete");
        Intent mainIntent = new Intent(RealSplashActivity .this,MainActivity.class);
        RealSplashActivity.this.startActivity(mainIntent);
        RealSplashActivity.this.finish();
    }
}
