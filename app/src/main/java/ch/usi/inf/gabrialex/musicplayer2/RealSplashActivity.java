package ch.usi.inf.gabrialex.musicplayer2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import ch.usi.inf.gabrialex.service.LibraryUpdateTask;
import ch.usi.inf.gabrialex.service.LocationService;

public class RealSplashActivity extends AppCompatActivity implements LibraryUpdateEventListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_splash);
        if (requestUserForPermissions()) {

            Intent locationIntent = new Intent(this, LocationService.class);
            this.startService(locationIntent);
            this.startLibraryUpdateTask();
        }
    }


    /**
     * requestUserForPermission
     */
    private boolean requestUserForPermissions() {
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED ) {

            String[] req = {Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.INTERNET };
            requestPermissions(req, 10);
            return false;
        }
        return true;
    }

    /**
     * On library update complete callback. Start main activity.
     */
    @Override
    public void onLibraryUpdateComplete(final int numAdded, final int numRemoved) {
        //Toast.makeText(this, "Library update complete!", Toast.LENGTH_LONG).show();
        System.out.println("onLibraryUpdateComplete");
        //Intent mainIntent = new Intent(RealSplashActivity .this,MainActivity.class);
        //RealSplashActivity.this.startActivity(mainIntent);
        //RealSplashActivity.this.finish();
        Looper.prepare();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                // your code to start second activity. Will wait for 3 seconds before calling this method
                Intent moodIntent = new Intent(RealSplashActivity .this,MoodActivity.class);
                moodIntent.putExtra("1", numAdded);
                moodIntent.putExtra("2", numRemoved);
                RealSplashActivity.this.startActivity(moodIntent);
                RealSplashActivity.this.finish();
            }
        }, 1500);
        Looper.loop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        this.startLibraryUpdateTask();
    }

    private void startLibraryUpdateTask() {
        LibraryUpdateTask libraryUpdateTask = new LibraryUpdateTask(this, this.getContentResolver());
        libraryUpdateTask.setEventListener(this);
        Thread thread = new Thread(libraryUpdateTask);
        thread.start();
    }
}
