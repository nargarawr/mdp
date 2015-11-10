package com.example.cxk.m54mdp_psyck_musicplayer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class PlayerActivity extends AppCompatActivity {

    private MusicPlayerService.MusicPlayerBinder musicPlayerService = null;

    static final int MEDIA_CONTENT_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        this.bindService(
                new Intent(this, MusicPlayerService.class),
                serviceConnection,
                Context.BIND_AUTO_CREATE
        );
    }

    public void onPlayPauseClick(View v) {
        Log.d("myapp", "PlayerActivity  public void onPlayPauseClick(View v)");
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

    public void onBrowseClick(View v) {
        Log.d("myapp", "PlayerActivity public void onBrowseClick(View v)");

        Bundle bundle = new Bundle();
        bundle.putString(MediaContentProvider.TYPE, MediaContentProvider.ARTIST);

        Intent intent = new Intent(PlayerActivity.this, MediaContentProvider.class);
        intent.putExtras(bundle);

        startActivityForResult(intent, MEDIA_CONTENT_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MEDIA_CONTENT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Bundle bundle = data.getExtras();
                String album = bundle.getString(MediaContentProvider.ALBUM);
                String artist = bundle.getString(MediaContentProvider.ARTIST);
                String song = bundle.getString(MediaContentProvider.SONG);
                Log.d("myapp", "Selected album was... " + album);
                Log.d("myapp", "Selected artist was... " + artist);
                Log.d("myapp", "Selected song was... " + song);
            } else if (resultCode == RESULT_CANCELED) {
                Log.d("myapp", "no result from browser activity");
            }
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            Log.d("myapp", "PlayerActivity onServiceConnected");
            musicPlayerService = (MusicPlayerService.MusicPlayerBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            Log.d("myapp", "PlayerActivity onServiceDisconnected");
            musicPlayerService = null;
        }
    };














    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        Log.d("myapp", "PlayerActivity onDestroy");

        if (serviceConnection != null) {
            unbindService(serviceConnection);
            serviceConnection = null;
        }
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        Log.d("myapp", "PlayerActivity onPause");
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Log.d("myapp", "PlayerActivity onResume");
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub

        Log.d("myapp", "PlayerActivity onStart");

        super.onStart();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        Log.d("myapp", "PlayerActivity onStop");

        super.onStop();
    }
}
