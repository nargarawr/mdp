package com.example.cxk.m54mdp_psyck_musicplayer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private MusicPlayerService.MusicPlayerBinder musicPlayerService = null;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.bindService(
                new Intent(this, MusicPlayerService.class),
                serviceConnection,
                Context.BIND_AUTO_CREATE
        );

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String s = intent.getStringExtra("PB FIN");
                Log.d("onReceive", s);
            }
        };
    }

    public void onPlayPauseClick(View v) {
        Log.d("activity", "public void onPlayPauseClick(View v)");
        Button playPauseButton = (Button) findViewById(R.id.playPauseButton);

        // If we're already playing music, we must have already loaded it
        if (!musicPlayerService.isPlaying()) {
            playPauseButton.setText("Pause");
            musicPlayerService.loadMusic(new String[]{"thelasttime.mp3"});
        } else {
            playPauseButton.setText("Play");
        }
        musicPlayerService.togglePlayback();
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            Log.d("g54mdp", "MainActivity onServiceConnected");
            musicPlayerService = (MusicPlayerService.MusicPlayerBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            Log.d("g54mdp", "MainActivity onServiceDisconnected");
            musicPlayerService = null;
        }
    };














    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        Log.d("g54mdp", "MainActivity onDestroy");

        if (serviceConnection != null) {
            unbindService(serviceConnection);
            serviceConnection = null;
        }
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        Log.d("g54mdp", "MainActivity onPause");
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Log.d("g54mdp", "MainActivity onResume");
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub

        Log.d("g54mdp", "MainActivity onStart");
        LocalBroadcastManager.getInstance(this).registerReceiver((broadcastReceiver),
                new IntentFilter("PB FIN")
        );
        super.onStart();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        Log.d("g54mdp", "MainActivity onStop");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }
}
