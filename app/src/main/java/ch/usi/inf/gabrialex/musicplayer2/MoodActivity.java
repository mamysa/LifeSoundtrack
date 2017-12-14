package ch.usi.inf.gabrialex.musicplayer2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import ch.usi.inf.gabrialex.datastructures.EnvironmentContext;

public class MoodActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        final ImageButton goodMood = (ImageButton) findViewById(R.id.goodMoodButton);
        goodMood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EnvironmentContext.getInstance().setMood("good");
                Intent mainIntent = new Intent(MoodActivity.this,MainActivity.class);
                MoodActivity.this.startActivity(mainIntent);
                MoodActivity.this.finish();
            }
        });
        final ImageButton neutralMood = (ImageButton) findViewById(R.id.neutralMoodButton);
        neutralMood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EnvironmentContext.getInstance().setMood("neutral");
                Intent mainIntent = new Intent(MoodActivity.this,MainActivity.class);
                MoodActivity.this.startActivity(mainIntent);
                MoodActivity.this.finish();
            }
        });
        final ImageButton badMood = (ImageButton) findViewById(R.id.badMoodButton);
        badMood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EnvironmentContext.getInstance().setMood("bad");
                Intent mainIntent = new Intent(MoodActivity.this,MainActivity.class);
                MoodActivity.this.startActivity(mainIntent);
                MoodActivity.this.finish();
            }
        });
    }

}
