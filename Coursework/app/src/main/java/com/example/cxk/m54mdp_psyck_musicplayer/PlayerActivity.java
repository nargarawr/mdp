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
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * TODO
 */
public class PlayerActivity extends AppCompatActivity {

    private MusicPlayerService.MusicPlayerBinder musicPlayerService = null;
    static final int MEDIA_CONTENT_REQUEST_CODE = 1;
    private BroadcastReceiver receiver;

    /**
     * TODO
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // Sets up the binder for the service
        this.bindService(
                new Intent(this, MusicPlayerService.class),
                serviceConnection,
                Context.BIND_AUTO_CREATE
        );

        // Sets up the receiver
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updatePlayingUI();
            }
        };
    }

    /**
     * Messages are passed back whenever the "Next Song", or "Previous Song" buttons are pressed.
     * We use this information to update the UI to reflects these changes. If the song was changed,
     * and we are still playing music, we can update the UI to display the new song's information.
     * If music is no longer playing, we display that information.
     */
    public void updatePlayingUI() {
        Button playPauseButton = (Button) findViewById(R.id.playPauseButton);

        if (musicPlayerService.isPlaying()) {
            playPauseButton.setText("Pause");
        } else {
            playPauseButton.setText("Play");
        }

        TextView currentlyPlaying = (TextView) findViewById(R.id.currentlyPlaying);
        Song playingSong = musicPlayerService.getPlayingSong();
        currentlyPlaying.setText(playingSong.getName());
    }

    /**
     * TODO
     */
    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                receiver,
                new IntentFilter(MusicPlayer.MUSIC_PLAYER_BROADCAST)
        );
    }

    /**
     * TODO
     */
    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
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
     * Called when the browse button is clicked. Launching the MediaContentProvider so users can
     * search for the song they want to play
     *
     * @param v The clicked button
     */
    public void onBrowseClick(View v) {
        Bundle bundle = new Bundle();
        bundle.putString(MediaContentProvider.TYPE, MediaContentProvider.ARTIST);

        Intent intent = new Intent(PlayerActivity.this, MediaContentProvider.class);
        intent.putExtras(bundle);

        startActivityForResult(intent, MEDIA_CONTENT_REQUEST_CODE);
    }

    /**
     * Called when the next button is clicked, plays the next song in the playback queue, or
     * stops playback if we've reached the end of the queue and aren't looping
     *
     * @param v The next button
     */
    public void onNextClick(View v) {
        // If there is no music queued yet, do nothing
        if (musicPlayerService.hasQueue()) {
            musicPlayerService.playNext();
        }
    }

    /**
     * Called when the previous button is clicked, plays the previous song in the playback queue, or
     * replays the song if we've reached the start of the queue and aren't looping
     *
     * @param v The previous button
     */
    public void onPreviousClick(View v) {
        // If there is no music queued yet, do nothing
        if (musicPlayerService.hasQueue()) {
            musicPlayerService.playPrevious();
        }
    }

    /**
     * TODO
     *
     * @param v
     */
    public void onRepeatSettingClick(View v) {
        boolean checked = ((RadioButton) v).isChecked();

        switch (v.getId()) {
            case R.id.radio_repeatNone:
                musicPlayerService.setRepeatSettings(false, false);
                break;
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
     *
     * @param v
     */
    public void onShuffleSettingClick(View v) {
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
            // Enable the playback modifier buttons
            (findViewById(R.id.playPauseButton)).setEnabled(true);
            (findViewById(R.id.nextButton)).setEnabled(true);
            (findViewById(R.id.prevButton)).setEnabled(true);

            // Get songs from the bundle
            Bundle bundle = data.getExtras();
            ArrayList<Song> songs = bundle.getParcelableArrayList(MediaContentProvider.SONG_ARRAY);
            int start_from = bundle.getInt(MediaContentProvider.START_FROM);
            for (Song s : songs) {
                Log.d("myapp", s.getNumber() + " " + s.getName() + " " + s.getDuration());
                Log.d("myapp", s.getFilepath());
            }

            // Overwrite the current queue if there is one
            if (musicPlayerService.hasQueue()) {
                musicPlayerService.stopPlayback();
                musicPlayerService.clearQueue();
            }

            // Load the new music in and start playing it
            musicPlayerService.loadMusic(songs, start_from);
            musicPlayerService.beginPlayback();

            updatePlayingUI();
        } else if (requestCode == MEDIA_CONTENT_REQUEST_CODE && resultCode == MediaContentProvider.NO_MUSIC) {
            Toast toast = Toast.makeText(
                    getApplicationContext(),
                    "We could not find any music on your phone!",
                    Toast.LENGTH_SHORT
            );
            toast.show();
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
