package ch.usi.inf.gabrialex.datastructures;

import android.location.Location;

/**
 * Created by alex on 08.12.17.
 */

/**
 * Where all the sensor data lives.
 */
public class EnvironmentContext {

    private static EnvironmentContext instance;

    /**
     * Get music context instance.
     * @return
     */
    public static synchronized EnvironmentContext getInstance() {
        if (instance == null) {
            instance = new EnvironmentContext();
        }
        return instance;
    }

    private Location lastLocation;
    private String mood;
    private String weather;
    private Object lock;

    public Location getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(Location lastLocation) {
        this.lastLocation = lastLocation;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }
}
