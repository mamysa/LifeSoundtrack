package ch.usi.inf.gabrialex.service;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.Comparator;

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

    public Audio(String data, String tracknum, String title, String album, String artist) {
        this.tracknum = tracknum;
        this.data = data;
        this.title = title;
        this.album = album;
        this.artist = artist;
    }

    public Audio(Parcel p) {
        this.tracknum = p.readString();
        this.data = p.readString();
        this.title = p.readString();
        this.album = p.readString();
        this.artist = p.readString();
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

    @Override
    public String toString() {
        return this.tracknum + " " + this.title + " " + this.album + " " + this.artist;
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
