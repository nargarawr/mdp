package com.example.cxk.mupsyck;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;

/**
 * Class MusicPlayerService
 * <p/>
 * A long running bound service that runs in the background and manages the music player
 */
public class MusicPlayerService extends Service {

    private MusicPlayerBinder binder;
    private MusicPlayer musicPlayer;
    private LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(this);

    public static String SERVICE_REBOUND = "SERVICE_REBOUND";
    public static String SERVICE_BOUND = "SERVICE_BOUND";

    public static int SERVICE_ID = 999;
    public static int NOTIFICATION_PENDING_INTENT_REQUEST_CODE = 50;

    /**
     * Creates a new music player, and binder when the service is created
     */
    @Override
    public void onCreate() {
        Log.d("cxk-db", "MusicPlayerService::onCreate()");
        super.onCreate();
        binder = new MusicPlayerBinder(this);
        musicPlayer = new MusicPlayer(getApplicationContext(), broadcaster, this);
    }

    /**
     * Called by the system every time a client explicitly starts the service by calling
     * {@link android.content.Context#startService}, providing the arguments it supplied and a
     * unique integer token representing the start request.  Do not call this method directly.
     *
     * @param intent  The Intent supplied to {@link android.content.Context#startService},
     *                as given.  This may be null if the service is being restarted after
     *                its process has gone away, and it had previously returned anything
     *                except {@link #START_STICKY_COMPATIBILITY}.
     * @param flags   Additional data about this start request.  Currently either
     *                0, {@link #START_FLAG_REDELIVERY}, or {@link #START_FLAG_RETRY}.
     * @param startId A unique integer representing this specific request to
     *                start.  Use with {@link #stopSelfResult(int)}.
     * @return The service start type
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sendNotification(null);
        return Service.START_STICKY;
    }

    /**
     * Nullifies the music player when the service is destroyed
     */
    @Override
    public void onDestroy() {
        musicPlayer = null;
        stopForeground(true);
        super.onDestroy();
    }

