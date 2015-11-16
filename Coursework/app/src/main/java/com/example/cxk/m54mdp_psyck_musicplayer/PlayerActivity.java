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
import android.util.TypedValue;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
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

    private MusicPlayerBinder musicPlayerBinder = null;
    private BroadcastReceiver receiver;

    // Private variables that record the state of the shuffle and repeat buttons
    private int shuffleState = 0;
    private int repeatState = 0;

    /**
     * Called when the activity is starting.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        if (savedInstanceState == null) {
            // Sets up the binder for the service
            this.bindService(
                    new Intent(this, MusicPlayerService.class),
                    serviceConnection,
                    Context.BIND_AUTO_CREATE
            );
        }

        // Sets up the broadcast receiver
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updatePlayingUI();
            }
        };
    }

    /**
     * Called after {@link #onRestoreInstanceState}, {@link #onRestart}, or
     * {@link #onPause}, for your activity to start interacting with the user.
     */
    @Override
    protected void onResume() {
        super.onResume();

        // If this isn't the first launch, update the UI to reflect any changes that may have happened
        // whilst the activity was not on the top of the stack
        if (musicPlayerBinder != null && musicPlayerBinder.hasQueue()) {
            updatePlayingUI();
        }
    }

    /**
     * Called to retrieve per-instance state from an activity before being killed
     * so that the state can be restored in {@link #onCreate} or
     * {@link #onRestoreInstanceState} (the {@link Bundle} populated by this method
     * will be passed to both).
     *
     * @param savedInstanceState Bundle in which to place the saved state.
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.d("myapp", "Saving instance state");
        savedInstanceState.putBinder("test", musicPlayerBinder);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Messages are passed back whenever the "Next Song", or "Previous Song" buttons are pressed.
     * We use this information to update the UI to reflects these changes. If the song was changed,
     * and we are still playing music, we can update the UI to display the new song's information.
     * If music is no longer playing, we display that information.
     */
    public void updatePlayingUI() {
        ImageButton playButtonImgButton = (ImageButton) findViewById(R.id.playPauseImageButton);

        if (musicPlayerBinder.isPlaying()) {
            playButtonImgButton.setImageResource(R.drawable.pause_button);
        } else {
            playButtonImgButton.setImageResource(R.drawable.play_button);
        }

        TextView currentlyPlaying = (TextView) findViewById(R.id.currentlyPlaying);
        Song playingSong = musicPlayerBinder.getPlayingSong();
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
        if (!(musicPlayerBinder.hasQueue())) {
            return;
        }

        if (musicPlayerBinder.isPlaying()) {
            musicPlayerBinder.pausePlayback();
        } else {
            musicPlayerBinder.beginPlayback();
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
        if (musicPlayerBinder.hasQueue()) {
            musicPlayerBinder.playNext();
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
        if (musicPlayerBinder.hasQueue()) {
            musicPlayerBinder.playPrevious();
        }
    }

    /**
     * Updates the musicPlayerBinder repeating settings
     *
     * @param v What was clicked
     */
    public void onRepeatSettingClick(View v) {
        // Increase the repeat state, or overflow it
        repeatState = (repeatState == 2) ? 0 : (repeatState + 1);

        musicPlayerBinder.setRepeatSettings(
                repeatState == 1, // All
                repeatState == 2 // One
        );

        ImageButton repeatButton = (ImageButton) findViewById(R.id.repeatButton);
        if (repeatState == 1) {
            repeatButton.setImageResource(R.drawable.repeat_all_button);
        } else if (repeatState == 2) {
            repeatButton.setImageResource(R.drawable.repeat_one_button);
        } else {
            repeatButton.setImageResource(R.drawable.repeat_off_button);
        }
    }

    /**
     * Updates the musicPlayerBinder shuffle setting
     *
     * @param v What was clicked
     */
    public void onShuffleSettingClick(View v) {
        // Increase the shuffle state, or overflow it
        shuffleState = (shuffleState == 1) ? 0 : 1;

        musicPlayerBinder.setShuffleSetting(
                (shuffleState == 1)
        );

        ImageButton shuffleButton = (ImageButton) findViewById(R.id.shuffleButton);
        if (shuffleState == 1) {
            shuffleButton.setImageResource(R.drawable.shuffle_on_button);
        } else {
            shuffleButton.setImageResource(R.drawable.shuffle_off_button);
        }
    }

    /**
     * What do do when another activity, that was launched from this activity,
     * finishes. If this was from a cancel, do nothing. Otherwise, if there was
     * no music, show an error. If there was music, load it into the musicPlayerBinder
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
            (findViewById(R.id.playPauseImageButton)).setEnabled(true);
            (findViewById(R.id.nextImageButton)).setEnabled(true);
            (findViewById(R.id.previousImageButton)).setEnabled(true);

            // Get songs from the bundle
            Bundle bundle = data.getExtras();
            ArrayList<Song> songs = bundle.getParcelableArrayList(MediaContentProvider.SONG_ARRAY);
            int start_from = bundle.getInt(MediaContentProvider.START_FROM);

            // Overwrite the current queue if there is one
            if (musicPlayerBinder.hasQueue()) {
                musicPlayerBinder.stopPlayback();
                musicPlayerBinder.clearQueue();
            }

            // Load the new music in and start playing it
            musicPlayerBinder.loadMusic(songs, start_from);
            musicPlayerBinder.beginPlayback();

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

        if (serviceConnection != null) {
            unbindService(serviceConnection);
            serviceConnection = null;
        }
    }

    /**
     * Used to connect from the main activity to the music player service
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {

        /**
         * Called when a connection to the Service has been established, with the IBinder of the communication channel to the Service.
         *
         * @param name    The concrete component name of the service that has been connected.
         * @param service The IBinder of the Service's communication channel, which you can now make calls on.
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicPlayerBinder = (MusicPlayerBinder) service;
        }

        /**
         * Called when a connection to the Service has been lost.
         *
         * @param name The concrete component name of the service whose connection has been lost.
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicPlayerBinder = null;
        }
    };
}
