package com.example.cxk.mupsyck;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

/**
 * TODO
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
     * //TODO
     *
     * @param c
     * @param positionDisplay
     * @param durationDisplay
     */
    public PlaybackBarManager(Context c, TextView positionDisplay, TextView durationDisplay) {

        this.start();

        this.broadcaster = LocalBroadcastManager.getInstance(c);

        this.currentSongPositionDisplay = positionDisplay;
        this.songDurationDisplay = durationDisplay;
    }

    /**
     * TODO
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
     * TODO
     *
     * @return
     */
    public int getSongPercentageComplete() {
        return (getCurrentSongLength() == 0) ? 0 : (int) (1000 * getCurrentSongPosition() / getCurrentSongLength());
    }

    /**
     * TODO
     *
     * @return
     */
    public long getCurrentSongLength() {
        String currentPos = songDurationDisplay.getText().toString();
        String[] s = currentPos.split(":");
        return (Integer.parseInt(s[0]) * 60) + (Integer.parseInt(s[1]));
    }

    /**
     * TODO
     *
     * @return
     */
    public long getCurrentSongPosition() {
        String currentPos = currentSongPositionDisplay.getText().toString();
        String[] s = currentPos.split(":");
        return (Integer.parseInt(s[0]) * 60) + (Integer.parseInt(s[1]));
    }

    /**
     * TODO
     * Sends a broadcast to the PlayerActivity so the UI can be updated
     */
    public void sendBroadcast(String formatted, int percentComplete) {
        Bundle bundle = new Bundle();
        bundle.putString(CURRENT_TIME, formatted);
        bundle.putInt(PERCENT_COMPLETE, percentComplete);

        Log.d("newlog", "cur time = " + formatted + ", %complete = " + getCurrentSongPosition() + "/" + getCurrentSongLength() + " " + (1000 * getCurrentSongPosition() / getCurrentSongLength()));


        Intent intent = new Intent(MUSIC_PROGRESS_BROADCAST);
        intent.putExtras(bundle);

        broadcaster.sendBroadcast(intent);
    }

    /**
     * TODO
     */
    public String formatAsTime(long s) {
        return String.format("%02d:%02d",
                TimeUnit.SECONDS.toMinutes(s),
                TimeUnit.SECONDS.toSeconds(s) - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(s))
        );
    }

    /**
     * TODO
     */
    public void setMusicPlaying(Song s) {
        this.musicPlaying = true;
    }

    /**
     * TODO
     */
    public void setMusicStopped() {
        this.musicPlaying = false;
    }
}
