package com.example.cxk.m54mdp_psyck_musicplayer;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;

/**
 * TODO
 */
public class MusicPlayerService extends Service {

    private MusicPlayerBinder binder;
    private MusicPlayer musicPlayer;
    private LocalBroadcastManager broadcaster;

    /**
     * TODO
     */
    @Override
    public void onCreate() {
        super.onCreate();
        binder = new MusicPlayerBinder(this);
        broadcaster = LocalBroadcastManager.getInstance(this);
        musicPlayer = new MusicPlayer(getApplicationContext(), broadcaster);
    }

    /**
     * TODO
     * @param arg0
     * @return
     */
    @Override
    public IBinder onBind(Intent arg0) {
        return binder;
    }

    /**
     * TODO
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    /**
     * TODO
     */
    @Override
    public void onDestroy() {
        Log.d("myapp", "MusicPlayerService service onDestroy");
        musicPlayer = null;
        super.onDestroy();
    }

    /**
     * TODO
     * @param intent
     */
    @Override
    public void onRebind(Intent intent) {
        // TODO Auto-generated method stub
        Log.d("myapp", "MusicPlayerService service onRebind");
        super.onRebind(intent);
    }

    /**
     * TODO
     * @param intent
     * @return
     */
    @Override
    public boolean onUnbind(Intent intent) {
        // TODO Auto-generated method stub
        Log.d("myapp", "MusicPlayerService service onUnbind");
        return super.onUnbind(intent);
    }

    /**
     * @return
     */
    public Song getPlayingSong(){
       return  musicPlayer.getPlayingSong();
    }

    /**
     * TODO
     */
    public void beginPlayback() {
        musicPlayer.beginPlayback();
        Log.d("myapp", "MusicPlayerService public void beginPlayback()");
    }

    /**
     * TODO
     */
    public void pausePlayback() {
        musicPlayer.pausePlayback();
    }

    /**
     * TODO
     * @param songs
     * @param start_from
     */
    public void loadMusic(ArrayList<Song> songs, int start_from) {
        musicPlayer.loadMusicIntoPlaybackQueue(songs, start_from);
        Log.d("myapp", "MusicPlayerService void loadMusic()");
    }

    /**
     * TODO
     * @return
     */
    public boolean isPlaying() {
        return musicPlayer.isPlaying();
    }

    /**
     * TODO
     * @return
     */
    public boolean hasQueue() {
        return musicPlayer.hasQueue();
    }

    /**
     * TODO
     */
    public void stopPlayback() {
        musicPlayer.stopPlayback();
    }

    /**
     * TODO
     */
    public void clearQueue() {
        musicPlayer.clearQueue();
    }

    /**
     * TODO
     */
    public void playNext () {
        musicPlayer.playNext();
    }

    /**
     * TODO
     */
    public void playPrevious() {
        musicPlayer.playPrevious();
    }

    /**
     * TODO
     * @param loopingAll
     * @param loopingOne
     */
    public void setRepeatSettings(boolean loopingAll, boolean loopingOne) {
        musicPlayer.setRepeatSettings(loopingAll, loopingOne);
    }
    /**
     *
     * @param shuffle
     */
    public void setShuffleSetting(boolean shuffle) {
        musicPlayer.setShuffleSetting(shuffle);
    }

}
