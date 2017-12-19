package ch.usi.inf.gabrialex.datastructures;

import android.location.Location;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

/**
 * Created by alex on 15.12.17.
 */

public class RankingReason {
    EnvironmentContext context;
    DateTime startTime;
    DateTime endTime;
    double realPlaytimeRatio;
    double duration;
    double timeRank;
    Location location;
    double locationRank;
    String weather;
    double weatherRank;
    String mood;
    double moodRank;
    double playtimeRatio;
    double totalRank;
    double freshness;

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
    public void setTotalRank(double a) {
        this.totalRank = a;
    }

    public void setDuration(double a) {
        this.duration = a;
    }

    public EnvironmentContext getContext() {
        return context;
    }

    public DateTime getStartTime() {
        return startTime;
    }

    public DateTime getEndTime() {
        return endTime;
    }

    public double getRealPlaytimeRatio() {
        return realPlaytimeRatio;
    }

    public double getTimeRank() {
        return timeRank;
    }

    public Location getLocation() {
        return location;
    }

    public double getLocationRank() {
        return locationRank;
    }

    public String getWeather() {
        return weather;
    }

    public double getWeatherRank() {
        return weatherRank;
    }

    public String getMood() {
        return mood;
    }

    public double getMoodRank() {
        return moodRank;
    }

    public double getPlaytimeRatio() {
        return playtimeRatio;
    }

    public void setEnvironmentContext(EnvironmentContext a) {
        this.context = a;
    }

    public void setFreshness(double a) {
        this.freshness = a;
    }

    public boolean isSuperImportant() {
        if((location.getLongitude()==0&&location.getLatitude()==0)|| location== null || Double.isInfinite(location.getLongitude())|| Double.isInfinite(location.getLongitude())){
            return false;
        }
        else if(startTime == null || endTime == null || mood==null ||weather==null){
            return false;
        }
        return true;
    }

    public double getTotalRank() {
        return this.totalRank;
    }

    public String getInfo() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM HH:mm:ss");
        NumberFormat formatter2 = new DecimalFormat("#0.00");
        String s = "";
        s += "Start Time:\t" + formatter.print(startTime) +"\n";
        s += "End Time:\t" + formatter.print(endTime) +"\n";
        s += "Duration:\t" + formatter2.format(this.duration) +" seconds\n";
        s += "Weather:\t" + weather +"\n";
        s += "Mood:\t" + mood +"\n";

        s += "Time Rank:\t" + formatter2.format(timeRank) +"\n";
        s += "Location Rank:\t" + formatter2.format(locationRank) +"\n";
        s += "Weather Rank:\t" + formatter2.format(weatherRank) +"\n";
        s += "Mood Rank:\t" + formatter2.format(moodRank) +"\n";
        s += "Freshness:\t" + formatter2.format(this.freshness);

        return s;
    }

}
