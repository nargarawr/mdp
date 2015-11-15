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
import android.widget.CheckBox;
import android.widget.RadioButton;

import java.util.ArrayList;

/**
 * TODO
 */
public class PlayerActivity extends AppCompatActivity {

    private MusicPlayerService.MusicPlayerBinder musicPlayerService = null;
    static final int MEDIA_CONTENT_REQUEST_CODE = 1;

    /**
     * TODO
     *
     * @param savedInstanceState
     */
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

    /**
     * TODO
     *
     * @param v
     */
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

    /**
     * TODO
     *
     * @param v
     */
    public void onBrowseClick(View v) {
        Log.d("myapp", "PlayerActivity public void onBrowseClick(View v)");

        Bundle bundle = new Bundle();
        bundle.putString(MediaContentProvider.TYPE, MediaContentProvider.ARTIST);

        Intent intent = new Intent(PlayerActivity.this, MediaContentProvider.class);
        intent.putExtras(bundle);

        startActivityForResult(intent, MEDIA_CONTENT_REQUEST_CODE);
    }

    /**
     * TODO
     *
     * @param v
     */
    public void onNextClick(View v) {
        Log.d("myapp", "PlayerActivity public void onNextClick(View v)");

        // If there is no music queued yet, do nothing
        if (!(musicPlayerService.hasQueue())) {
            Log.d("myapp", "has no queue so will return");
            return;
        }

        musicPlayerService.playNext();
    }

    /**
     * TODO
     *
     * @param v
     */
    public void onPreviousClick(View v) {
        Log.d("myapp", "PlayerActivity public void onPreviousClick(View v)");

        // If there is no music queued yet, do nothing
        if (!(musicPlayerService.hasQueue())) {
            Log.d("myapp", "has no queue so will return");
            return;
        }

        musicPlayerService.playPrevious();
    }

    /**
     * TODO
     * @param v
     */
    public void onRepeatSettingClick(View v) {
        boolean checked = ((RadioButton) v).isChecked();

        // Check which radio button was clicked
        switch (v.getId()) {
            case R.id.radio_repeatAll:
                musicPlayerService.setRepeatSettings(checked, !checked);
                break;
            case R.id.radio_repeatOne:
                musicPlayerService.setRepeatSettings(!checked, checked);
                break;
        }
    }

    /**
     * TODO
     * @param v
     */
    public void onShuffleSettingClick(View v){
        boolean checked = ((CheckBox) v).isChecked();
        musicPlayerService.setShuffleSetting(checked);
    }

    /**
     * TODO
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
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

    /**
     * TODO
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("myapp", "PlayerActivity onDestroy");

        if (serviceConnection != null) {
            unbindService(serviceConnection);
            serviceConnection = null;
        }
    }

    /**
     * TODO
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {

        /**
         * TODO
         * @param name
         * @param service
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            Log.d("myapp", "PlayerActivity onServiceConnected");
            musicPlayerService = (MusicPlayerService.MusicPlayerBinder) service;
        }

        /**
         * TODO
         * @param name
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            Log.d("myapp", "PlayerActivity onServiceDisconnected");
            musicPlayerService = null;
        }
    };
}
