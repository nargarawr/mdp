package com.example.cxk.mupsyck;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.concurrent.TimeUnit;

/**
 * Song
 * <p/>
 * Represents a Song
 */
public class Song implements Parcelable {
    // Fields added in the constructor
    private String artist;
    private String album;
    private String name;
    private String duration;
    private String number;
    private String filepath;

    // Fields added later
    // rating is stored as a string to make the process of parcelling easier
    private String artwork;
    private String rating = "0";

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
        String[] data = new String[8];
        p.readStringArray(data);

        this.artist = data[0];
        this.album = data[1];
        this.name = data[2];
        this.duration = data[3];
        this.number = data[4];
        this.filepath = data[5];
        this.artwork = data[6];
        this.rating = data[7];
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
        return this.album;
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
     * Returns the duration of a song as a MM:SS formatted string
     *
     * @return Duration of a song as a MM:SS Formatted string
     */
    String getDurationAsString() {
        long ms;
        try {
            ms = Long.parseLong(this.duration);
        } catch (Exception e) {
            return this.duration;
        }

        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(ms),
                TimeUnit.MILLISECONDS.toSeconds(ms) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ms))
        );
    }

    /**
     * Returns the file path of the song
     *
     * @return The file path of the song
     */
    public String getFilepath() {
        return this.filepath;
    }

    /**
     * Returns the file path of the song artwork
     *
     * @return the file path of the song artwork
     */
    public String getArtwork() {
        return this.artwork;
    }

    /**
     * Sets the artwork of the song's album
     *
     * @param artwork The path to this song's album's artwork
     */
    public void setArtwork(String artwork){
        this.artwork = artwork;
    }

    /**
     * Returns the song's rating
     *
     * @return Song rating, integer between 0 and 5
     */
    public int getRating(){
        return Integer.parseInt(this.rating);
    }

    /**
     * Sets the song rating
     *
     * @param rating The rating of the song, between 1 and 5
     */
    public void setRating(String rating) {
        this.rating = rating;
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
                this.filepath,
                this.artwork,
                this.rating
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
