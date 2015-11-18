package com.example.cxk.mupsyck;



/*
when the phone rotates, the activity is destroyed
i pass in the binder from the old activity, and create a new service connection using that binder
however this doesn't connect to the same service, and therefore the musicPlayer is null and I cannot interact
 */

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Point;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
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
public class PlayerActivity extends Activity {

    static final int MEDIA_CONTENT_REQUEST_CODE = 1;
    static final int MEDIA_SHOWLIST_REQUEST_CODE = 2;

    private MusicPlayerBinder musicPlayerService = null;
    private BroadcastReceiver receiver;

    private ServiceConnection serviceConnection;

    // Private variables that record the state of the shuffle and repeat buttons
    private int shuffleState = 0;
    private int repeatState = 0;

    private final String SAVED_INSTANCE_BINDER = "SAVED_INSTANCE_BINDER";

    private PlaybackBarManager playbackBar;

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

        if (savedInstanceState != null) {
            musicPlayerService = (MusicPlayerBinder) savedInstanceState.getBinder(SAVED_INSTANCE_BINDER);
        }

        this.serviceConnection = new ServiceConnection() {
            /**
             * Called when a connection to the Service has been established, with the IBinder of the communication channel to the Service.
             *
             * @param name    The concrete component name of the service that has been connected.
             * @param service The IBinder of the Service's communication channel, which you can now make calls on.
             */
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                musicPlayerService = (MusicPlayerBinder) service;
            }