    /**
     * Return the communication channel to the service.  May return null if
     * clients can not bind to the service.  The returned
     * {@link android.os.IBinder} is usually for a complex interface
     * that has been <a href="{@docRoot}guide/components/aidl.html">described using
     * aidl</a>.
     *
     * @param intent The Intent that was used to bind to this service,
     *               as given to {@link android.content.Context#bindService
     *               Context.bindService}.  Note that any extras that were included with
     *               the Intent at that point will <em>not</em> be seen here.
     * @return Return an IBinder through which clients can call on to the
     * service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        Intent broadcastIntent = new Intent(SERVICE_BOUND);
        broadcaster.sendBroadcast(broadcastIntent);
        return binder;
    }

    /**
     * Called when new clients have connected to the service, after it had
     * previously been notified that all had disconnected in its
     * {@link #onUnbind}.  This will only be called if the implementation
     * of {@link #onUnbind} was overridden to return true.
     *
     * @param intent The Intent that was used to bind to this service,
     *               as given to {@link android.content.Context#bindService
     *               Context.bindService}.  Note that any extras that were included with
     *               the Intent at that point will <em>not</em> be seen here.
     */
    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Intent broadcastIntent = new Intent(SERVICE_REBOUND);
        broadcaster.sendBroadcast(broadcastIntent);
    }

    /**
     * Called when all clients have disconnected from a particular interface
     * published by the service.  The default implementation does nothing and
     * returns false.
     *
     * @param intent The Intent that was used to bind to this service,
     *               as given to {@link android.content.Context#bindService
     *               Context.bindService}.  Note that any extras that were included with
     *               the Intent at that point will <em>not</em> be seen here.
     * @return Return true if you would like to have the service's
     * {@link #onRebind} method later called when new clients bind to it.
     */
    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    /**
     * Create an return the notification to display, based on the current song
     *
     * @param currentSong The song currently playing
     *
     * @return A notification object of the currently playing song
     */
    public Notification getNotification(Song currentSong, PendingIntent pendingIntent) {
        // Create the notification builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // Add the top bar icon
        int topBarIcon;

        // If we're on lollipop, use the white icon
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            topBarIcon = this.isPlaying()
                    ? R.drawable.notification_playing_icon_white
                    : R.drawable.notification_paused_icon_white;
        } else {
            topBarIcon = this.isPlaying()
                    ? R.drawable.notification_playing_icon
                    : R.drawable.notification_paused_icon;
        }
        builder.setSmallIcon(topBarIcon);

        // If the album has artwork, display this as the big icon, otherwise display the top bar icon again
        Bitmap bmImg = BitmapFactory.decodeFile(currentSong.getArtwork());
        if (bmImg == null) {
            builder.setLargeIcon(BitmapFactory.decodeResource(this.getResources(), topBarIcon));
        } else {
            // Scale the image to fit the notification square perfectly
            Resources res = this.getResources();
            builder.setLargeIcon(Bitmap.createScaledBitmap(
                    bmImg,
                    (int) res.getDimension(android.R.dimen.notification_large_icon_width),
                    (int) res.getDimension(android.R.dimen.notification_large_icon_height),
                    false
            ));
        }

        // Display song information
        builder.setContentTitle(currentSong.getArtist());
        builder.setContentText(currentSong.getName());
        builder.setTicker(currentSong.getName());

        // Priority, un-closable, and the intent to send
        builder.setContentIntent(pendingIntent);
        builder.setOngoing(true);
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);

        return builder.build();
    }

    /**
     * Sends a notification to the user, displaying the song and artist currently playing, and
     * allowing them to re-launch the activity after it's destruction and continue their
     * interaction with the player
     *
     * @param currentSong The song currently playing
     */
    public void sendNotification(Song currentSong) {
        if (currentSong == null) {
            return;
        }

        Intent notificationIntent = new Intent(this, PlayerActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                NOTIFICATION_PENDING_INTENT_REQUEST_CODE,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        Notification notification = getNotification(currentSong, pendingIntent);

        startForeground(SERVICE_ID, notification);
    }

    /**
     * Loads the given music into the music player playback queue
     *
     * @param songs      ArrayList of Songs to load
     * @param start_from What to set the initial playback queue index to
     */
    public void loadMusic(ArrayList<Song> songs, int start_from) {
        musicPlayer.loadMusicIntoPlaybackQueue(songs, start_from);
    }

    /**
     * Begin or resume playback of the song at the current index of the playback queue
     */
    public void beginPlayback() {
        musicPlayer.beginPlayback();
    }

    /**
     * Pauses playback of the current song
     */
    public void pausePlayback() {
        musicPlayer.pausePlayback();
    }

    /**
     * Stops playback of the current song, resets the music player and resets the datasource
     */
    public void stopPlayback() {
        musicPlayer.stopPlayback();
    }

    /**
     * Returns whether or not the music player is currently playing
     *
     * @return Whether the music player is currently playing
     */
    public boolean isPlaying() {
        return musicPlayer.isPlaying();
    }

    /**
     * Returns whether or not the music player has a queue loaded
     *
     * @return Whether the music player has a queue loaded
     */
    public boolean hasQueue() {
        return musicPlayer.hasQueue();
    }

    /**
     * Clears the playback queue
     */
    public void clearQueue() {
        musicPlayer.clearQueue();
    }

    /**
     * Plays the next song in the playback queue
     */
    public void playNext() {
        musicPlayer.playNext();
    }

    /**
     * Returns the song at the current index of the music player playback queue
     *
     * @return The Song at the current index of the music player playback queue
     */
    public Song getPlayingSong() {
        return musicPlayer.getPlayingSong();
    }

    /**
     * Plays the previous song in the playback queue
     */
    public void playPrevious() {
        musicPlayer.playPrevious();
    }

    /**
     * Sets the repeat settings of the music player
     *
     * @param loopingAll Whether we should loop all songs
     * @param loopingOne Whether we should loop a single song
     */
    public void setRepeatSettings(boolean loopingAll, boolean loopingOne) {
        musicPlayer.setRepeatSettings(loopingAll, loopingOne);
    }

    /**
     * Sets the shuffle setting of the music player
     *
     * @param shuffle Whether we should shuffle playback
     */
    public void setShuffleSetting(boolean shuffle) {
        musicPlayer.setShuffleSetting(shuffle);
    }

    /**
     * Seek to the a particular position in the playback bar, as a percentage of the total width
     *
     * @param percent The percent to seek to
     */
    public void seekToPosition(float percent) {
        musicPlayer.seekToPosition(percent);
    }

    /**
     * Gets the percentage of how complete this song is (0-100)
     *
     * @return An integer between 0 and 100 for how complete the current song is
     */
    public int getPercentComplete() {
        return musicPlayer.getPercentComplete();
    }

    /**
     * Send a broadcast from the music player to tell the PlayerActivity that playback has changed
     *
     * @param keepPlaybackPosition Whether or not to keep where we are in the song on the UI, or reset it
     */
    public void sendMusicPlayerBroadcast(boolean keepPlaybackPosition) {
        musicPlayer.sendBroadcast(keepPlaybackPosition);
    }
}
