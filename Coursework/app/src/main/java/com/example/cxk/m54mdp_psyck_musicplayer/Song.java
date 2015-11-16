package com.example.cxk.m54mdp_psyck_musicplayer;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Song
 * <p/>
 * Represents a Song
 */
public class Song implements Parcelable {
    private String artist;
    private String album;
    private String name;
    private String duration;
    private String number;
    private String filepath;

    /**
     * Default Constructor - Sets up the song object and assigns member variables
     *
     * @param artist   The name of the artist
     * @param album    The name of the album
     * @param name     The name of the song
     * @param duration The duration of the song, in milli seconds
     * @param number   The track number of this song
     */
    public Song(String artist, String album, String name, String duration, String number, String filepath) {
        this.artist = artist;
        this.album = album;
        this.name = name;
        this.duration = duration;
        this.number = number;
        this.filepath = filepath;
    }

    /**
     * Parcelable Constructor - Creates a Song object from a parcel
     *
     * @param p The parcel to read from
     */
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

    /**
     * Returns the song's artist
     *
     * @return The name of the artist
     */
    public String getArtist() {
        return this.artist;
    }

    /**
     * Returns the song's album
     *
     * @return The name of the album
     */
    public String getAlbum() {
        return this.getAlbum();
    }

    /**
     * Returns the song's name
     *
     * @return The name of the song
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the song's duration
     *
     * @return The duration of the song in milli seconds
     */
    int getDuration() {
        return Integer.valueOf(this.duration);
    }

    /**
     * Returns the song's track number
     *
     * @return The track number of the song
     */
    public int getNumber() {
        return Integer.valueOf(this.number);
    }

    /**
     * Returns the file path of the songg
     *
     * @return The file path of the song
     */
    public String getFilepath() {
        return this.filepath;
    }

    /**
     * Not used
     *
     * @return 0
     */
    public int describeContents() {
        return 0;
    }

    /**
     * Writes this object to a parcel
     *
     * @param p     The parcel to write to
     * @param flags Any flags required
     */
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

    /**
     * Used to create a Parcelable version of this Class
     */
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        /**
         * Creates a Song object from a parcel
         * @param p The parcel to create from
         * @return The song created
         */
        public Song createFromParcel(Parcel p) {
            return new Song(p);
        }

        /**
         * Used to prepare the parcelable array
         * @param size The size of the array
         * @return The size of the parcelable array
         */
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };
}
