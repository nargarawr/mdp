package com.example.cxk.m54mdp_psyck_musicplayer;

import android.util.Log;

import java.util.ArrayList;

/**
 * Class PlaybackQueue
 * <p/>
 * Used to manage the list of songs we currently have selected, including picking the next and previous songs
 */
public class PlaybackQueue {

    // The list of songs in this queue
    private ArrayList<Song> songs;

    // Our position in the queue
    private int index;

    /**
     * Default constructor - Sets up member variables
     */
    public PlaybackQueue() {
        this.songs = new ArrayList<>();
        this.index = 0;
    }

    /**
     * Adds an ArrayList of Songs to the playback queue
     *
     * @param songs ArrayList of Song to add to the queue
     */
    public void addSongsToQueue(ArrayList<Song> songs) {
        this.songs = songs;
    }

    /**
     * Gets the song at the current index of the playback queue
     *
     * @return Song, the song at the current index of the playback queue
     */
    public Song getSong() {
        return this.songs.get(this.index);
    }

    /**
     * Sets the current position in the playback queue
     *
     * @param index The position
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Moves the index of the playback queue to the next song
     *
     * @param loopingAll Whether to overflow when we reach the end
     * @param loopingOne Whether to play the same song regardless
     * @param shuffle    Whether to select the next song randomly
     * @return boolean Whether finding a new song was successful. If not, stop playback
     */
    public boolean moveToNextSong(boolean loopingAll, boolean loopingOne, boolean shuffle) {
        // If we're repeating this song, do nothing
        if (loopingOne) {
            return true;
        }

        if (shuffle) {
            this.index = (int) (Math.random() * (songs.size()));
            Log.d("myapp", "random song picked - " + this.index);
        } else {
            // Increase the index of which song is playing in the queue
            this.index++;

            // If we overflow the ArrayList, either reset to 0 if looping all, or return false, and stop
            if (this.index >= songs.size()) {
                if (loopingAll) {
                    this.index = 0;
                } else {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Moves the index of the playback queue to the previous song
     *
     * @param loopingAll Whether to overflow when we reach the start
     * @param loopingOne Whether to play the same song regardless
     * @param shuffle    Whether to select the next song (to play) randomly
     */
    public void moveToPreviousSong(boolean loopingAll, boolean loopingOne, boolean shuffle) {
        // If we're repeating this song, do nothing
        if (loopingOne || !loopingAll) {
            return;
        }

        if (shuffle) {
            this.index = (int) (Math.random() * (songs.size() - 1));
            Log.d("myapp", "random song picked - " + this.index);
        } else {
            // Decrease the index of which song is playing in the queue
            this.index--;

            // If we overflow, reset to back of the queue
            if (this.index < 0) {
                this.index = songs.size() - 1;
            }
        }

    }

    /**
     * Returns the number of songs in the playback queue
     *
     * @return The number of songs in the playback queue
     */
    public int length() {
        return songs.size();
    }

    /**
     * Empties the playback queue
     */
    public void clear() {
        this.songs.clear();
    }
}
