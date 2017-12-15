package ch.usi.inf.gabrialex.datastructures;

import android.location.Location;

import org.joda.time.DateTime;

import java.util.Date;

/**
 * Created by alex on 08.12.17.
 */

/**
 * Where all the sensor data lives.
 */
public class EnvironmentContext {

    private static EnvironmentContext instance;

    public static Location LOCATION_DEFAULT_VALUE = new Location("");
    public static DateTime DATETIME_DEFAULT_VALUE = new DateTime(new Date());

    static {
        LOCATION_DEFAULT_VALUE.setLongitude(Double.NEGATIVE_INFINITY);
        LOCATION_DEFAULT_VALUE.setLatitude(Double.NEGATIVE_INFINITY);
    }

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
    private DateTime dateTime;

    private EnvironmentContext() {
        this.lastLocation = LOCATION_DEFAULT_VALUE;
    }

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

    private void setTime(DateTime dt) {
        this.dateTime = dt;
    }

    public DateTime getDateTime() {
        return this.dateTime;
    }

    /**
     * Copy environment context for ranking in order to avoid environment
     * getting modified mid-ranking process.
     * @return
     */
    public static EnvironmentContext copy() {
        EnvironmentContext envContext = EnvironmentContext.getInstance();
        synchronized (EnvironmentContext.class) {
            EnvironmentContext ctx = new EnvironmentContext();
            ctx.setTime(new DateTime());
            ctx.setLastLocation(envContext.getLastLocation());
            ctx.setMood(envContext.getMood());
            ctx.setWeather(envContext.getWeather());
            return ctx;
        }
    }
}
