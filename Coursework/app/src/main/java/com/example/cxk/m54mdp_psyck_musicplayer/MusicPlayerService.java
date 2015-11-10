package com.example.cxk.m54mdp_psyck_musicplayer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;

public class MusicPlayerService extends Service {

    private final IBinder binder = new MusicPlayerBinder();
    private MusicPlayer musicPlayer;
    private LocalBroadcastManager broadcastManager;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        Log.d("myapp", "MusicPlayerService service onCreate");
        super.onCreate();
        musicPlayer = new MusicPlayer(getApplicationContext());
        broadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        Log.d("myapp", "MusicPlayerService service onBind");
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        Log.d("myapp", "MusicPlayerService service onStartCommand");
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        Log.d("myapp", "MusicPlayerService service onDestroy");
        musicPlayer = null;
        super.onDestroy();
    }

    @Override
    public void onRebind(Intent intent) {
        // TODO Auto-generated method stub
        Log.d("myapp", "MusicPlayerService service onRebind");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // TODO Auto-generated method stub
        Log.d("myapp", "MusicPlayerService service onUnbind");
        return super.onUnbind(intent);
    }


    public void beginPlayback() {
        musicPlayer.beginPlayback();
        Log.d("myapp", "MusicPlayerService public void beginPlayback()");
    }

    public void pausePlayback() {
        musicPlayer.pausePlayback();
    }

    public void loadMusic(ArrayList<Song> songs, int start_from) {
        musicPlayer.loadMusicIntoPlaybackQueue(songs, start_from);
        Log.d("myapp", "MusicPlayerService void loadMusic()");
    }

    public boolean isPlaying() {
        return musicPlayer.isPlaying();
    }

    public void playbackComplete(String message) {
        Intent intent = new Intent("PBFIN");
        if (message != null)
            intent.putExtra("PBFIN", message);
        this.broadcastManager.sendBroadcast(intent);
    }

    public boolean hasQueue() {
        return musicPlayer.hasQueue();
    }

    public void stopPlayback() {
        musicPlayer.stopPlayback();
    }

    public void clearQueue() {
        musicPlayer.clearQueue();
    }

    public class MusicPlayerBinder extends Binder {
        void beginPlayback() {
            Log.d("myapp", "MusicPlayerBinder void beginPlayback()");
            MusicPlayerService.this.beginPlayback();
        }

        void loadMusic(ArrayList<Song> songs, int start_from) {
            Log.d("myapp", "MusicPlayerBinder void loadMusic()");
            MusicPlayerService.this.loadMusic(songs, start_from);
        }

        void pausePlayback() {
            MusicPlayerService.this.pausePlayback();
        }

        void stopPlayback() {
            MusicPlayerService.this.stopPlayback();
        }

        void clearQueue() {
            MusicPlayerService.this.clearQueue();
        }

        boolean isPlaying() {
            return MusicPlayerService.this.isPlaying();
        }

        boolean hasQueue() {
            return MusicPlayerService.this.hasQueue();
        }

        MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }
}