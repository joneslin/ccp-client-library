package com.coretronic.ccpclient.CCPUtils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;
import com.coretronic.ccpclient.CCPUtils.Detector.CCPDetector;
import com.coretronic.ccpclient.CCPUtils.Interface.CCPAidlInterface;
import com.coretronic.ccpclient.CCPUtils.Logger.DiskLogHandler;
import com.coretronic.ccpclient.CCPUtils.Logger.User;
import com.coretronic.ccpservice.ICCPAidlInterface;
import com.google.gson.Gson;
import com.orhanobut.logger.CsvFormatStrategy;
import com.orhanobut.logger.DiskLogAdapter;
import com.orhanobut.logger.DiskLogStrategy;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;

/**
 * Created by Jones Lin on 2019-08-22.
 */
public class CCPStarter {
    Context context;
    ICCPAidlInterface iccpAidlInterface = null;
    ServiceConnection serviceConnection = null;
    CCPAidlInterface ccpAidlInterface;
    public ICCPAidlInterface getIccpAidlInterface() {
        return iccpAidlInterface;
    }
    public ServiceConnection getServiceConnection() {
        return serviceConnection;
    }

    public CCPStarter(Context context, CCPAidlInterface ccpAidlInterface) {
        this.context = context;
        this.ccpAidlInterface = ccpAidlInterface;
    }

    public CCPStarter(Context context) {
        this.context = context;
    }

    public void start(){
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                iccpAidlInterface = ICCPAidlInterface.Stub.asInterface(iBinder);
                Toast.makeText(context.getApplicationContext(),	"This APP already connected to CCP Service !!", Toast.LENGTH_SHORT).show();
                ccpAidlInterface.alreadyConnected();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };

        // CCP Detector. 偵測ccp若存在則啟動，cpp不存在則下載並啟動，也一併啟動bind service。
        CCPDetector ccpDetector = new CCPDetector(context, iccpAidlInterface, serviceConnection, false);
        ccpDetector.startCCPService();

        // example: Boardcast method.
        sendMessageToService();

        // example: Save Log.
        saveLogToFile();
    }

    private void sendMessageToService(){
        Intent intent = new Intent("coretronic.intent.action.iot.client.message");
        intent.putExtra("message", "client to service");
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        context.sendBroadcast(intent);
    }

    private void saveLogToFile(){
        //標準sdcard/logger路徑、logs_檔名、500k檔案大小
//        FormatStrategy formatStrategy = CsvFormatStrategy.newBuilder()
//                .tag(this.getPackageName())
//                .build();

        // 客製化路徑、檔名、檔案大小
        FormatStrategy formatStrategy = CsvFormatStrategy.newBuilder()
                .tag(context.getPackageName())
                .logStrategy(new DiskLogStrategy(new DiskLogHandler(Environment.getExternalStorageDirectory().getAbsolutePath()+"/logger/", "log", 1000*1000)))
                .build();

        Logger.addLogAdapter(new DiskLogAdapter(formatStrategy));
        for (int i=0; i<=20000; i++) {
            User user = new User();
            user.setAccount("這是帳號");
            user.setMobile("這是電話");
            Logger.d(new Gson().toJson(user));
        }
    }

}
