package ch.usi.inf.gabrialex.datastructures;

/**
 * Created by alex on 27.11.17.
 */

import android.location.Location;
import android.util.Log;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Date;

import ch.usi.inf.gabrialex.service.Audio;

/**
 * Class where we store location, track playtime, and all other useful things we need
 * for ranking.
 */
public class MusicContext {

    private static MusicContext instance;

    /**
     * Get music context instance.
     * @return
     */
    public static MusicContext getInstance() {
        if (instance == null) {
            instance = new MusicContext();
        }
        return instance;
    }

    private Audio activeMedia;
    private ArrayList<DateTime> dates;
    private ArrayList<String> moods;
    private ArrayList<Location> locations;
    private ArrayList<String> weatherConditions;

    private MusicContext() {
        this.dates = new ArrayList<>();
        this.moods = new ArrayList<>();
        this.locations = new ArrayList<>();
        this.weatherConditions = new ArrayList<>();
    }

    public void trackChanged(Audio audio) {
        if (this.activeMedia != null) {
            //todo s
            System.out.println(this.activeMedia);
            for (DateTime d: this.dates) {
                System.out.println(d);
            }
            for (String d: this.weatherConditions) {
                System.out.println(d);
            }
            for (Location l: this.locations) {
                System.out.println(l);

            }

            System.out.println("----");
        }

        // reset context
        this.activeMedia = audio;
        this.dates.clear();
        this.moods.clear();
        this.locations.clear();
        this.weatherConditions.clear();
    }


    public void timestamp() {
        DateTime dateTime = new DateTime(new Date());
        EnvironmentContext context = EnvironmentContext.getInstance();
        this.dates.add(dateTime);
        this.moods.add(context.getMood());
        this.locations.add(context.getLastLocation());
        this.weatherConditions.add(context.getWeather());
    }
}
