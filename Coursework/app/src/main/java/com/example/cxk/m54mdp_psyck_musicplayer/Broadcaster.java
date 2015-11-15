package com.example.cxk.m54mdp_psyck_musicplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * TODO
 */
public class Broadcaster extends BroadcastReceiver {

    final static String PLAYBACK_STOPPED = "PLAYBACK_STOPPED";

    /**
     * TODOmya
     */
    public Broadcaster () {

    }

    /**
     * TODO
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("myapp-b", "Message broadcast");
    }


}
