package com.example.cxk.m54mdp_psyck_musicplayer;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

/**
 *
 */
public class Song implements Parcelable {
    private String artist;
    private String album;
    private String name;
    private String duration;
    private String number;
    private String filepath;

    /**
     * Default Constructer
     *
     * @param artist   a
     * @param album    b
     * @param name     c
     * @param duration d
     * @param number   e
     */
    public Song(String artist, String album, String name, String duration, String number, String filepath) {
        this.artist = artist;
        this.album = album;
        this.name = name;
        this.duration = duration;
        this.number = number;
        this.filepath = filepath;
    }

    public String getArtist() {
        return this.artist;
    }

    public String getAlbum() {
        return this.getAlbum();
    }

    public String getName() {
        return this.name;
    }

    public int getDuration() {
        return Integer.valueOf(this.duration);
    }

    public int getNumber() {
        return Integer.valueOf(this.number);
    }

    public String getFilepath() {
        return this.filepath;
    }

    public Song(Parcel p) {
        String[] data = new String[6];
        p.readStringArray(data);

        this.artist = data[0];
        this.album = data[1];
        this.name = data[2];
        this.duration = data[3];
        this.number = data[4];
        this.filepath = data[5];
    }

    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel p, int flags) {
        p.writeStringArray(new String[]{
                this.artist,
                this.album,
                this.name,
                this.duration,
                this.number,
                this.filepath
        });
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Song createFromParcel(Parcel p) {
            return new Song(p);
        }

        public Song[] newArray(int size) {
            return new Song[size];
        }
    };
}
