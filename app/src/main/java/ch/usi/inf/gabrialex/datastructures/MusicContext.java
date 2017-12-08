package ch.usi.inf.gabrialex.datastructures;

import android.location.Location;

import org.joda.time.DateTime;

import java.util.ArrayList;

import ch.usi.inf.gabrialex.service.Audio;

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
}
