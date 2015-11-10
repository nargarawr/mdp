package com.example.cxk.m54mdp_psyck_musicplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

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

        // If there is no music queued yet, do nothing
        if (!(musicPlayerService.hasQueue())) {
            Log.d("myapp", "has no queue so will return");
            return;
        }

        if (musicPlayerService.isPlaying()) {
            Log.d("myapp", "is playing, so will pause");
            playPauseButton.setText("Play");
            musicPlayerService.pausePlayback();
        } else {
            Log.d("myapp", "is pause, so will play");
            playPauseButton.setText("Pause");
            musicPlayerService.beginPlayback();
        }

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
        if (requestCode == MEDIA_CONTENT_REQUEST_CODE && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            ArrayList<Song> songs = bundle.getParcelableArrayList(MediaContentProvider.SONG_ARRAY);
            int start_from = bundle.getInt(MediaContentProvider.START_FROM);
            for (Song s : songs) {
                Log.d("myapp", s.getNumber() + " " + s.getName() + " " + s.getDuration());
                Log.d("myapp", s.getFilepath());
            }

            if (musicPlayerService.hasQueue()) {
                Log.d("myapp", "hass queue");
                musicPlayerService.stopPlayback();
                musicPlayerService.clearQueue();
            }

            musicPlayerService.loadMusic(songs, start_from);
            musicPlayerService.beginPlayback();

            Button playPauseButton = (Button) findViewById(R.id.playPauseButton);
            playPauseButton.setText("Pause");
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
