package com.example.cxk.m54mdp_psyck_musicplayer;


import android.os.Environment;
import android.os.Parcelable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class PlaybackQueue {

    private ArrayList<Song> songs;
    private int index;

    public PlaybackQueue() {
        this.songs = new ArrayList<>();
        this.index = 0;
    }

    public void addSongsToQueue (ArrayList<Song> songs) {
        this.songs = songs;
    }

    public Song getSong() {
        return this.songs.get(this.index);
    }

    public void setIndex (int index) {
        this.index = index;
    }

    public void moveToNextSong() {
        this.index++;
        if (this.index >= songs.size()) {
            this.index = 0;
        }
    }

    public void moveToPreviousSong () {
        this.index--;
        if (this.index < 0) {
            this.index = songs.size() - 1;
        }
    }

    public int length() {
        return songs.size();
    }

    public void clear() {
        this.songs.clear();
    }
}
