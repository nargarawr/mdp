package com.example.cxk.m54mdp_psyck_musicplayer;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;

/**
 * TODO
 */
public class MusicPlayerService extends Service {

    private final IBinder binder = new MusicPlayerBinder();
    private MusicPlayer musicPlayer;
    private LocalBroadcastManager broadcaster;

    /**
     * TODO
     */
    @Override
    public void onCreate() {
        Log.d("myapp", "MusicPlayerService service onCreate");
        super.onCreate();
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
        Log.d("myapp", "MusicPlayerService service onBind");
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
        Log.d("myapp", "MusicPlayerService service onStartCommand");
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

    /**
     * TODO
     */
    public class MusicPlayerBinder extends Binder {
        /**
         * TODO
         */
        void beginPlayback() {
            Log.d("myapp", "MusicPlayerBinder void beginPlayback()");
            MusicPlayerService.this.beginPlayback();
        }

        /**
         * TODO
         * @param songs
         * @param start_from
         */
        void loadMusic(ArrayList<Song> songs, int start_from) {
            Log.d("myapp", "MusicPlayerBinder void loadMusic()");
            MusicPlayerService.this.loadMusic(songs, start_from);
        }

        /**
         * TODO
         */
        void pausePlayback() {
            MusicPlayerService.this.pausePlayback();
        }

        /**
         * TODO
         */
        void stopPlayback() {
            MusicPlayerService.this.stopPlayback();
        }

        /**
         * TODO
         */
        void clearQueue() {
            MusicPlayerService.this.clearQueue();
        }

        /**
         * TODO
         * @return
         */
        boolean isPlaying() {
            return MusicPlayerService.this.isPlaying();
        }

        /**
         * TODO
         * @return
         */
        boolean hasQueue() {
            return MusicPlayerService.this.hasQueue();
        }
        /**
         * TODO
         */
        void playNext () {
            MusicPlayerService.this.playNext();
        }
        /**
         * TODO
         */
        void playPrevious () {
            MusicPlayerService.this.playPrevious();
        }

        /**
         * TODO
         * @param loopingAll
         * @param loopingOne
         */
        void setRepeatSettings(boolean loopingAll, boolean loopingOne ){
            MusicPlayerService.this.setRepeatSettings(loopingAll, loopingOne);
        }

        /**
         *
         * @param shuffle
         */
        void setShuffleSetting(boolean shuffle) {
            MusicPlayerService.this.setShuffleSetting(shuffle);
        }

        /**
         * @return
         */
        Song getPlayingSong(){
            return  MusicPlayerService.this.getPlayingSong();
        }


        /**
         * TODO
         * @return
         */
        MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }
}