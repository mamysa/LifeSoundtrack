package ch.usi.inf.gabrialex.datastructures;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by alex on 15.12.17.
 */

public class RankWeightPreferences {
    // importance parameters are in [0,1] range.
    public static final double IMPORTANCE_MIN = 0.0;
    public static final double IMPORTANCE_MAX = 1.0;

    public static double IMPORTANCE_TIME = 1.0;
    public static double IMPORTANCE_LOCATION = 1.0;
    public static double IMPORTANCE_WEATHER = 1.0;

    private static final String WEIGHT_PREFS_FILENAME = "weight_prefs.txt";

    static public void readPreferences(Context context) {
        File file = new File(context.getFilesDir(), WEIGHT_PREFS_FILENAME);

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            IMPORTANCE_TIME = Double.parseDouble(reader.readLine().trim());
            IMPORTANCE_LOCATION = Double.parseDouble(reader.readLine().trim());
            IMPORTANCE_WEATHER = Double.parseDouble(reader.readLine().trim());
            reader.close();
        }
        catch (FileNotFoundException ex) {
            Log.i("readRankWeightPrefs", "could not locate files, creating file");
            writePreferences(context);
        }
        catch (IOException ex) {
            Log.e("readRankWeightPrefs", "IOException");
        }
    }

    static public void writePreferences(Context context) {
        File file = new File(context.getFilesDir(), WEIGHT_PREFS_FILENAME);
        try {
            FileWriter fileWriter = new FileWriter(file, false);
            fileWriter.write(IMPORTANCE_TIME + "\n");
            fileWriter.write(IMPORTANCE_LOCATION + "\n");
            fileWriter.write(IMPORTANCE_WEATHER + "\n");
            fileWriter.close();
        }
        catch (IOException ex) {
            Log.e("writeRankWeightPrefs", "IOException");
        }
    }
}
