package com.example.cxk.mupsyck;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Class HeadphoneStateListener
 * <p/>
 * Used exclusively to listen to the plugging in and unplugging of headphones to the device and to
 * pause playback when headphones are unplugged
 */
public class HeadphoneStateListener extends BroadcastReceiver {
    private MusicPlayerBinder musicPlayerBinder;
    private int previousState;
    private boolean firstStateRecorded = false;

    /**
     * Default Constructor, assigns member variables
     *
     * @param musicPlayerBinder The music player binder object
     */
    public HeadphoneStateListener(MusicPlayerBinder musicPlayerBinder) {
        this.musicPlayerBinder = musicPlayerBinder;
    }

    /**
     * What to do when a headphones are plugged in or unplugged. Firstly, records the initial
     * state of the headphones, after this, when head phones are unplugged, music playback is paused.
     *
     * @param context Application context
     * @param intent  Headset intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
            int state = intent.getIntExtra("state", -1);

            // Record the initial state
            if (!firstStateRecorded) {
                previousState = state;
                firstStateRecorded = true;
                return;
            }

            // If the headphone state has now changed
            if (state != previousState) {
                previousState = state;

                if (state == 0) {
                    // If we have just unplugged our headphones, pause playback
                    musicPlayerBinder.pausePlayback();
                    // Updates the UI to be paused as well
                    musicPlayerBinder.sendMusicPlayerBroadcast(true);
                }
            }
        }
    }
}
