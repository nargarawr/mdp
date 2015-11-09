package com.example.cxk.m54mdp_psyck_musicplayer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class MusicPlayerService extends Service {

    private final IBinder binder = new MusicPlayerBinder();
    private MusicPlayer musicPlayer;
    private LocalBroadcastManager broadcastManager;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        Log.d("g54mdp", "service onCreate");
        super.onCreate();
        musicPlayer = new MusicPlayer(getApplicationContext());
        broadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        Log.d("g54mdp", "service onBind");
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        Log.d("g54mdp", "service onStartCommand");
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        Log.d("g54mdp", "service onDestroy");
        musicPlayer.running = false;
        musicPlayer = null;
        super.onDestroy();
    }

    @Override
    public void onRebind(Intent intent) {
        // TODO Auto-generated method stub
        Log.d("g54mdp", "service onRebind");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // TODO Auto-generated method stub
        Log.d("g54mdp", "service onUnbind");
        return super.onUnbind(intent);
    }


    public class MusicPlayerBinder extends Binder {
        void togglePlayback() {
            Log.d("binder","void togglePlayback()");
            MusicPlayerService.this.togglePlayback();
        }

        void loadMusic(String[] music) {
            Log.d("binder", "void loadMusic()");
            MusicPlayerService.this.loadMusic(music);
        }

        boolean isPlaying() {
            return MusicPlayerService.this.isPlaying();
        }

        MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }

    public void togglePlayback() {
        musicPlayer.togglePlayback();
        Log.d("service","public void togglePlayback()");
    }

    public void loadMusic(String[] music){
        musicPlayer.loadMusic(music);
        Log.d("binder", "void loadMusic()");
    }

    public boolean isPlaying() {
        return musicPlayer.isPlaying();
    }

    public void playbackComplete(String message) {
        Intent intent = new Intent("PBFIN");
        if(message != null)
            intent.putExtra("PBFIN", message);
        this.broadcastManager.sendBroadcast(intent);
    }
}