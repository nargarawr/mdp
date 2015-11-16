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
 * Class PlayerActivity
 * <p/>
 * The Main Activity of the application, which deals with the playing of music,
 * as well as the interaction of music playback. Allows users to play and pause
 * a song, select a new song set to play, go to the next song, go to the previous
 * song, set the shuffle and repeat settings, and displays a UI for the currently
 * playing song.
 */
public class PlayerActivity extends AppCompatActivity {

    static final int MEDIA_CONTENT_REQUEST_CODE = 1;

    private MusicPlayerBinder musicPlayerService = null;
    private BroadcastReceiver receiver;

    /**
     * TODO
     *
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

        // Sets up the broadcast receiver
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
     * When the activity is started, register the broadcaster
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
     * When the acitivty is stopped, unregister the broadcaster
     */
    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }

    /**
     * Either play or pause the music playback when the pause/play button is clicked
     *
     * @param v What was clicked
     */
    public void onPlayPauseClick(View v) {
        // If there is no music queued yet, do nothing
        if (!(musicPlayerService.hasQueue())) {
            return;
        }

        if (musicPlayerService.isPlaying()) {
            musicPlayerService.pausePlayback();
        } else {
            musicPlayerService.beginPlayback();
        }
        updatePlayingUI();
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
     * Updates the musicPlayerService repeating settings
     *
     * @param v What was clicked
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
     * Updates the musicPlayerService shuffle setting
     *
     * @param v What was clicked
     */
    public void onShuffleSettingClick(View v) {
        boolean checked = ((CheckBox) v).isChecked();
        musicPlayerService.setShuffleSetting(checked);
    }

    /**
     * What do do when another activity, that was launched from this activty,
     * finishes. If this was from a cancel, do nothing. Otherwise, if there was
     * no music, show an error. If there was music, load it into the musicPlayerService
     * and begin playback.
     *
     * @param requestCode Which activity was launched
     * @param resultCode  The status of the activity at close
     * @param data        Any data the activity returns
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
            // Couldn't find any music, show an error
            Toast toast = Toast.makeText(
                    getApplicationContext(),
                    "Could not find any music on your phone!",
                    Toast.LENGTH_SHORT
            );
            toast.show();
        }
    }

    /**
     * What happens when the activity is destroyed, which will unbind the connection to the service
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
     * Used to connect from the main activity to the music player service
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {

        /** TODO
         * What happens when the connection is created, will create a musicPlayerService
         *
         * @param name
         * @param service
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicPlayerService = (MusicPlayerBinder) service;
        }

        /**
         * TODO
         * What happens when the connection is complete, nullifies the musicPlayerService
         *
         * @param name
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicPlayerService = null;
        }
    };
}
