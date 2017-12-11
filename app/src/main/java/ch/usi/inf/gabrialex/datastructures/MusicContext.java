package ch.usi.inf.gabrialex.datastructures;

import android.location.Location;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Date;

import ch.usi.inf.gabrialex.service.Audio;
import ch.usi.inf.gabrialex.service.EventHandler;

/**
 * Created by alex on 08.12.17.
 */

public class MusicContext {

    private Audio activeMedia;
    private ArrayList<DateTime> dates;
    private ArrayList<String> moods;
    private ArrayList<Location> locations;
    private ArrayList<String> weatherConditions;
    private boolean containsData;

    // indicates time when player receives onTrackChange events when switching to activeMedia and
    // switching from active media.
    private DateTime startTimestamp;
    private DateTime endTimestamp;

    public MusicContext() {
        this.dates = new ArrayList<>();
        this.moods = new ArrayList<>();
        this.locations = new ArrayList<>();
    }

    public Audio getActiveMedia() {
        return activeMedia;
    }

    public void setActiveMedia(Audio activeMedia) {
        this.activeMedia = activeMedia;
    }

    public ArrayList<DateTime> getDates() {
        return dates;
    }

    public void addDate(DateTime date) {
        this.dates.add(date);
        this.containsData = true;
    }

    public void setStartTimestamp(DateTime t){
        this.startTimestamp = t;
    }

    public void setEndTimestamp(DateTime t){
        this.endTimestamp = t;
    }

    public DateTime getStartTimestamp() {
        return this.startTimestamp;
    }

    public DateTime getEndTimestamp() {
        return this.endTimestamp;
    }

    public ArrayList<String> getMoods() {
        return moods;
    }

    public void addMood(String mood) {
        this.moods.add(mood);
        this.containsData = true;
    }

    public ArrayList<Location> getLocations() {
        return locations;
    }

    public void addLocation(Location location) {
        this.locations.add(location);
        this.containsData = true;
    }

    public ArrayList<String> getWeatherConditions() {
        return weatherConditions;
    }

    public void setWeatherConditions(ArrayList<String> weatherConditions) {
        this.weatherConditions = weatherConditions;
    }

    public boolean hasData() {
        return this.containsData;
    }

    public boolean hasAudio() {
        return this.activeMedia != null;
    }

    /**
     * If user skips the track by spamming previous/next button while being paused, that will result
     * in data being empty. We initialize it here with current location/datetime, etc, so that we can
     * use it for ranking.
     */
    public void initializeWithEnvironment() {
        DateTime dateTime = new DateTime(new Date());
        this.dates.add(dateTime);
        this.dates.add(dateTime);

        EnvironmentContext env =  EnvironmentContext.getInstance();
        synchronized (EnvironmentContext.class) {
            Location location = env.getLastLocation();
            this.locations.add(location);
            this.locations.add(location);
        }
    }
}
