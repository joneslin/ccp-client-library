package com.coretronic.ccpclient.CCPUtils.Example;

import android.content.Context;
import android.content.Intent;

/**
 * Created by Jones Lin on 2019-09-17.
 */
public class BroadcastExample {
        private void sendMessageToService(Context context){
        Intent intent = new Intent("coretronic.intent.action.iot.client.message");
        intent.putExtra("message", "client to service");
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        context.sendBroadcast(intent);
    }
}
