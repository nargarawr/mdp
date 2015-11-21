package com.example.cxk.mupsyck;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
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

    static final String KEEP_PLAYBACK_POSITION = "KEEP_PLAYBACK_POSITION";

    private MusicPlayerBinder musicPlayerBinder = null;
    private BroadcastReceiver receiver;
    private ServiceConnection serviceConnection;
    private Intent musicServiceIntent;
    private TelephonyStateListener telephonyStateListener;
    private PlaybackBarManager playbackBar;
    private SharedPreferences sharedPref;

    private int shuffleState;
    private int repeatState;
    private String albumArtworkPath = "";
    private int phoneWidth;

    /**
     * Called when the activity is starting.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState. <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("cxk-db", "PlayerActivity::onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // Get width of phone screen
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        phoneWidth = size.x;

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
                Log.d("cxk-db", "serviceConnection::onServiceConnected()");
                if (musicPlayerBinder == null) {
                    musicPlayerBinder = (MusicPlayerBinder) service;

                    // Check for any shared preferences
                    sharedPref = getPreferences(Context.MODE_PRIVATE);
                    lookForSharedPreferences();

                    // Hook up the telephony state listener to listen for phone calls
                    telephonyStateListener = new TelephonyStateListener(musicPlayerBinder);
                    try {
                        TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
                        telephonyManager.listen(telephonyStateListener, PhoneStateListener.LISTEN_CALL_STATE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Hook up the headphone state listener to listen for headphones being plugged in
                    IntentFilter receiverFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
                    HeadphoneStateListener listener = new HeadphoneStateListener(musicPlayerBinder);
                    registerReceiver(listener, receiverFilter);
                }
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

        this.startService(musicServiceIntent);
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
                    float percentComplete = (100 * event.getX() / phoneWidth);

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
                onBroadcastReceived(context, intent);
            }
        };


        // Set initial values of shuffle and repeat states
        shuffleState = 0;
        repeatState = 0;

        if (musicPlayerBinder != null && musicPlayerBinder.hasQueue()) {
            Log.d("cxk-db", "playing .. " + musicPlayerBinder.getPlayingSong().getName());
        }
    }

    /**
     * Called after onRestoreInstanceState, onRestart, or
     * #onPause, for your activity to start interacting with the user.
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("cxk-db", "on resumed");
        // If this isn't the first launch, update the UI to reflect any changes that may have happened
        // whilst the activity was not on the top of the stack
        if (musicPlayerBinder != null && musicPlayerBinder.hasQueue()) {
            Log.d("cxk-db", "on resumed 2");
            updatePlayingUI(true);
            showAlbumArtwork(albumArtworkPath);
            playbackBar.seekToPosition(musicPlayerBinder.getPercentComplete());
        }
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
            ((TextView) findViewById(R.id.songCurrentPositionDisplay)).setText(R.string.time_zero);
            ((ProgressBar) findViewById(R.id.progressBar)).setProgress(0);
        }
        ((TextView) findViewById(R.id.songDurationDisplay)).setText(playingSong.getDurationAsString());

        setStarsOn(musicPlayerBinder.getPlayingSong().getRating());
    }

    /**
     * Updates the playback bar and the text view representing the current position in the song
     *
     * @param currentTime     The current time of the song as an MM:SS string
     * @param percentComplete How complete the song is, for the playback bar
     */
    public void updatePlaybackPosition(String currentTime, float percentComplete) {
        ((TextView) findViewById(R.id.songCurrentPositionDisplay)).setText(currentTime);
        ((ProgressBar) findViewById(R.id.progressBar)).setProgress((int) percentComplete);
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
        intentFilter.addAction(MusicPlayerService.SERVICE_REBOUND);
        intentFilter.addAction(MusicPlayerService.SERVICE_BOUND);

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
     * Launches the MediaContentProvider activity, but index into the current album and artist.
     * This allows us to reuse that code to represent a "playlist", aka, a list of the current
     * songs in the queue. Sneaky
     *
     * @param v The button clicked
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
     * Updates the value of the repeat setting on the UI and in the music player
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
     * Deals with the user giving a song a rating, updates the UI to display the new rating,
     * and updates the database
     *
     * @param v Which star button was clicked
     */
    public void onStarClick(View v) {
        // Firstly, we get which star was clicked, and update all other stars to be on or off
        String idAsString = v.getResources().getResourceName(v.getId());
        int clickedStar = Integer.parseInt(idAsString.substring(idAsString.length() - 1));
        setStarsOn(clickedStar);

        // Finally, we send the updated rating to the database for storage
        // TODO
    }

    /**
     * TODO
     * @param clickedStar
     */
    public void setStarsOn(int clickedStar) {
        for (int i = 1; i <= 5; i++) {
            int resourceId = getResources().getIdentifier("btnStar" + i, "id", getPackageName());
            ImageButton star = (ImageButton) findViewById(resourceId);

            if (i <= clickedStar) {
                star.setBackgroundResource(R.drawable.star_on_button);
            } else {
                star.setBackgroundResource(R.drawable.star_off_button);
            }
        }
    }

    /**
     * Updates the value of the shuffle setting on the UI and in the music player
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
            showAlbumArtwork(albumArtPath);

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
        unbindService(serviceConnection);
        playbackBar.terminate();

        // If music playback is stopped, and the app closed, destroy the service
        if (!musicPlayerBinder.isPlaying()) {
            stopService(musicServiceIntent);
        }
    }

    /**
     * When the service connection is initiated, will look for any shared preferences to load.
     * Currently it will get the shuffle setting the user had when they were last using the application,
     * the repeat setting the user had when they were last using the application, and the queue the user
     * had when they were last using the application,
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

    /**
     * Displays the artwork for an album, or display a message saying there is none
     *
     * @param path The path of the artwork
     */
    public void showAlbumArtwork(String path) {
        albumArtworkPath = path;
        Bitmap bmImg = BitmapFactory.decodeFile(albumArtworkPath);
        if (bmImg == null) {
            findViewById(R.id.noAlbumArtFound).setVisibility(View.VISIBLE);
        } else {
            BitmapDrawable background = new BitmapDrawable(bmImg);
            findViewById(R.id.backgroundImg).setBackgroundDrawable(background);
            findViewById(R.id.noAlbumArtFound).setVisibility(View.GONE);
        }
        findViewById(R.id.noSongSelected).setVisibility(View.GONE);
    }

    /**
     *
     */
    public void onBroadcastReceived(Context context, Intent intent) {
        if (intent.getAction().equals(MusicPlayer.MUSIC_PLAYER_BROADCAST)) {
            // Broadcast to tell us song playback has changed in someway, sent from music player class
            boolean keepPlaybackPosition = intent.getBooleanExtra(PlayerActivity.KEEP_PLAYBACK_POSITION, false);

            if (keepPlaybackPosition) {
                updatePlayingUI(false);
            } else {
                updatePlayingUI(true);
                updatePlaybackPosition(getString(R.string.time_zero), 0);
            }

        } else if (intent.getAction().equals(PlaybackBarManager.MUSIC_PROGRESS_BROADCAST)) {
            // Broadcast to tell us the music playback has progressed, sent from playback bar manager
            Bundle bundle = intent.getExtras();

            updatePlaybackPosition(
                    bundle.getString(PlaybackBarManager.CURRENT_TIME),
                    bundle.getFloat(PlaybackBarManager.PERCENT_COMPLETE)
            );

        } else if (intent.getAction().equals(MusicPlayerService.SERVICE_BOUND)) {
            // Broadcast to tell us when the service is first created
            // If this is the case, bring up the music selector straight away
            onBrowseClick(null);

        } else if (intent.getAction().equals(MusicPlayerService.SERVICE_REBOUND)) {
            // Broadcast to tell us when the service is rebound, sent from the music service
            updatePlayingUI(true);
            albumArtworkPath = musicPlayerBinder.getPlayingSong().getArtwork();
            showAlbumArtwork(albumArtworkPath);

            playbackBar.seekToPosition(musicPlayerBinder.getPercentComplete());

            findViewById(R.id.browseButton).setVisibility(View.GONE);
            findViewById(R.id.browseButtonIcon).setVisibility(View.VISIBLE);
            findViewById(R.id.browsePlaylistIcon).setVisibility(View.VISIBLE);
        }

        if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
            Log.d("cxk-db", "----------------HEADPHONES?!?!?!");
        }
    }
}
