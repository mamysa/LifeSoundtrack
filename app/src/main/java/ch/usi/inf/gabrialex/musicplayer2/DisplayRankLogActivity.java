package ch.usi.inf.gabrialex.musicplayer2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import ch.usi.inf.gabrialex.service.PlaylistRankingTask;

public class DisplayRankLogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_rank_log);

        // read debug file
        File logFile = new File(this.getFilesDir(), PlaylistRankingTask.LOG_FILE_NAME);
        ListView view = (ListView) findViewById(R.id.log_view);

        ArrayList<String> parsed = this.parse(logFile);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, parsed);
        view.setAdapter(adapter);
    }


    private ArrayList<String> parse(File file) {
        ArrayList<String> entries = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));

            StringBuilder sb = new StringBuilder();
            String line = reader.readLine();
            while (line != null) {
                if (line.length() == 0) {
                    entries.add(sb.toString());
                    sb = new StringBuilder();
                    line = reader.readLine();
                    continue;
                }
                sb.append(line);
                line = reader.readLine();
            }

            entries.add(sb.toString());
        }
        catch (Exception ex) {
            Log.e("DisplayRankLogActivity", "error opening log file for reading");
        }

        System.out.println(entries.size());
        return entries;
    }



    /*
    private class LogListViewAdapter extends ArrayAdapter<String> {



    }
    */
}
