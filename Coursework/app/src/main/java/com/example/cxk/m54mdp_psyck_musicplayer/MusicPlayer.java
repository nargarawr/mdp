package com.example.cxk.m54mdp_psyck_musicplayer;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Button;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MusicPlayer extends Thread implements Runnable {

    private static final String ACTION_PLAY = "ACTION_PLAY";
    public boolean running = true;
    private PlaybackQueue playbackQueue;
    private boolean hasDataSource = false;

    MediaPlayer mediaPlayer;
    Context context;


    public MusicPlayer(Context context) {
        this.start();

        this.playbackQueue = new PlaybackQueue();
        this.mediaPlayer = new MediaPlayer();
        this.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        this.context = context;
    }
    public void run() {
        Log.d("myapp", "MusicPlayer public void run()");
    }

    public void pausePlayback() {
        mediaPlayer.pause();
    }

    public void beginPlayback() {
        Log.d("myapp", "MusicPlayer public void beginPlayback()");
        if (this.hasDataSource) {
            Log.d("myapp", "has datasorce");
            mediaPlayer.start();
            return;
        }

        Log.d("myapp", "is currently playing - " + this.mediaPlayer.isPlaying());

        try {
            Log.d("myapp", "trying to play");
            Uri myUri = Uri.parse(this.playbackQueue.getSong().getFilepath());
            this.mediaPlayer.setDataSource(this.context, myUri);
            this.hasDataSource = true;
            this.mediaPlayer.prepare();
            this.mediaPlayer.start();
            Log.d("myapp", "MusicPlayer playing song: " + this.playbackQueue.getSong().getFilepath());
        } catch (IOException e) {
            Log.d("myapp", "MusicPlayer Could not load the file");
            e.printStackTrace();
        } catch (Exception e) {
            Log.d("myapp", "There was a exception");
            e.printStackTrace();
        }
    }

    public void loadMusicIntoPlaybackQueue(ArrayList<Song> songs, int start_from) {
        Log.d("myapp", "MusicPlayer public void loadMusic()");
        this.playbackQueue.addSongsToQueue(songs);
        this.playbackQueue.setIndex(start_from);
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public boolean hasQueue () {
        return this.playbackQueue.length() > 0;
    }

    public void stopPlayback() {
        this.mediaPlayer.stop();
        this.hasDataSource = false;
    }

    public void clearQueue() {
        this.playbackQueue.clear();
    }
}