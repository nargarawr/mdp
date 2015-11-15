package com.example.cxk.m54mdp_psyck_musicplayer;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

/**
 * TODO
 */
public class MusicPlayer extends Thread implements Runnable {

    private PlaybackQueue playbackQueue;
    private boolean hasDataSource = false;
    private MediaPlayer mediaPlayer;
    private Context context;

    // Playback options
    private boolean loopingOne = false;
    private boolean loopingAll = false;
    private boolean shuffle = false;

    /**
     * Default Constructor for the Music Player
     *
     * Sets up the member variables, as well as the media player's on completion listener
     *
     * @param context Application context
     */
    public MusicPlayer(Context context) {
        this.start();

        this.playbackQueue = new PlaybackQueue();
        this.mediaPlayer = new MediaPlayer();
        this.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        this.context = context;

        this.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            /**
             * Called when the media player finishes playing a song
             *
             * @param mp The media player
             */
            public void onCompletion(MediaPlayer mp) {
                playNext();
            }
        });
    }

    /**
     * Plays the next song in the playback queue, or stops the player if there are none
     */
    public void playNext() {
        // Look at what the next song is
        boolean successful = this.playbackQueue.moveToNextSong(loopingAll, loopingOne, shuffle);

        // Stop playback and reset the player
        stopPlayback();

        // If we found another song to play, play it
        if (successful) {
            beginPlayback();
        }

        Intent intent = new Intent();
        intent.setAction(Broadcaster.PLAYBACK_STOPPED);
        this.context.sendBroadcast(intent);
    }

    /**
     * Plays the prevous song in the playback queue. If we're at the start of the queue, and we're
     * looping, go to the back of the queue, otherwise keep replaying the first song
     */
    public void playPrevious(){
        this.playbackQueue.moveToPreviousSong(loopingAll, loopingOne, shuffle);

        // Stop playback and reset the player, and play again (previous song will always return a return)
        stopPlayback();
        beginPlayback();
    }

    /**
     * TODO
     */
    public void run() {
        Log.d("myapp", "MusicPlayer public void run()");
    }

    /**
     * Pause the player playback
     */
    public void pausePlayback() {
        mediaPlayer.pause();
    }

    /**
     * TODO
     */
    public void beginPlayback() {
        Log.d("myapp", "MusicPlayer public void beginPlayback()");
        if (this.hasDataSource) {
            mediaPlayer.start();
            return;
        }

        Log.d("myapp", "is currently playing - " + this.mediaPlayer.isPlaying());

        try {
            // Get the first song in the playback queue
            Uri myUri = Uri.parse(this.playbackQueue.getSong().getFilepath());

            // Load the song in the music player
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

    /**
     * Loads the provided songs into the playback queue for this media player, and sets which is the first song
     *
     * @param songs ArrayList of Song objects to be added to the queue
     * @param start_from Which song we should start playback from
     */
    public void loadMusicIntoPlaybackQueue(ArrayList<Song> songs, int start_from) {
        this.playbackQueue.addSongsToQueue(songs);
        this.playbackQueue.setIndex(start_from);
    }

    /**
     * Returns whether or not the media player is currently playing music
     *
     * @return boolean Whether the media player is currently playing
     */
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    /**
     * Returns whether a queue exists for the media player
     *
     * @return boolean Whether the media player has a queue
     */
    public boolean hasQueue () {
        return this.playbackQueue.length() > 0;
    }

    /**
     * Ends playback and resets the media player, ready for the next song, if there is one.
     */
    public void stopPlayback() {
        this.mediaPlayer.stop();
        this.mediaPlayer.reset();
        this.hasDataSource = false;
    }

    /**
     * Remove the entire queue for this media player
     */
    public void clearQueue() {
        this.playbackQueue.clear();
    }

    /**
     * Sets the value of the loopingAll and loopingOne variables, so we can change the behaviour of
     * what happens when a song ends
     *
     * @param loopingAll Whether we are looping the entire playlist
     * @param loopingOne Whether we are looping a single song
     */
    public void setRepeatSettings(boolean loopingAll, boolean loopingOne) {
        Log.d("myapp", "Repeat settings: one - " + loopingAll + ", one - " + loopingOne);
        this.loopingAll = loopingAll;
        this.loopingOne = loopingOne;
    }

    public void setShuffleSetting(boolean shuffle) {
        Log.d("myapp", "Shuffle: " + shuffle);
        this.shuffle = shuffle;
    }
}