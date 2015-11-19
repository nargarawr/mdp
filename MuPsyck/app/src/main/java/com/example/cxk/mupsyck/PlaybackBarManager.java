package com.example.cxk.mupsyck;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

/**
 * Class PlaybackBarManager
 * <p/>
 * Used to interact with, and update, the playback bar, and the text displaying the current song position
 */
public class PlaybackBarManager extends Thread implements Runnable {

    public static String MUSIC_PROGRESS_BROADCAST = "MUSIC_PROGRESS_BROADCAST";
    public static String CURRENT_TIME = "CURRENT_TIME";
    public static String PERCENT_COMPLETE = "PERCENT_COMPLETE";

    private boolean musicPlaying = false;
    private boolean running = true;

    private TextView currentSongPositionDisplay;
    private TextView songDurationDisplay;

    private LocalBroadcastManager broadcaster;


    /**
     * Default constructor, sets up member variables
     *
     * @param c Application context
     * @param positionDisplay TextView containing the song's current position
     * @param durationDisplay TextView containing the song's duration
     */
    public PlaybackBarManager(Context c, TextView positionDisplay, TextView durationDisplay) {
        this.start();

        this.broadcaster = LocalBroadcastManager.getInstance(c);
        this.currentSongPositionDisplay = positionDisplay;
        this.songDurationDisplay = durationDisplay;
    }

    /**
     * Called when the thread starts. Used to send an update of song progress every second
     */
    public void run() {
        while (this.running) {
            if (this.musicPlaying) {
                // Sends a broadcast containing a string representing the current song time, and
                // a number between 0 and 100 of how far along the progress bar should be
                sendBroadcast(
                        formatAsTime(getCurrentSongPosition() + 1),
                        getSongPercentageComplete()
                );

                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Returns how complete the current song is
     *
     * @return Percentage completion of current song
     */
    public int getSongPercentageComplete() {
        return (getCurrentSongLength() == 0) ? 0 : (int) (1000 * getCurrentSongPosition() / getCurrentSongLength());
    }

    /**
     * Returns the length of the current song
     *
     * @return Length of the current song in seconds
     */
    public long getCurrentSongLength() {
        String currentPos = songDurationDisplay.getText().toString();
        String[] s = currentPos.split(":");
        return (Integer.parseInt(s[0]) * 60) + (Integer.parseInt(s[1]));
    }

    /**
     * Returns the current playback position of the song playing
     *
     * @return The current playback position of the song playing in seconds
     */
    public long getCurrentSongPosition() {
        String currentPos = currentSongPositionDisplay.getText().toString();
        String[] s = currentPos.split(":");
        return (Integer.parseInt(s[0]) * 60) + (Integer.parseInt(s[1]));
    }

    /**
     * Sends a broadcast to the PlayerActivity so the UI can be updated
     *
     * @param formatted An MM:SS formatted string displaying the current position in the current song
     * @param percentComplete How complete the current song is
     */
    public void sendBroadcast(String formatted, int percentComplete) {
        Bundle bundle = new Bundle();
        bundle.putString(CURRENT_TIME, formatted);
        bundle.putInt(PERCENT_COMPLETE, percentComplete);
        Intent intent = new Intent(MUSIC_PROGRESS_BROADCAST);
        intent.putExtras(bundle);

        broadcaster.sendBroadcast(intent);
    }

    /**
     * Given a number of seconds, will return an MM:SS string representing them
     * @param s The number of seconds
     * @return An MM:SS formatted time
     */
    public String formatAsTime(long s) {
        return String.format("%02d:%02d",
                TimeUnit.SECONDS.toMinutes(s),
                TimeUnit.SECONDS.toSeconds(s) - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(s))
        );
    }

    /**
     * Tells the PlaybackBarManager that the song is playing
     */
    public void setMusicPlaying() {
        this.musicPlaying = true;
    }

    /**
     * Tells the PlaybackBarManager that the song is no longer playing
     */
    public void setMusicStopped() {
        this.musicPlaying = false;
    }

    /**
     * Sends a broadcast which will update UI to show the song being {percentComplete}% complete
     *
     * @param percentComplete The percentage complete the song should be
     */
    public void  seekToPosition(int percentComplete) {
        sendBroadcast(
                formatAsTime(this.getCurrentSongLength() * percentComplete/100),
                percentComplete * 10
        );
    }
}
