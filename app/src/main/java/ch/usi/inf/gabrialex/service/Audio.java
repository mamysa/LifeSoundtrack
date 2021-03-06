package ch.usi.inf.gabrialex.service;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Comparator;

import ch.usi.inf.gabrialex.datastructures.RankingReason;

/**
 * Created by gabriele on 16.11.17.
 */

// @FIXME do we need this one at all? Maybe operate on ID's instead?
public class Audio implements Comparable<Audio>, Parcelable {

    private String tracknum;
    private String data;
    private String title;
    private String album;
    private String artist;
    private int length;
    private int id;
    private double rank;
    private ArrayList<RankingReason> rankingReasons; // FIXME really hacky!

    public ArrayList<RankingReason> getRankingReasons() {
        return rankingReasons;
    }

    public Audio(String data, String tracknum, String title, String album, String artist, int length, int id) {
        this.tracknum = tracknum;
        this.data = data;
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.length = length;
        this.id = id;
        this.rank = 0.0d;
        this.rankingReasons = new ArrayList<>();
    }

    public Audio(Parcel p) {
        this.tracknum = p.readString();
        this.data = p.readString();
        this.title = p.readString();
        this.album = p.readString();
        this.artist = p.readString();
        this.length = p.readInt();
        this.id = p.readInt();
        this.rank = p.readDouble();
        this.rankingReasons = new ArrayList<>();
    }

    public String getTracknum() {
        return tracknum;
    }

    public String getData() {
        return data;
    }

    public String getTitle() {
        return title;
    }

    public String getAlbum() {
        return album;
    }

    public String getArtist() {
        return artist;
    }

    public int getDuration() { return this.length; }

    public int getId() { return this.id; }

    public double getRank() {
        return this.rank;
    }

    public void setRank(double rank) {
        this.rank = rank;
    }

    public void addReason(RankingReason reason) {
        this.rankingReasons.add(reason);
    }

    @Override
    public String toString() {
        return this.id + " " + this.tracknum + " " + this.title + " " + this.album + " " + this.artist;
    }


    @Override
    public int compareTo(@NonNull Audio audio) {
        return Integer.parseInt(this.tracknum) - Integer.parseInt(audio.getTracknum());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.tracknum);
        parcel.writeString(this.data);
        parcel.writeString(this.title);
        parcel.writeString(this.album);
        parcel.writeString(this.artist);
        parcel.writeInt(this.length);
        parcel.writeInt(this.id);
        parcel.writeDouble(this.rank);
    }

    public static final Parcelable.Creator<Audio> CREATOR = new Parcelable.Creator<Audio>() {
        @Override
        public Audio createFromParcel(Parcel parcel) {
            return new Audio(parcel);
        }

        @Override
        public Audio[] newArray(int i) {
            return new Audio[i];
        }
    };
}
