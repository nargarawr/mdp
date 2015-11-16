package com.example.cxk.m54mdp_psyck_musicplayer;

import android.os.Binder;

import java.util.ArrayList;

public class MusicPlayerBinder extends Binder {

    private MusicPlayerService musicPlayerService;

    public MusicPlayerBinder (MusicPlayerService mps) {
        this.musicPlayerService = mps;
    }

    /**
     * TODO
     */
    public void beginPlayback() {
        musicPlayerService.beginPlayback();
    }

    /**
     * TODO
     * @param songs
     * @param start_from
     */
    public void loadMusic(ArrayList<Song> songs, int start_from) {
        musicPlayerService.loadMusic(songs, start_from);
    }

    /**
     * TODO
     */
    public void pausePlayback() {
        musicPlayerService.pausePlayback();
    }

    /**
     * TODO
     */
    public void stopPlayback() {
        musicPlayerService.stopPlayback();
    }

    /**
     * TODO
     */
    public void clearQueue() {
        musicPlayerService.clearQueue();
    }

    /**
     * TODO
     * @return
     */
    public boolean isPlaying() {
        return musicPlayerService.isPlaying();
    }

    /**
     * TODO
     * @return
     */
    public boolean hasQueue() {
        return musicPlayerService.hasQueue();
    }
    /**
     * TODO
     */
    public void playNext () {
        musicPlayerService.playNext();
    }
    /**
     * TODO
     */
    public void playPrevious () {
        musicPlayerService.playPrevious();
    }

    /**
     * TODO
     * @param loopingAll
     * @param loopingOne
     */
    public void setRepeatSettings(boolean loopingAll, boolean loopingOne ){
        musicPlayerService.setRepeatSettings(loopingAll, loopingOne);
    }

    /**
     *
     * @param shuffle
     */
    public void setShuffleSetting(boolean shuffle) {
        musicPlayerService.setShuffleSetting(shuffle);
    }

    /**
     * @return
     */
    public Song getPlayingSong(){
        return  musicPlayerService.getPlayingSong();
    }


    /**
     * TODO
     * @return
     */
    public  MusicPlayerService getService() {
        return musicPlayerService;
    }
}
