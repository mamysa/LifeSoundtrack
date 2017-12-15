package ch.usi.inf.gabrialex.datastructures;

import android.location.Location;

import org.joda.time.DateTime;

/**
 * Created by alex on 15.12.17.
 */

public class RankingReason {
    EnvironmentContext context;
    DateTime startTime;
    DateTime endTime;
    double realPlaytimeRatio;
    double timeRank;
    Location location;
    double locationRank;
    String weather;
    double weatherRank;
    String mood;
    double moodRank;
    double playtimeRatio;
    double totalRank;

    public void setTimeInfo(DateTime a, DateTime b, double d) {
        this.startTime = a;
        this.endTime = b;
        this.timeRank = d;
    }

    public void setLocationInfo(Location a, double b) {
        this.location = a;
        this.locationRank = b;
    }

    public void setWeatherInfo(String a, double b) {
        this.weather = a;
        this.weatherRank = b;
    }

    public void setMoodInfo(String a, double b) {
        this.mood = a;
        this.moodRank = b;
    }

    public void setTotalRank(double a, double b) {
        this.playtimeRatio = a;
        this.totalRank = b;
    }

    public void setEnvironmentContext(EnvironmentContext a) {
        this.context = a;
    }

    public boolean isSuperImportant() {
        return true;
    }

    public double getTotalRank() {
        return this.totalRank;
    }
}