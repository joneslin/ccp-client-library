package com.coretronic.ccpclientlibrary.CCPUtils.BroadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ClientBroadcastReceiver extends BroadcastReceiver {

    public static final String TAG = ClientBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: " + intent.getStringExtra("message"));
    }
}