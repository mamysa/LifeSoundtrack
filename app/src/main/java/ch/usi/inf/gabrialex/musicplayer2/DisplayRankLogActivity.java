package ch.usi.inf.gabrialex.musicplayer2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import ch.usi.inf.gabrialex.service.PlaylistRankingTask;

public class DisplayRankLogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_rank_log);

        // read debug file
        File logFile = new File(this.getFilesDir(), PlaylistRankingTask.LOG_FILE_NAME);
        TextView view = (TextView)findViewById(R.id.log_view);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(logFile));
            StringBuilder sb = new StringBuilder();
            String line = reader.readLine();
            while (reader.readLine() != null) {
                sb.append(line);
            }
            view.setText(sb.toString());
        }
        catch (Exception ex) {
            view.setText("Whoopsie!");
        }
    }
}
