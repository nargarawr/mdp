package com.example.cxk.mupsyck;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;


/**
 * Class MusicPlayer
 * <p/>
 * Used to interact with a MediaPlayer object, and to mediate interaction from the rest of the application
 * to this media player.
 */
public class MusicPlayer extends Thread {

    static final String MUSIC_PLAYER_BROADCAST = "MUSIC_PLAYER_BROADCAST";

    private PlaybackQueue playbackQueue;
    private boolean hasDataSource = false;
    private MediaPlayer mediaPlayer;
    private Context context;
    private LocalBroadcastManager broadcaster;
    private MusicPlayerService musicPlayerService;


    // Playback options
    private boolean loopingOne = false;
    private boolean loopingAll = false;
    private boolean shuffle = false;

    /**
     * Default Constructor for the Music Player
     * <p/>
     * Sets up the member variables, as well as the media player's on completion listener
     *
     * @param context     Application context
     * @param broadcaster Broadcaster to send messages to PlayerActivity
     */
    public MusicPlayer(Context context, LocalBroadcastManager broadcaster, MusicPlayerService musicPlayerService) {
        this.start();

        this.playbackQueue = new PlaybackQueue();
        this.mediaPlayer = new MediaPlayer();
        this.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        this.context = context;
        this.broadcaster = broadcaster;
        this.musicPlayerService = musicPlayerService;

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
     * Sends a broadcast to the PlayerActivity so the UI can be updated
     *
     * @param keepPlaybackPosition Whether or not the play back position should be keep, or set to 0
     */
    public void sendBroadcast(boolean keepPlaybackPosition) {
        Intent intent = new Intent(MUSIC_PLAYER_BROADCAST);
        intent.putExtra(PlayerActivity.KEEP_PLAYBACK_POSITION, keepPlaybackPosition);
        broadcaster.sendBroadcast(intent);
        musicPlayerService.sendNotification(getPlayingSong());
    }

    /**
     * Loads the provided songs into the playback queue for this media player, and sets which is the first song
     *
     * @param songs      ArrayList of Song objects to be added to the queue
     * @param start_from Which song we should start playback from
     */
    public void loadMusicIntoPlaybackQueue(ArrayList<Song> songs, int start_from) {
        this.playbackQueue.addSongsToQueue(songs);
        this.playbackQueue.setIndex(start_from);
    }

    /**
     * Begins playback of the media player. If there is no data source, it will set one up and play,
     * otherwise it will just start playing from the song at the current index of the playback queue
     */
    public void beginPlayback() {
        if (this.hasDataSource) {
            mediaPlayer.start();
            musicPlayerService.sendNotification(getPlayingSong());
            return;
        }

        try {
            // Get the song at the current index of the playback queue
            Uri myUri = Uri.parse(this.playbackQueue.getSong().getFilepath());

            // Load the song in the music player
            this.mediaPlayer.setDataSource(this.context, myUri);
            this.hasDataSource = true;

            this.mediaPlayer.prepare();
            this.mediaPlayer.start();

            musicPlayerService.sendNotification(getPlayingSong());
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * Pause the player playback
     */
    public void pausePlayback() {
        mediaPlayer.pause();
        musicPlayerService.sendNotification(getPlayingSong());
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
    public boolean hasQueue() {
        return ((this.playbackQueue != null) && (this.playbackQueue.length() > 0));
    }

    /**
     * Remove the entire queue for this media player
     */
    public void clearQueue() {
        this.playbackQueue.clear();
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
        sendBroadcast(false);
    }

    /**
     * Plays the previous song in the playback queue. If we're at the start of the queue, and we're
     * looping, go to the back of the queue, otherwise keep replaying the first song
     */
    public void playPrevious() {
        this.playbackQueue.moveToPreviousSong(loopingAll, loopingOne, shuffle);

        // Stop playback and reset the player, and play again (previous song will always return a return)
        stopPlayback();
        beginPlayback();
        sendBroadcast(false);
    }

    /**
     * Get the song that is currently queued to play
     *
     * @return The Song currently queued to play
     */
    public Song getPlayingSong() {
        return this.playbackQueue.getSong();
    }

    /**
     * Sets the value of the loopingAll and loopingOne variables, so we can change the behaviour of
     * what happens when a song ends
     *
     * @param loopingAll Whether we are looping the entire playlist
     * @param loopingOne Whether we are looping a single song
     */
    public void setRepeatSettings(boolean loopingAll, boolean loopingOne) {
        this.loopingAll = loopingAll;
        this.loopingOne = loopingOne;
    }

    /**
     * Sets the value of the shuffle variable
     *
     * @param shuffle Whether or not we should shuffle the songs
     */
    public void setShuffleSetting(boolean shuffle) {
        this.shuffle = shuffle;
    }

    /**
     * Seek to the a particular position in the playback bar, as a percentage of the total width
     *
     * @param percent The percent to seek to
     */
    public void seekToPosition(float percent) {
        float duration = playbackQueue.getSong().getDuration();
        mediaPlayer.seekTo((int) (duration * (percent / 100)));
    }

    /**
     * Gets the percentage of how complete this song is (0-100)
     *
     * @return An integer between 0 and 100 for how complete the current song is
     */
    public int getPercentComplete() {
        float position = mediaPlayer.getCurrentPosition() / 1000;
        float duration = mediaPlayer.getDuration() / 1000;
        return (int) ((position / duration) * 100);
    }

}