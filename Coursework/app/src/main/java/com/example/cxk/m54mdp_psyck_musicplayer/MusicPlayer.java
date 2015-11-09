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
        Log.d("MusicPlayer", "public void run()");
    }

    public void togglePlayback() {
        Log.d("MusicPlayer", "public void togglePlayback()");
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        } else {
            if (this.hasDataSource) {
                mediaPlayer.start();
            } else {
                try {
                    Uri myUri = Uri.parse(this.playbackQueue.getSong());

                    Context c = this.context;
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            // call playbackComplete() of the binder/service and pass back to main thread
                        }
                    });

                    this.mediaPlayer.setDataSource(this.context, myUri);
                    this.hasDataSource = true;
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    Log.d("MusicPlayer", "playing song: " + this.playbackQueue.getSong());
                } catch (IOException e) {
                    Log.e("IOException", "Could not load the file");
                    e.printStackTrace();
                } catch (Exception e) {
                    Log.d("exception", "there was one");
                    e.printStackTrace();
                }
            }
        }
    }


    public void loadMusicIntoPlaybackQueue(String[] music) {
        Log.d("MusicPlayer", "public void loadMusic()");
        this.playbackQueue.addSongsToQueue(music);
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }
}