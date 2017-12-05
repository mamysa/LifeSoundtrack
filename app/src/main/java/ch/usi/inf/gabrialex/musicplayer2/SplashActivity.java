package ch.usi.inf.gabrialex.musicplayer2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import ch.usi.inf.gabrialex.datastructures.MusicContext;

public class SplashActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        requestUserForPermissions();
        /*while (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
        }*/
        final ImageButton goodMood = (ImageButton) findViewById(R.id.goodMoodButton);
        goodMood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MusicContext.getInstance().setMood("good");
                Intent mainIntent = new Intent(SplashActivity.this,MainActivity.class);
                SplashActivity.this.startActivity(mainIntent);
                SplashActivity.this.finish();
            }
        });
        final ImageButton neutralMood = (ImageButton) findViewById(R.id.neutralMoodButton);
        neutralMood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MusicContext.getInstance().setMood("neutral");
                Intent mainIntent = new Intent(SplashActivity.this,MainActivity.class);
                SplashActivity.this.startActivity(mainIntent);
                SplashActivity.this.finish();
            }
        });
        final ImageButton badMood = (ImageButton) findViewById(R.id.badMoodButton);
        badMood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MusicContext.getInstance().setMood("bad");
                Intent mainIntent = new Intent(SplashActivity.this,MainActivity.class);
                SplashActivity.this.startActivity(mainIntent);
                SplashActivity.this.finish();
            }
        });
    }

    private void requestUserForPermissions() {
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            String[] req = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
            requestPermissions(req, 10);
        }
    }
}
