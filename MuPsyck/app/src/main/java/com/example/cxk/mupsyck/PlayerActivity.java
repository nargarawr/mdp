package com.example.cxk.mupsyck;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
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

    private MusicPlayerBinder musicPlayerBinder = null;
    private BroadcastReceiver receiver;
    private ServiceConnection serviceConnection;
    private Intent musicServiceIntent;

    // Private variables that record the state of the shuffle and repeat buttons
    private int shuffleState;
    private int repeatState;

    private String albumArtworkPath = "";
    private int phoneWidth;

    private final String SAVED_INSTANCE_BINDER = "SAVED_INSTANCE_BINDER";

    private PlaybackBarManager playbackBar;

    SharedPreferences sharedPref;

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

        // Get width of phone screen
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        phoneWidth = size.x;

        if (savedInstanceState != null) {
            musicPlayerBinder = (MusicPlayerBinder) savedInstanceState.getBinder(SAVED_INSTANCE_BINDER);

            // Displays the album artwork
            albumArtworkPath = savedInstanceState.getString(MediaContentProvider.ALBUM_ART_PATH);
            Bitmap bmImg = BitmapFactory.decodeFile(albumArtworkPath);
            BitmapDrawable background = new BitmapDrawable(bmImg);
            findViewById(R.id.backgroundImg).setBackgroundDrawable(background);
        }

        // Creates a service connection
        this.serviceConnection = new ServiceConnection() {
            /**
             * Called when a connection to the Service has been established, with the IBinder of the communication channel to the Service.
             *
             * @param name    The concrete component name of the service that has been connected.
             * @param service The IBinder of the Service's communication channel, which you can now make calls on.
             */
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                if (musicPlayerBinder == null) {
                    musicPlayerBinder = (MusicPlayerBinder) service;
                }

                // Check for any shared preferences
                sharedPref = getPreferences(Context.MODE_PRIVATE);
                lookForSharedPreferences();
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

        // Binds to the service
        musicServiceIntent = new Intent(this, MusicPlayerService.class);
        this.bindService(
                musicServiceIntent,
                serviceConnection,
                Context.BIND_AUTO_CREATE
        );

        // Creates the playback bar object to manage updating the playback progress bar
        playbackBar = new PlaybackBarManager(
                getApplicationContext(),
                (TextView) findViewById(R.id.songCurrentPositionDisplay),
                (TextView) findViewById(R.id.songDurationDisplay)
        );

        // Add an on click listener to the progress bar so we can seek
        findViewById(R.id.progressBar).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN && musicPlayerBinder != null) {
                    int percentComplete = (int) (100 * event.getX() / phoneWidth);

                    // Seek in the music player
                    musicPlayerBinder.seekToPosition(percentComplete);

                    // Update the interface
                    playbackBar.seekToPosition(percentComplete);
                }
                return true;
            }
        });

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

        // Set initial values of shuffle and repeat states
        shuffleState = 0;
        repeatState = 0;

        // If this is the first time the app has been launched, bring up the music selector straight away
        // Otherwise, we will load music, so we don't need to display the browse button
        if (savedInstanceState == null) {
            onBrowseClick(null);
        } else {
            findViewById(R.id.browseButton).setVisibility(View.GONE);
            findViewById(R.id.browseButtonIcon).setVisibility(View.VISIBLE);
            findViewById(R.id.browsePlaylistIcon).setVisibility(View.VISIBLE);
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
        if (musicPlayerBinder != null && musicPlayerBinder.hasQueue()) {
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
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBinder(SAVED_INSTANCE_BINDER, this.musicPlayerBinder);
        savedInstanceState.putString(MediaContentProvider.ALBUM_ART_PATH, albumArtworkPath);

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
        Song playingSong = musicPlayerBinder.getPlayingSong();

        if (musicPlayerBinder.isPlaying()) {
            playButtonImgButton.setBackgroundResource(R.drawable.pause_default);
            playbackBar.setMusicPlaying();
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
        if (!(musicPlayerBinder.hasQueue())) {
            return;
        }

        if (musicPlayerBinder.isPlaying()) {
            musicPlayerBinder.pausePlayback();
        } else {
            musicPlayerBinder.beginPlayback();
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
    public void onPlaylistClick(View v) {
        // If there is no music, do nothing
        if (!(musicPlayerBinder.hasQueue())) {
            return;
        }

        Song currentSong = musicPlayerBinder.getPlayingSong();

        Bundle bundle = new Bundle();
        bundle.putString(MediaContentProvider.TYPE, MediaContentProvider.SONG);
        bundle.putString(MediaContentProvider.ARTIST, currentSong.getArtist());
        bundle.putString(MediaContentProvider.ALBUM, currentSong.getAlbum());
        bundle.putBoolean(MediaContentProvider.DISPLAY_PLAYLIST, true);

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
     * Updates the musicPlayerBinder repeating settings and saves them as a shared preference
     *
     * @param v What was clicked
     */
    public void onRepeatSettingClick(View v) {
        updateRepeatSetting();

        // Save this as a shared preference
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.preference_repeat), repeatState);
        editor.apply();
    }

    /**
     * TODO
     */
    public void updateRepeatSetting() {
        if (musicPlayerBinder == null) {
            return;
        }

        // Increase the repeat state, or overflow it
        repeatState = (repeatState == 2) ? 0 : (repeatState + 1);

        musicPlayerBinder.setRepeatSettings(
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
     * Updates the musicPlayerBinder shuffle setting and saves them as a shared preference
     *
     * @param v What was clicked
     */
    public void onShuffleSettingClick(View v) {
        updateShuffleSetting();

        // Save this as a shared preference
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.preference_shuffle), shuffleState);
        editor.apply();
    }

    /**
     * TODO
     */
    public void updateShuffleSetting() {
        if (musicPlayerBinder == null) {
            return;
        }

        // Increase the shuffle state, or overflow it
        shuffleState = (shuffleState == 1) ? 0 : 1;

        musicPlayerBinder.setShuffleSetting(
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
     * no music, show an error. If there was music, load it into the musicPlayerBinder
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
            String albumArtPath = bundle.getString(MediaContentProvider.ALBUM_ART_PATH);

            // Displays the album artwork
            albumArtworkPath = albumArtPath;
            Bitmap bmImg = BitmapFactory.decodeFile(albumArtPath);
            BitmapDrawable background = new BitmapDrawable(bmImg);
            findViewById(R.id.backgroundImg).setBackgroundDrawable(background);

            // Overwrite the current queue if there is one
            if (musicPlayerBinder.hasQueue()) {
                musicPlayerBinder.stopPlayback();
                musicPlayerBinder.clearQueue();
            }

            // Load the new music in and start playing it
            musicPlayerBinder.loadMusic(songs, start_from);
            musicPlayerBinder.beginPlayback();

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
        /*
        if (!(musicPlayerBinder.isPlaying())) {
            unbindService(serviceConnection);
            stopService(musicServiceIntent);
        }
        */
    }

    /**
     *TODO
     */
    public void lookForSharedPreferences() {
        // Get shuffle setting and invert it, because then our function can set it correctly
        shuffleState = sharedPref.getInt(getString(R.string.preference_shuffle), 0);
        shuffleState = (shuffleState == 1) ? 0 : 1;
        updateShuffleSetting();

        // Get repeat setting
        repeatState = sharedPref.getInt(getString(R.string.preference_repeat), 0);
        repeatState = (repeatState == 0) ? 2 : (repeatState - 1);
        updateRepeatSetting();

    }
}
