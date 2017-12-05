package ch.usi.inf.gabrialex.datastructures;

/**
 * Created by alex on 27.11.17.
 */

import android.location.Location;

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
    private ArrayList<Date> dates;
    private Location lastLocation;
    private String mood;

    private MusicContext() {
        this.dates = new ArrayList<>();
    }

    public void trackChanged(Audio audio) {
        if (this.activeMedia != null) {
            System.out.println(this.activeMedia);
            for (Date d: this.dates) {
                System.out.println(d);
            }

            System.out.println("----");
        }

        this.activeMedia = audio;
        this.dates.clear();
    }

    public void setLastLocation(Location lastLocation) {
        this.lastLocation = lastLocation;
    }

    public void timestamp() {
        this.dates.add(new Date());
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }
}
