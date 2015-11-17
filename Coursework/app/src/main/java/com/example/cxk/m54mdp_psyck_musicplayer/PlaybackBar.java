package com.example.cxk.m54mdp_psyck_musicplayer;

import android.util.Log;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

/**
 * TODO
 */
public class PlaybackBar extends Thread implements Runnable {

    private boolean musicPlaying = false;
    private boolean running = true;

    private int currentSongLength;
    private int currentSongPosition;
    private int phoneWidth;
    private int threadSleepTime;
    private TextView positionDisplay;

    /**
     * TODO
     * @param phoneWidth
     */
    public PlaybackBar(int phoneWidth, TextView positionDisplay){
        Log.d("PlaybackBar", "playbar bar created");
        this.start();

        this.currentSongLength = 0;
        this.currentSongPosition = 0;
        this.phoneWidth = phoneWidth;
        this.positionDisplay = positionDisplay;
    }

    /**
     * TODO
     */
    public void run(){
        while (this.running) {
            if (this.musicPlaying) {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    Log.d("PlaybackBar", "exception");
                }
                Log.d("PlaybackBar", "music is playing, i am moving the progress bar 1px each " + this.threadSleepTime + " milliseconds");
                increaseCurrentPosition();
            }
        }
    }

    /**
     * TODO
     */
    public void increaseCurrentPosition(){
        String currentPos = positionDisplay.getText().toString();
        String[] s = currentPos.split(":");
        long seconds = (Integer.parseInt(s[0])  * 60) + (Integer.parseInt(s[1]));
        seconds++;

        String formatted = String.format("%02d:%02d",
                TimeUnit.SECONDS.toMinutes(seconds),
                TimeUnit.SECONDS.toSeconds(seconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(seconds))
        );

        //positionDisplay.setText(formatted);
    }

    /**
     * TODO
     */
    public void setMusicPlaying(Song s){
        this.musicPlaying = true;
        this.currentSongLength = s.getDuration();
    }

    /**
     * TODO
     */
    public void setMusicStopped(){
        this.musicPlaying = false;
    }
}
