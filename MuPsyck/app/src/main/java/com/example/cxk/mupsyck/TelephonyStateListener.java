package com.example.cxk.mupsyck;

import android.telephony.PhoneStateListener;

/**
 * Class TelephonyStateListener
 * <p/>
 * Used exclusively to listen to the state of telephony on the device, and to pause playing music
 * when a phone call is received.
 */
public class TelephonyStateListener extends PhoneStateListener {

    private MusicPlayerBinder musicPlayerBinder;
    private boolean wasPlaying = false;

    /**
     * Default Constructor, assigns member variables
     *
     * @param musicPlayerBinder The music player binder object
     */
    public TelephonyStateListener(MusicPlayerBinder musicPlayerBinder) {
        this.musicPlayerBinder = musicPlayerBinder;
    }

    /**
     * What to do when a phone call is received or hung up. If one is received, music will be paused,
     * and when it is hung up, if music was playing previously, it will begin playing again
     *
     * @param state Telephone call state
     * @param incomingNumber The number ringing the phone, if any
     */
    public void onCallStateChanged(int state, String incomingNumber) {
        if (state == 1) { // phone call received
            wasPlaying = musicPlayerBinder.isPlaying();
            musicPlayerBinder.pausePlayback();
        } else if (state == 0 && wasPlaying) { // phone call ended
            musicPlayerBinder.beginPlayback();
            wasPlaying = false;
        }
    }
}