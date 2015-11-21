package com.example.cxk.mupsyck;


import android.os.Binder;

import java.util.ArrayList;

/**
 * Class MusicPlayerBinder
 * <p/>
 * Binder to the MusicPlayerService used to interact with the service whilst it's running in the background
 */
public class MusicPlayerBinder extends Binder {

    private MusicPlayerService musicPlayerService;

    /**
     * Default constructor, assigns member variables
     *
     * @param mps The music player service
     */
    public MusicPlayerBinder(MusicPlayerService mps) {
        this.musicPlayerService = mps;
    }

    /**
     * Loads the given music into the music player playback queue
     *
     * @param songs      ArrayList of Songs to load
     * @param start_from What to set the initial playback queue index to
     */
    public void loadMusic(ArrayList<Song> songs, int start_from) {
        musicPlayerService.loadMusic(songs, start_from);
    }

    /**
     * Begin or resume playback of the song at the current index of the playback queue
     */
    public void beginPlayback() {
        musicPlayerService.beginPlayback();
    }

    /**
     * Pauses playback of the current song
     */
    public void pausePlayback() {
        musicPlayerService.pausePlayback();
    }

    /**
     * Stops playback of the current song, resets the music player and resets the datasource
     */
    public void stopPlayback() {
        musicPlayerService.stopPlayback();
    }

    /**
     * Returns whether or not the music player is currently playing
     *
     * @return Whether the music player is currently playing
     */
    public boolean isPlaying() {
        return musicPlayerService.isPlaying();
    }

    /**
     * Returns whether or not the music player has a queue loaded
     *
     * @return Whether the music player has a queue loaded
     */
    public boolean hasQueue() {
        return musicPlayerService.hasQueue();
    }

    /**
     * Clears the playback queue
     */
    public void clearQueue() {
        musicPlayerService.clearQueue();
    }

    /**
     * Plays the next song in the playback queue
     */
    public void playNext() {
        musicPlayerService.playNext();
    }

    /**
     * Plays the previous song in the playback queue
     */
    public void playPrevious() {
        musicPlayerService.playPrevious();
    }

    /**
     * Returns the song at the current index of the music player playback queue
     *
     * @return The Song at the current index of the music player playback queue
     */
    public Song getPlayingSong() {
        return musicPlayerService.getPlayingSong();
    }

    /**
     * Sets the repeat settings of the music player
     *
     * @param loopingAll Whether we should loop all songs
     * @param loopingOne Whether we should loop a single song
     */
    public void setRepeatSettings(boolean loopingAll, boolean loopingOne) {
        musicPlayerService.setRepeatSettings(loopingAll, loopingOne);
    }

    /**
     * Sets the shuffle setting of the music player
     *
     * @param shuffle Whether we should shuffle playback
     */
    public void setShuffleSetting(boolean shuffle) {
        musicPlayerService.setShuffleSetting(shuffle);
    }

    /**
     * Seek to the a particular position in the playback bar, as a percentage of the total width
     *
     * @param percent The percent to seek to
     */
    public void seekToPosition(float percent) {
        musicPlayerService.seekToPosition(percent);
    }

    /**
     * Gets the percentage of how complete this song is (0-100)
     *
     * @return An integer between 0 and 100 for how complete the current song is
     */
    public int getPercentComplete() {
        return musicPlayerService.getPercentComplete();
    }

    /**
     * Send a broadcast from the music player to tell the PlayerActivity that playback has changed
     *
     * @param keepPlaybackPosition Whether or not to keep where we are in the song on the UI, or reset it
     */
    public void sendMusicPlayerBroadcast(boolean keepPlaybackPosition) {
        musicPlayerService.sendMusicPlayerBroadcast(keepPlaybackPosition);
    }
}