            /**
             * Called when a connection to the Service has been lost.
             *
             * @param name The concrete component name of the service whose connection has been lost.
             */
            @Override
            public void onServiceDisconnected(ComponentName name) {
                musicPlayerService = null;
            }
        };

        // Sets up the binder for the service
        this.bindService(
                new Intent(this, MusicPlayerService.class),
                serviceConnection,
                Context.BIND_AUTO_CREATE
        );

        // Creates the playback bar object to manage updating the playback progress bar
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        playbackBar = new PlaybackBarManager(
                getApplicationContext(),
                (TextView) findViewById(R.id.songCurrentPositionDisplay),
                (TextView) findViewById(R.id.songDurationDisplay)
        );

        // Sets up the broadcast receiver
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(MusicPlayer.MUSIC_PLAYER_BROADCAST)) {
                    updatePlayingUI(true);
                } else if (intent.getAction().equals(PlaybackBarManager.MUSIC_PROGRESS_BROADCAST)) {
                    Bundle bundle = intent.getExtras();

                    updatePlaybackPosition(
                            bundle.getString(PlaybackBarManager.CURRENT_TIME),
                            bundle.getInt(PlaybackBarManager.PERCENT_COMPLETE)
                    );
                }
            }
        };

        // If this is the first time the app has been launched, bring up the music selector straight away
        if (savedInstanceState == null) {
            onBrowseClick(null);
        }
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
        if (musicPlayerService != null && musicPlayerService.hasQueue()) {
            updatePlayingUI(true);
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
        Log.d("Binder", "Saving instance state");
        if (musicPlayerService != null && musicPlayerService.hasQueue()) {
            Log.d("Binder", "current queue = " + musicPlayerService.getPlayingSong().getFilepath());
        }

        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBinder(SAVED_INSTANCE_BINDER, this.musicPlayerService);
    }

    /**
     * Messages are passed back whenever the "Next Song", or "Previous Song" buttons are pressed.
     * We use this information to update the UI to reflects these changes. If the song was changed,
     * and we are still playing music, we can update the UI to display the new song's information.
     * If music is no longer playing, we display that information.
     *
     * @param resetTimer whether or not to reset the timer to 00:00 (aka, any time except on pause)
     */
    public void updatePlayingUI(boolean resetTimer) {
        ImageButton playButtonImgButton = (ImageButton) findViewById(R.id.playPauseImageButton);
        Song playingSong = musicPlayerService.getPlayingSong();

        if (musicPlayerService.isPlaying()) {
            playButtonImgButton.setBackgroundResource(R.drawable.pause_default);
            playbackBar.setMusicPlaying(playingSong);
        } else {
            playButtonImgButton.setBackgroundResource(R.drawable.play_default);
            playbackBar.setMusicStopped();
        }

        // Update the UI to display information which Song is playing
        ((TextView) findViewById(R.id.currentlyPlayingSong)).setText(playingSong.getName());
        ((TextView) findViewById(R.id.currentlyPlayingArtist)).setText(playingSong.getArtist());
        if (resetTimer) {
            ((TextView) findViewById(R.id.songCurrentPositionDisplay)).setText("00:00");
            ((ProgressBar) findViewById(R.id.progressBar)).setProgress(0);
        }
        ((TextView) findViewById(R.id.songDurationDisplay)).setText(playingSong.getDurationAsString());
    }

    /**
     * TODO
     */
    public void updatePlaybackPosition(String currentTime, int percentComplete) {
        ((ProgressBar) findViewById(R.id.progressBar)).setProgress(percentComplete);
        ((TextView) findViewById(R.id.songCurrentPositionDisplay)).setText(currentTime);
    }

    /**
     * When the activity is started, register the broadcaster
     */
    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicPlayer.MUSIC_PLAYER_BROADCAST);
        intentFilter.addAction(PlaybackBarManager.MUSIC_PROGRESS_BROADCAST);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                receiver,
                intentFilter
        );
    }

    /**
     * When the activity is stopped, unregister the broadcaster
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
            Log.d("Binder", "queue empty");
            return;
        }
        Log.d("Binder", "current queue = " + musicPlayerService.getPlayingSong().getFilepath());
        Log.d("Binder", musicPlayerService.getPlayingSong() + "");

        if (musicPlayerService.isPlaying()) {
            musicPlayerService.pausePlayback();
        } else {
            musicPlayerService.beginPlayback();
        }
        updatePlayingUI(false);
    }

    /**
     * Called when the browse button is clicked. Launches the MediaContentProvider so users can
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
     * TODO
     *
     * @param v
     */
    public void onShowlistClick(View v) {
        // If there is no music, do nothing
        if (!(musicPlayerService.hasQueue())) {
            return;
        }

        Song currentSong = musicPlayerService.getPlayingSong();

        Bundle bundle = new Bundle();
        bundle.putString(MediaContentProvider.TYPE, MediaContentProvider.SONG);
        bundle.putString(MediaContentProvider.ARTIST, currentSong.getArtist());
        bundle.putString(MediaContentProvider.ALBUM, currentSong.getAlbum());

        Intent intent = new Intent(PlayerActivity.this, MediaContentProvider.class);
        intent.putExtras(bundle);

        startActivityForResult(intent, MEDIA_SHOWLIST_REQUEST_CODE);
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
        // Increase the repeat state, or overflow it
        repeatState = (repeatState == 2) ? 0 : (repeatState + 1);

        musicPlayerService.setRepeatSettings(
                repeatState == 1, // All
                repeatState == 2 // One
        );

        ImageButton repeatButton = (ImageButton) findViewById(R.id.repeatButton);
        if (repeatState == 1) {
            repeatButton.setBackgroundResource(R.drawable.repeat_all);
        } else if (repeatState == 2) {
            repeatButton.setBackgroundResource(R.drawable.repeat_one);
        } else {
            repeatButton.setBackgroundResource(R.drawable.repeat_off);
        }
    }

    /**
     * Updates the musicPlayerService shuffle setting
     *
     * @param v What was clicked
     */
    public void onShuffleSettingClick(View v) {
        // Increase the shuffle state, or overflow it
        shuffleState = (shuffleState == 1) ? 0 : 1;

        musicPlayerService.setShuffleSetting(
                (shuffleState == 1)
        );

        ImageButton shuffleButton = (ImageButton) findViewById(R.id.shuffleButton);
        if (shuffleState == 1) {
            shuffleButton.setBackgroundResource(R.drawable.shuffle_on);
        } else {
            shuffleButton.setBackgroundResource(R.drawable.shuffle_off);
        }
    }

    /**
     * What do do when another activity, that was launched from this activity,
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
        if ((requestCode == MEDIA_CONTENT_REQUEST_CODE || requestCode == MEDIA_SHOWLIST_REQUEST_CODE) && resultCode == RESULT_OK) {
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

            // Change the top bar to display song name, and change the browse music button to be an icon
            findViewById(R.id.browseButton).setVisibility(View.GONE);
            findViewById(R.id.browseButtonIcon).setVisibility(View.VISIBLE);
            findViewById(R.id.browsePlaylistIcon).setVisibility(View.VISIBLE);

            updatePlayingUI(true);
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


}
