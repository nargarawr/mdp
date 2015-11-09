package com.example.cxk.m54mdp_psyck_musicplayer;


import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class PlaybackQueue {

    private ArrayList<String> songs;
    private File musicDirectory;
    private int index;

    public PlaybackQueue() {
        this.songs = new ArrayList<>();
        this.musicDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        this.index = 0;
    }

    public void addSongToQueue(String song) {
        this.songs.add(song);
    }

    public void addSongsToQueue (String[] songs) {
        Collections.addAll(this.songs, songs);
    }

    public String getSong() {
        return this.musicDirectory + "/" + this.songs.get(this.index);
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
}
